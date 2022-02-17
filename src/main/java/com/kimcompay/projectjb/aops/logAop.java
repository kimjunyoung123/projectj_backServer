package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;

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

    @Around("execution(* com.kimcompay.projectjb.users.company.flyerService.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.userService.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.storeService.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.productService.*(..))")
    public Object writeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("mehtod: "+method.getName());
        int len=method.getParameterCount();
        String[] paramNames=signature.getParameterNames();
        Object[] paramValues =joinPoint.getArgs();
        for(int i=0;i<len;i++){
            logger.info("요청변수: "+paramNames[i]+" 요청값: "+paramValues[i].toString());
        }
      
        return joinPoint.proceed();   
    }
}
