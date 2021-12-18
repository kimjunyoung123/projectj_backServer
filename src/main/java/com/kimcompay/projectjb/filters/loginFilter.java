package com.kimcompay.projectjb.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.company.comDetail;
import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.user.userDetail;
import com.kimcompay.projectjb.users.user.userVo;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class loginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final Logger logger=LoggerFactory.getLogger(loginFilter.class);
    private jwtService jwtService;
    private AuthenticationManager authenticationManager;

    public loginFilter(jwtService jwtService,AuthenticationManager authenticationManager){
        this.jwtService=jwtService;
        this.authenticationManager=authenticationManager;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException {
        logger.info("loginFilter");
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            JSONObject jsonObject=objectMapper.readValue(request.getInputStream(), JSONObject.class);
            logger.info("로그인시도 정보: "+jsonObject);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jsonObject.get("email"),jsonObject.get("pwd")));
        } catch (Exception e) {
            e.printStackTrace();
            throw utillService.makeRuntimeEX("로그인에 실패했습니다", "");
        }
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,Authentication authResult) throws IOException, ServletException {
        logger.info("successfulAuthentication");
        String email=null;
        SecurityContextHolder.getContext().setAuthentication(authResult);
        try {
            userDetail userDetail=(userDetail)authResult.getPrincipal();
            email=userDetail.getUservo().getEmail();
        } catch (Exception e) {
            logger.info("유저 디테일 미존재 기업꺼내기");
            comDetail comDetail=(comDetail)authResult.getPrincipal();
            email=comDetail.getComVo().getCemail();
        }
        String access_token=jwtService.get_access_token(email);
        String refresh_token=jwtService.get_refresh_token();
        logger.info("엑세스토큰: "+access_token);
        logger.info("리프레시토큰: "+refresh_token);
        

  
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,AuthenticationException failed) throws IOException, ServletException {
        logger.info("unsuccessfulAuthentication");
        logger.info("로그인 실패");
    }
}
