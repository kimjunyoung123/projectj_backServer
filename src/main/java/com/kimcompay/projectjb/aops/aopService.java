package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class aopService {
    private Logger logger=LoggerFactory.getLogger(aopService.class); 
    @Autowired
    private fileService fileService;
    
    @Async
    @Before("execution(* com.kimcompay.projectjb.users.company.*.*(..))"
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
    public void writeLog(JoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("pakage: "+signature.getDeclaringTypeName());
        logger.info("mehtod: "+method.getName());
        logger.info("요청변수: "+utillService.arrToLogString(signature.getParameterNames())+" 요청값: "+utillService.arrToLogString(joinPoint.getArgs()));
    }
    @Async
    @AfterReturning(value = "execution(* com.kimcompay.projectjb.users.company.service.storeService.tryInsert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.storeService.tryUpdate(..))",returning="response")
    public void doneInserOrUpdate(JoinPoint joinPoint,Object response) {
        logger.info("doneInserOrUpdate");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Object[] values=joinPoint.getArgs();
        String[] keys=signature.getParameterNames();
        SecurityContextHolderAwareRequestWrapper SecurityContextHolderAwareRequestWrapper=null;
        logger.info("pakage: "+signature.getDeclaringTypeName());
        for(int i=0;i<keys.length;i++){
            if(keys[i].equals("request")){
                SecurityContextHolderAwareRequestWrapper=(org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper) values[i];
            }
        }
        System.out.println("return: "+response);
        //인증 세션 비우기
        /*HttpSession httpSession=SecurityContextHolderAwareRequestWrapper.getSession();
        httpSession.removeAttribute(senums.auth.get()+senums.phonet.get());
        //사용하지 않는 사진 지우기
        //이미지가 있다면 path제거후 이름만 얻어내기
        String text="";
        String 
        List<String>usingImgs=utillService.getOnlyImgNames(text);
        usingImgs.add(tryInsertStoreDto.getThumbNail().split("/")[4]);
        fileService.deleteFile(httpSession,usingImgs);
        httpSession.removeAttribute(senums.imgSessionName.get());
        System.out.println(SecurityContextHolderAwareRequestWrapper.getSession());*/
    }
    @Before("execution(* com.kimcompay.projectjb.controllers.restController.testaop(..))")
    public void name(JoinPoint joinPoint) {
        logger.info("name");
        System.out.println(utillService.getLoginId());
        logger.info(utillService.getHttpServletRequest().getParameter("test"));
        for (Object obj : joinPoint.getArgs()) {
            if (obj instanceof HttpServletRequest ) {
                HttpServletRequest request = (HttpServletRequest) obj;
                System.out.println(request.getParameter("test"));
                // Doing...

            }
        } 
    }
    @Before("execution(* com.kimcompay.projectjb.users.company.service.productService.getProductAndEvents(..))")
    public void checkOwner(JoinPoint joinPoint) {
        logger.info("checkOwner");
        logger.info(Integer.toString(utillService.getLoginId()));
        logger.info(utillService.getHttpServletRequest().getParameter("storeId"));
        
    }
    
    
}
