package com.kimcompay.projectjb.users;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
public class principalDetails implements UserDetails {
    private Logger logger=LoggerFactory.getLogger(principalDetails.class);
    private Map<Object,Object>princi=new HashMap<>();
    private boolean is_can=false;

    public principalDetails(Map<Object,Object>map,boolean checkLoginDate){
        this.princi=map;
        if(checkLoginDate){
            is_can=check_lock(Integer.parseInt(map.get("sleep").toString()),map.get("login_date").toString());
        }
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        logger.info("getAuthorities");
        String role=princi.get("role").toString();
        Collection<GrantedAuthority>roles=new ArrayList<>();
        logger.info("유저의 권한: "+role);
        roles.add(new SimpleGrantedAuthority(role));
        return roles;
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return princi.get("pwd").toString();
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return princi.get("email").toString();
    }

    @Override
    public boolean isAccountNonExpired() {//계정만료여부
        // TODO Auto-generated method stub
        return is_can;
    }

    @Override
    public boolean isAccountNonLocked() {//계정잠금여부
        // TODO Auto-generated method stub
        return is_can;
    }

    @Override
    public boolean isCredentialsNonExpired() {//계정패스워드 만료여부
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled() {//계정이 사용가능한지 여부
        // TODO Auto-generated method stub
        return is_can;
    }
    public String getRole() {
        return princi.get("role").toString();
    }
    private Boolean check_lock(int num,String loginDate){
        logger.info("check_lock");
        if(num!=0||LocalDateTime.now().isAfter(Timestamp.valueOf(loginDate).toLocalDateTime().plusYears(1))){
            return false;
        }
        return true;
    }
    
}
