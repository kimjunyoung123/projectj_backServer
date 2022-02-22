package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.company.model.products.tryProductInsertDto;
import com.kimcompay.projectjb.users.company.model.stores.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.stores.tryUpdateStoreDto;
import com.kimcompay.projectjb.users.company.service.storeService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class aopService {
    private Logger logger=LoggerFactory.getLogger(aopService.class); 
    @Autowired
    private fileService fileService;
    @Autowired
    private storeService storeService;
    @Value("${aws.bucket.name}")
    private String bucketName;

    private HttpSession httpSession;
    
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
    +"||execution(* com.kimcompay.projectjb.board.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.google.ocrService.*(..))")
    public void writeLog(JoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("pakage: "+signature.getDeclaringTypeName());
        logger.info("method: "+method.getName());
        logger.info("요청변수: "+utillService.arrToLogString(signature.getParameterNames())+" 요청값: "+utillService.arrToLogString(joinPoint.getArgs()));
    }
    //-----------------------------------------------------------------------------------------------------
    //매장정보 접근전 주인인지 확인
    @Before("execution(* com.kimcompay.projectjb.users.company.service.productService.getProductAndEvents(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDeliverAddress(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.getFlyers(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDelivers(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.updateProductController(..))")
    public void checkOwner(JoinPoint joinPoint) {
        logger.info("checkOwner");
        storeService.checkExist(Integer.parseInt(utillService.getHttpServletRequest().getParameter("storeId")));
    }
    //----------------------------------------------------------------------------------------------------
    private void deleteImgs(String text,String thumNail,HttpSession httpSession) {
        logger.info("deleteImgs");
        List<String>usingImgs=utillService.getOnlyImgNames(text);
        usingImgs.add(utillService.getImgNameInPath(thumNail, 4));
        logger.info("삭제방지 배열만듬");
        fileService.deleteFile(httpSession,usingImgs);
        httpSession.removeAttribute(senums.imgSessionName.get());
    }
    //update insert 전 이전 까지 사용했던 세션 가져오기 컨트롤러에서 낚아챔
    @Async
    @Before(value = "execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.storeUpdate(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.storeInsert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.uploadAndOcr(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.insertFlyerAndProducts(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.updateProductController(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertFlyer(..))"///전단 업로드 역시 추가해야함  
    )
    public void setHttpSession(JoinPoint joinPoint) {
        logger.info("setHttpSession");
        for (Object obj : joinPoint.getArgs()) {
            if (obj instanceof HttpServletRequest ) {
                logger.info("find session");
                HttpServletRequest request = (HttpServletRequest) obj;
                this.httpSession=request.getSession();
                break;
            }else if(obj instanceof HttpSession){
                logger.info("find session");
                this.httpSession=(HttpSession) obj;
                break;
            }
        } 
    }
    //update insert 후 사용 하지 않는 사진들 클라우드 제거 
    @Async
    @AfterReturning(value = "execution(* com.kimcompay.projectjb.users.company.service.storeService.insert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.storeService.tryUpdate(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.productService.insert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.productService.tryUpdate(..))"
    ,returning="response")
    public void doneInserOrUpdate(JoinPoint joinPoint,Object response) {
        logger.info("doneInserOrUpdate");
        logger.info("response: "+response);
        Object[] values=joinPoint.getArgs();
        logger.info("dto: "+utillService.arrToLogString(values));
        for(Object dto:values){
            if(dto instanceof tryUpdateStoreDto){
                tryUpdateStoreDto tryUpdateStoreDto=(tryUpdateStoreDto)dto;
                deleteImgs(tryUpdateStoreDto.getText(), tryUpdateStoreDto.getThumbNail(), this.httpSession);
                break;
            }else if(dto instanceof tryInsertStoreDto){
                tryInsertStoreDto insertStoreDto=(tryInsertStoreDto)dto;
                deleteImgs(insertStoreDto.getText(), insertStoreDto.getThumbNail(), this.httpSession);
                break;
            }else if(dto instanceof tryProductInsertDto){
                tryProductInsertDto tryProductInsertDto=(tryProductInsertDto)dto;
                deleteImgs(tryProductInsertDto.getText(), tryProductInsertDto.getProductImgPath(), this.httpSession);
                break;
            }else {
                continue;
            }   
        }    
    }
    //-------------------------------------------------------------
    //인증 서비스 이후 인증 정보 지워주기 
    @Async
    @AfterReturning(value = "execution(* com.kimcompay.projectjb.users.company.service.storeService.insert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.storeService.tryUpdate(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.userService.insert(..))"
    ,returning="response")
    public void removeAuthSession(JoinPoint JoinPoint,Object response) {
        logger.info("removeAuthSession");
        logger.info("response: "+response);
        httpSession.removeAttribute(senums.auth.get()+senums.phonet.get());
        httpSession.removeAttribute(senums.auth.get()+senums.emailt.get());
    }
    
    
    
    
}
