package com.kimcompay.projectjb.users;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
public class principalDetails implements UserDetails {
    
    private Map<Object,Object>princi=new HashMap<>();
    private boolean is_can=false;


    public principalDetails(Map<Object,Object>map){
        this.princi=map;
        is_can=check_lock(Integer.parseInt(map.get("sleep").toString()),map.get("login_date").toString());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority>roles=new ArrayList<>();
        roles.add(new GrantedAuthority(){
            @Override
            public String getAuthority() {
                System.out.println(princi.get("role")+"권한 가져오기");
                return princi.get("role").toString();
            }
        });
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
    private Boolean check_lock(int num,String loginDate){
        if(num!=0||LocalDateTime.now().isAfter(Timestamp.valueOf(loginDate).toLocalDateTime().plusYears(1))){
            return false;
        }
        return true;
    }
    
}
