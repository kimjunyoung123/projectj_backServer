package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class userService {
    private Logger logger=LoggerFactory.getLogger(userService.class);

    @Autowired
    private jungbuService jungbuService;
    
    public void insert(tryInsertDto tryInsertDto,HttpSession session) {
        JSONObject res=jungbuService.getCompanyNum(Integer.parseInt(tryInsertDto.getCompany_num()));
        logger.info(res.toString());
    }
    private void checkValues(tryInsertDto tryInsertDto) {
        
    
    }
}
