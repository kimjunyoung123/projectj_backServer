package com.kimcompay.projectjb.aops;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.basket.basketVo;
import com.kimcompay.projectjb.users.company.model.flyers.tryInsertFlyerDto;
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
    @Autowired
    private basketDao basketDao;

    private HttpSession httpSession;
    
    //@Async //흐름 파악위해 비동기 해제
    @Before("execution(* com.kimcompay.projectjb.apis.aws.services.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.jungbu.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.kakao.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.naver.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.sns.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.requestTo.*(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.jwt.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.userService.*(..))"
    +"||execution(* com.kimcompay.projectjb.users.user.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.utillService.*(..))"
    +"||execution(* com.kimcompay.projectjb.board.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.google.ocrService.*(..))"
    +"||execution(* com.kimcompay.projectjb.payments.service.*.*(..))"
    +"||execution(* com.kimcompay.projectjb.apis.settle.*.*(..))")
    public void writeLog(JoinPoint joinPoint) throws Throwable {
        logger.info("writeLog");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method=signature.getMethod();
        logger.info("pakage: "+signature.getDeclaringTypeName());
        logger.info("method: "+method.getName());
        logger.info("요청변수: "+utillService.arrToLogString(signature.getParameterNames())+" 요청값: "+utillService.arrToLogString(joinPoint.getArgs()));
    }
    //-----------------------------------------------------------------------------------------------------
    //매장정보 접근전 주인인지 확인 select
    @Before("execution(* com.kimcompay.projectjb.users.company.service.productService.getProductAndEvents(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDeliverAddress(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.getFlyers(..))"
    +"||execution(* com.kimcompay.projectjb.delivery.service.deliveryService.getDelivers(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.getFlyerAndProducts(..))")
    public void checkOwnerParam(JoinPoint joinPoint) {
        logger.info("checkOwnerParam");
        storeService.checkExist(Integer.parseInt(utillService.getHttpServletRequest().getParameter("storeId")));
    }
    //update,insert,delete
    @Before("execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertFlyer(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertProduct(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.deleteActionHub(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertFlyer(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.updateProductController(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryUpdateFlyer(..))")
    public void checkOwnerPath(JoinPoint joinPoint) {
        logger.info("checkOwnerPath");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String[] paramNames=signature.getParameterNames();
        Object[] values=joinPoint.getArgs();
        for(int i=0;i<paramNames.length;i++){
            if(paramNames[i].equals("storeId")){
                storeService.checkExist(Integer.parseInt(values[i].toString()));
                break;
            }
        }
    }
    //----------------------------------------------------------------------------------------------------
    private void deleteImgs(String text,String thumNail) {
        logger.info("deleteImgs");
        List<String>usingImgs=utillService.getOnlyImgNames(text);
        usingImgs.add(utillService.getImgNameInPath(thumNail, 4));
        logger.info("삭제방지 배열만듬");
        deleteImgsCore(usingImgs);
    }
    private void deleteImgsCore(List<String>usingImgs) {
        fileService.deleteFile(this.httpSession,usingImgs);
        this.httpSession.removeAttribute(senums.imgSessionName.get());
    }
    //update insert 전 이전 까지 사용했던 세션 가져오기 컨트롤러에서 낚아챔
    @Async
    @Before(value = "execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.storeUpdate(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.storeInsert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.insertFlyerAndProducts(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.updateProductController(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertFlyer(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryInsertProduct(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.compayAuthRestController.tryUpdateFlyer(..))")
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
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.insert(..))"
    +"||execution(* com.kimcompay.projectjb.users.company.service.flyerService.tryUpdate(..))"
    ,returning="response")
    public void doneInserOrUpdate(JoinPoint joinPoint,Object response) {
        logger.info("doneInserOrUpdate");
        logger.info("response: "+response);
        Object[] values=joinPoint.getArgs();
        logger.info("dto: "+utillService.arrToLogString(values));
        for(Object dto:values){
            if(dto instanceof tryUpdateStoreDto){
                tryUpdateStoreDto tryUpdateStoreDto=(tryUpdateStoreDto)dto;
                deleteImgs(tryUpdateStoreDto.getText(), tryUpdateStoreDto.getThumbNail());
                break;
            }else if(dto instanceof tryInsertStoreDto){
                tryInsertStoreDto insertStoreDto=(tryInsertStoreDto)dto;
                deleteImgs(insertStoreDto.getText(), insertStoreDto.getThumbNail());
                break;
            }else if(dto instanceof tryProductInsertDto){
                tryProductInsertDto tryProductInsertDto=(tryProductInsertDto)dto;
                deleteImgs(tryProductInsertDto.getText(), tryProductInsertDto.getProductImgPath());
                break;
            }else if(dto instanceof tryInsertFlyerDto){
                tryInsertFlyerDto tryInsertFlyerDto=(tryInsertFlyerDto)dto;
                deleteImgs(tryInsertFlyerDto.getFlyerImgs());
            }else {
                continue;
            }   
        }    
    }
    private void deleteImgs(List<String>imgs) {
        for(int i=0;i<imgs.size();i++){
            if(Optional.ofNullable(imgs.get(i)).orElseGet(()->null)==null){
                continue;
            }
            imgs.set(i, utillService.getImgNameInPath(imgs.get(i),4));
        }
        logger.info("삭제방지배열생성");
        deleteImgsCore(imgs);
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
    //-------------------------------------------------------------------------
    ///장바구니 접근전 주인 확인  
    @Before("execution(* com.kimcompay.projectjb.payments.service.basketService.tryDelete(..))")
    public void checkOwner(JoinPoint joinPoint) {
        logger.info("checkOwner");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        int len=signature.getParameterNames().length;
        String[] names=signature.getParameterNames();
        Object[] values=joinPoint.getArgs();
        for(int i=0;i<len;i++){
            if(names[i].equals("basketId")){
                basketVo basketVo=basketDao.findById(Integer.parseInt(values[i].toString())).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 장바구니 제품입니다", "checkOwner"));
                utillService.checkOwner(basketVo.getUserId(),"본인 소유의 제품이 아닙니다");
                break;
            }
        }
    }
    
    
    
    
}
