package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.tryUpdateStoreDto;
import com.kimcompay.projectjb.users.company.service.storeService;

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
    @Autowired
    private storeService storeService;

    private Map<String,Object>doAfter=new HashMap<>();
    
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
    +"||execution(* com.kimcompay.projectjb.utillService.*(..))"
    +"||execution(* com.kimcompay.projectjb.aops.*.*(..))")
    public void writeLog(JoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("pakage: "+signature.getDeclaringTypeName());
        logger.info("mehtod: "+method.getName());
        logger.info("요청변수: "+utillService.arrToLogString(signature.getParameterNames())+" 요청값: "+utillService.arrToLogString(joinPoint.getArgs()));
    }
    
    @Async
    @Before("execution(* com.kimcompay.projectjb.users.company.service.storeService.tryUpdate(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.*.*(..))")
    public void getImgAndText(JoinPoint joinPoint) {
        Object[] values=joinPoint.getArgs();
        for(Object value:values){
            if(value instanceof tryUpdateStoreDto || value instanceof tryInsertStoreDto){
                doAfter.put("dto", value);
                break;
            }
        }
    }
    private void deleteImgs(String text,String thumNail,HttpSession httpSession) {
        List<String>usingImgs=utillService.getOnlyImgNames(text);
        usingImgs.add(thumNail.split("/")[4]);
        fileService.deleteFile(httpSession,usingImgs);
        httpSession.removeAttribute(senums.imgSessionName.get());
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
    //매장정보 접근전 주인인지 확인
    @Before("execution(* com.kimcompay.projectjb.users.company.service.productService.getProductAndEvents(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDeliverAddress(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.getFlyers(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDelivers(..))")
    public void checkOwner(JoinPoint joinPoint) {
        logger.info("checkOwner");
        storeService.checkExist(Integer.parseInt(utillService.getHttpServletRequest().getParameter("storeId")));
    }
    //미사용 사진 정리
    @AfterReturning(value = "execution(* com.kimcompay.projectjb.users.company.service.storeService.tryInsert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.storeService.tryUpdate(..))",returning="response")
    public void doneInserOrUpdate(JoinPoint joinPoint,Object response) {
        logger.info("dto: "+doAfter.toString());
        //인증 세션 비우기
        HttpSession httpSession=utillService.getHttpServletRequest().getSession();
        httpSession.removeAttribute(senums.auth.get()+senums.phonet.get());
        Object dto=doAfter.get("dto");
        String text=null;
        String thumNail=null;
        boolean authSessionFlag=false;
        if(dto instanceof tryUpdateStoreDto){
            tryUpdateStoreDto tryUpdateStoreDto=(tryUpdateStoreDto)dto;
            text=tryUpdateStoreDto.getText();
            thumNail=tryUpdateStoreDto.getThumbNail();
            authSessionFlag=true;
        }else if(dto instanceof tryInsertStoreDto){
            tryInsertStoreDto insertStoreDto=(tryInsertStoreDto)dto;
            text=insertStoreDto.getText();
            thumNail=insertStoreDto.getThumbNail();
            authSessionFlag=true;
        }
        deleteImgs(text, thumNail, httpSession);
        if(authSessionFlag){
            removeAuthSession(httpSession);
        }
    }
    private void removeAuthSession(HttpSession httpSession) {
        httpSession.removeAttribute(senums.auth.get()+senums.phonet.get());
    }
    
    
    
    
}
