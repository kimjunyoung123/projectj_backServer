package com.kimcompay.projectjb.aops;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class logAop {
    private Logger logger=LoggerFactory.getLogger(logAop.class); 

   // @Before()
    public void name(JoinPoint JoinPoint) {
        
    }
}
