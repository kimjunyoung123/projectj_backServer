package com.kimcompay.projectjb.filters;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.principalDetails;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class loginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final Logger logger=LoggerFactory.getLogger(loginFilter.class);
    private jwtService jwtService;
    private AuthenticationManager authenticationManager;
    private RedisTemplate<String,Object>redisTemplate;

    private String access_cookie_name;
    private String refresh_cookie_name;

    public loginFilter(jwtService jwtService,AuthenticationManager authenticationManager,RedisTemplate<String,Object>redisTemplate,String access_cookie_name,String refresh_cookie_name){
        this.jwtService=jwtService;
        this.authenticationManager=authenticationManager;
        this.redisTemplate=redisTemplate;
        this.access_cookie_name=access_cookie_name;
        this.refresh_cookie_name=refresh_cookie_name;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException {
        logger.info("loginFilter");
            ObjectMapper objectMapper=new ObjectMapper();
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject = objectMapper.readValue(request.getInputStream(), JSONObject.class);
            } catch (StreamReadException e) {
                logger.info("로그인 시도중 값 변환에 실패했습니다");
                e.printStackTrace();
            } catch (DatabindException e) {
                logger.info("로그인 시도중 값 변환에 실패했습니다");
                e.printStackTrace();
            } catch (IOException e) {
                logger.info("로그인 시도중 값 변환에 실패했습니다");
                e.printStackTrace();
            }
            logger.info("로그인시도 정보: "+jsonObject);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jsonObject.get("email"),jsonObject.get("pwd")));
    
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,Authentication authResult) throws IOException, ServletException {
        logger.info("successfulAuthentication");
        String id=null;
        SecurityContextHolder.getContext().setAuthentication(authResult);
        //프린시펄디에일 꺼내기
        principalDetails principalDetails=(principalDetails)authResult.getPrincipal();
        Map<Object,Object>result=principalDetails.getPrinci();
        id=result.get("id").toString();
       /* //vo->map으로 변환
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> result= objectMapper.convertValue(map.get("dto"), Map.class);*/
        Timestamp loginDate=Timestamp.valueOf(LocalDateTime.now());
        result.put("pwd", null);//비밀번호 지우기
        result.put("loginDate",loginDate);//로그인 날짜부여
        logger.info("로그인정보: "+result.toString());
        //토큰발급
        String access_token=jwtService.get_access_token(id);
        String refresh_token=jwtService.get_refresh_token();
        logger.info("엑세스토큰: "+access_token);
        logger.info("리프레시토큰: "+refresh_token);
        //토큰 쿠키로 발급
        utillService.makeLoginCookie(access_token, refresh_token,access_cookie_name,refresh_cookie_name);
        //redis 유저정보 밀어넣기
        //리프레시토큰 찾기위해 넣는것
        redisTemplate.opsForHash().put(refresh_cookie_name, refresh_cookie_name, refresh_token);
        redisTemplate.opsForHash().put(id+senums.loginTextRedis.get(), id+senums.loginTextRedis.get(),result);
        //리프레시토큰을 넣는것
        redisTemplate.opsForValue().set(refresh_token,id);
        logger.info("로그인 과정완료");
        utillService.goFoward("/login?flag=true&date="+loginDate+"&kind="+result.get("kind") , request, response);
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,AuthenticationException failed) throws IOException, ServletException {
        logger.info("unsuccessfulAuthentication");
        logger.info("로그인 실패");
        //로그인 실패원인 분류
        //failed.getClass().getSimpleName()도 되지만 instanceofeh도 가능
        String cause=null;
        if(failed instanceof BadCredentialsException){
            cause="이메일 혹은 비밀번호가 일치하지 않습니다";
        }else if(failed instanceof InternalAuthenticationServiceException){
            cause="가입한 이메일이 없습니다";
        }else if(failed instanceof LockedException || failed instanceof DisabledException){
            cause="계정이 잠겨있습니다";
        }else{
            cause="알수 없는 오류 발생";
        }
        utillService.goFoward("/login?flag=false&cause="+cause, request, response);
    }
}
