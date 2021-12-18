package com.kimcompay.projectjb.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class corsFilter {

    @Value("${front.domain}")
    private String front_domain;
    @Value("${foword.front}")
    private String foword_front;

    @Bean
    public CorsFilter crosfilter() {
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration=new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin(front_domain);
        configuration.addAllowedOrigin(foword_front);
        //configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("refreshToken");
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
