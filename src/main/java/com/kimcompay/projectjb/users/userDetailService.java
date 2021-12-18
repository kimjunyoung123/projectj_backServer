package com.kimcompay.projectjb.users;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.users.company.comDetail;
import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.company.compayDao;
import com.kimcompay.projectjb.users.user.userDetail;
import com.kimcompay.projectjb.users.user.userVo;
import com.kimcompay.projectjb.users.user.userdao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class userDetailService implements UserDetailsService {
    private Logger logger=LoggerFactory.getLogger(userDetailService.class);
    
    @Autowired
    private userdao userdao;
    @Autowired
    private compayDao compayDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("loadUserByUsername");
        try {
            userVo userVo=userdao.findByEmail(username).orElseThrow(()->new NullPointerException("유저정보가없음"));
            return new userDetail(userVo);
        } catch (NullPointerException e) {
            logger.info("기업회원인지 찾기");
            try {
                comVo comVo=compayDao.findByCemail(username).orElseThrow(()->utillService.makeRuntimeEX("회원정보가 없습니다", "loadUserByUsername"));
                return new comDetail(comVo);
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
}
