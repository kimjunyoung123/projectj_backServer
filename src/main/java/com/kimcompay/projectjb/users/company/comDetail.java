package com.kimcompay.projectjb.users.company;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
public class comDetail implements UserDetails {
    private comVo comVo;
    private boolean is_can=false;

    public comDetail(comVo comVo){
        this.comVo=comVo;
        this.is_can=check_lock(comVo.getCsleep());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return comVo.getCpwd();
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return comVo.getCemail();
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
    private Boolean check_lock(int num){
        if(num==0){
            return true;
        }
        return false;
    }
    
}
