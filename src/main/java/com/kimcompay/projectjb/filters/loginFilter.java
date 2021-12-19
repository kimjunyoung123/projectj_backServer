package com.kimcompay.projectjb.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.user.userVo;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private RedisTemplate<String,String>redisTemplate;

    //@Value("${access_token_cookie}")
    private String access_cookie_name="actk";
    //@Value("${refresh_token_cookie}")
    private String refresh_cookie_name="rftk";

    public loginFilter(jwtService jwtService,AuthenticationManager authenticationManager,RedisTemplate<String,String>redisTemplate){
        this.jwtService=jwtService;
        this.authenticationManager=authenticationManager;
        this.redisTemplate=redisTemplate;
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
        //프린시펄디에일 꺼내기
        principalDetails principalDetails=(principalDetails)authResult.getPrincipal();
        Map<String,Object>map=principalDetails.getPrinci();
        //vo->map으로 변환
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> result = objectMapper.convertValue(map.get("dto"), Map.class);
        logger.info("로그인정보: "+result.toString());
        //redis밀어넣기
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(map.get("email").toString(),result);
        //토큰생성
        String access_token=jwtService.get_access_token(email);
        String refresh_token=jwtService.get_refresh_token();
        logger.info("엑세스토큰: "+access_token);
        logger.info("리프레시토큰: "+refresh_token);
        //레디스에 유저정보 담기
        //쿠키저장
        Map<String,Object>infor=new HashMap<>();
        infor.put(access_cookie_name, access_token);
        infor.put(refresh_cookie_name, refresh_token);
        utillService.makeCookie(infor, response);
        
  
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,AuthenticationException failed) throws IOException, ServletException {
        logger.info("unsuccessfulAuthentication");
        logger.info("로그인 실패");
    }
}
