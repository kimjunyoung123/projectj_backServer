package com.kimcompay.projectjb.configs;

import com.kimcompay.projectjb.filters.corsFilter;
import com.kimcompay.projectjb.filters.loginFilter;
import com.kimcompay.projectjb.jwt.jwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration//빈등록: 스프링 컨테이너에서 객체에서 관리
@EnableWebSecurity/////필터를 추가해준다
public class securityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private corsFilter corsFilter;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    /*@Autowired
    private userDao userDao;*/

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
            .addFilter(corsFilter.crosfilter())
            .addFilter(new loginFilter(jwtService,authenticationManager(),redisTemplate))
            //.addFilter(new authorizationFilter(authenticationManager(),jwtService,userDao))
            .csrf().disable().formLogin().disable().httpBasic().disable()
            .authorizeRequests().antMatchers("/api/**").authenticated().anyRequest().permitAll();

    }
}
