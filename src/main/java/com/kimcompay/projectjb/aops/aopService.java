package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;

import com.kimcompay.projectjb.utillService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class aopService {
    private Logger logger=LoggerFactory.getLogger(aopService.class); 

    @Around("execution(* com.kimcompay.projectjb.users.company.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.aws.services.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.jungbu.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.kakao.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.naver.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.sns.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.requestTo.*(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.jwt.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.userService.*(..))"
    +"||execution(* com.kimcompay.projectjb.utillService.*(..))")
    public Object writeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("pakage: "+signature.getDeclaringTypeName());
        logger.info("mehtod: "+method.getName());
        logger.info("요청변수: "+utillService.arrToLogString(signature.getParameterNames())+" 요청값: "+utillService.arrToLogString(joinPoint.getArgs()));
        return joinPoint.proceed();   
    }
}
