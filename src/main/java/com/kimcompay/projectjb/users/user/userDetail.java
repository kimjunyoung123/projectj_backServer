package com.kimcompay.projectjb.users.user;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
public class userDetail implements UserDetails {
    
    private userVo uservo;
    
    public userDetail(userVo uservo){
        this.uservo=uservo;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority>roles=new ArrayList<>();
        roles.add(new GrantedAuthority(){
            @Override
            public String getAuthority() {
                System.out.println(uservo.getUrole()+"권한 가져오기");
                return uservo.getUrole();
            }
        });
        return roles;
    }

    @Override
    public String getPassword() {
        return uservo.getUpwd();
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return uservo.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }
    
}
