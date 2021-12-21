package com.kimcompay.projectjb.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
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
    private RedisTemplate<String,String>redisTemplate;

    private String access_cookie_name;
    private String refresh_cookie_name;

    public loginFilter(jwtService jwtService,AuthenticationManager authenticationManager,RedisTemplate<String,String>redisTemplate,String access_cookie_name,String refresh_cookie_name){
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (DatabindException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("로그인시도 정보: "+jsonObject);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jsonObject.get("email"),jsonObject.get("pwd")));
    
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,Authentication authResult) throws IOException, ServletException {
        logger.info("successfulAuthentication");
        String email=null;
        SecurityContextHolder.getContext().setAuthentication(authResult);
        //프린시펄디에일 꺼내기
        principalDetails principalDetails=(principalDetails)authResult.getPrincipal();
        Map<Object,Object>result=principalDetails.getPrinci();
        email=principalDetails.getUsername();
       /* //vo->map으로 변환
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,Object> result= objectMapper.convertValue(map.get("dto"), Map.class);*/
        result.put("pwd", null);//비밀번호 지우기
        logger.info("로그인정보: "+result.toString());
        //토큰발급
        String access_token=jwtService.get_access_token(email);
        String refresh_token=jwtService.get_refresh_token();
        logger.info("엑세스토큰: "+access_token);
        logger.info("리프레시토큰: "+refresh_token);
        //쿠키저장
        Map<String,Object>infor=new HashMap<>();
        infor.put(access_cookie_name, access_token);
        infor.put(refresh_cookie_name, refresh_token);
        utillService.makeCookie(infor, response);
        //redis구격에 맞게 int값 ->string
        for(Entry<Object, Object> s:result.entrySet()){
            logger.info(s.getKey().toString());
            if(Optional.ofNullable(s.getValue()).orElseGet(()->null)==null){
                logger.info("null발견 무시");
                continue;
            }else if(!s.getValue().getClass().getSimpleName().equals("String")){
                result.put(s.getKey(), s.getValue().toString());
            }
        }
        //redis밀어넣기
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        result.put(refresh_cookie_name, refresh_token);
        hashOperations.putAll(email,result);
        logger.info("로그인 과정완료");
        utillService.goFoward("/login/login/null/?flag=true", request, response);
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
        utillService.goFoward("/login/login/null/?flag=false&cause="+cause, request, response);
    }
}
