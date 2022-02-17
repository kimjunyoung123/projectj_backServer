package com.kimcompay.projectjb.aops;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class logAop {
    private Logger logger=LoggerFactory.getLogger(logAop.class); 

    @Around("execution(* com.kimcompay.projectjb.users.company.flyerService.*(..))||execution(* com.kimcompay.projectjb.users.user.userService.*(..))")
    public Object writeLog(ProceedingJoinPoint JoinPoint) throws Throwable {
        logger.info("writeLog");
        logger.info("요청변수: "+JoinPoint.getArgs());
        return JoinPoint.proceed();   
    }
}
