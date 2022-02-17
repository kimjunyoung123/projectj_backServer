package com.kimcompay.projectjb.aops;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class logAop {
    private Logger logger=LoggerFactory.getLogger(logAop.class); 

    @Around("execution(* com.kimcompay.projectjb.users.company.flyerService.*(..))||execution(* com.kimcompay.projectjb.users.user.userService.*(..))")
    public Object writeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        logger.info("mehtod: "+signature.getName());
        logger.info("요청변수: "+joinPoint.getArgs());
        return joinPoint.proceed();   
    }
}
