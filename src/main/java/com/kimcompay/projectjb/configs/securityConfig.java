package com.kimcompay.projectjb.configs;

import com.kimcompay.projectjb.filters.authorizationFilter;
import com.kimcompay.projectjb.filters.corsFilter;
import com.kimcompay.projectjb.filters.loginFilter;
import com.kimcompay.projectjb.jwt.jwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration//빈등록: 스프링 컨테이너에서 객체에서 관리
@EnableWebSecurity/////필터를 추가해준다
public class securityConfig extends WebSecurityConfigurerAdapter {

    @Value("${access_token_cookie}")
    private String access_token_cookie_name;
    @Value("${refresh_token_cookie}")
    private String refresh_token_cookie_name;
    
    @Autowired
    private corsFilter corsFilter;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
    @Bean
    public BCryptPasswordEncoder pwdEncoder() {
       return  new BCryptPasswordEncoder();
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
             .csrf().disable()
            //.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            //.and()
            .addFilter(corsFilter.crosfilter())
            .addFilter(new loginFilter(jwtService,authenticationManager(),redisTemplate,access_token_cookie_name,refresh_token_cookie_name))
            .addFilter(new authorizationFilter(authenticationManager(),jwtService,redisTemplate,access_token_cookie_name,refresh_token_cookie_name))
            //.csrf().disable()
            
            .formLogin().disable().httpBasic().disable()
            .authorizeRequests().antMatchers("/auth/store/**").access("hasRole('ROLE_COMPANY') or hasRole('ROLE_ADMIN')").antMatchers("/auth/**").authenticated() 
            .anyRequest().permitAll();

    }
}
