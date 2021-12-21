package com.kimcompay.projectjb.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.principalDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class authorizationFilter extends BasicAuthenticationFilter  {
    private Logger logger=LoggerFactory.getLogger(authorizationFilter.class);
    private jwtService jwtService;
    private RedisTemplate<String,String>redisTemplate;
    private String access_cookie_name;
    private String refreshTokenCookieName;

    public authorizationFilter(AuthenticationManager authenticationManager,jwtService jwtService,RedisTemplate<String,String>redisTemplate,String access_cookie_name,String refreshTokenCookieName) {
        super(authenticationManager);
        this.jwtService=jwtService;
        this.redisTemplate=redisTemplate;
        this.access_cookie_name=access_cookie_name;
        this.refreshTokenCookieName=refreshTokenCookieName;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)throws IOException, ServletException {
        logger.info("doFilterInternal");
        logger.info("요청 uri: "+request.getRequestURI());
        String access_token=null;
        try {
            //엑세스 토큰&이메일 가져오기
            access_token=utillService.getCookieValue(request, access_cookie_name);
            logger.info("엑세스 토큰: "+access_token);
            String email=jwtService.openJwt(access_token);
            logger.info("이메일: "+email);
            //redis에서 꺼내기
            //시큐리티 인증세션 주입
            principalDetails principalDetails=new principalDetails(redisTemplate.opsForHash().entries(email));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principalDetails,null,principalDetails.getAuthorities()));

        } catch (TokenExpiredException e) {
            logger.info("만료된 토큰: "+access_token);
            //리프레쉬토큰 꺼내기
            String refreshToken=Optional.ofNullable(utillService.getCookieValue(request,refreshTokenCookieName)).orElseGet(()->null);
            logger.info("리프레시토큰: "+refreshToken);
            //레디스에서 이메일 가져오기
            String email=Optional.ofNullable(redisTemplate.opsForValue().get(refreshToken)).orElseGet(()->null);
            logger.info("redis에 서 찾은 이메일: "+email);
            if(refreshToken==null||email==null){
                utillService.goFoward("/tokenExpire/x", request, response);
                return;
            }
            //새 엑세스 토큰 발급
            String accessToken=jwtService.get_access_token(email);
            Map<String,Object>infor=new HashMap<>();
            infor.put(access_cookie_name, accessToken);
            utillService.makeCookie(infor, response);
            utillService.goFoward("/tokenExpire/y", request, response);
            return;
        }catch(NullPointerException e2){
            logger.info("토큰이 없음");
        }
        logger.info("인증필터 통과");
        chain.doFilter(request, response);
    }

    
}
