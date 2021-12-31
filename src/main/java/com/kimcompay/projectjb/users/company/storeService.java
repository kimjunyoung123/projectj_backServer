package com.kimcompay.projectjb.users.company;

import java.util.ArrayList;
import java.util.List;


import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class storeService {
    private Logger logger=LoggerFactory.getLogger(storeService.class);

    @Autowired
    private storeDao storeDao;

    public void actionHub(tryInsertStoreDto tryInsertStoreDto,String action) {
        logger.info("actionHub");
        logger.info("요청행위: "+action);
        logger.info("요청정보: "+tryInsertStoreDto);
        if(action.equals(senums.insert.get())){
            logger.info("매장등록 요청");
            insert(tryInsertStoreDto);
        }else if(action.equals(senums.update.get())){
            logger.info("매장 수정 요청");
        }
        throw utillService.makeRuntimeEX(senums.defaultMessage2.get(),"actionHub");
    }
    @Transactional(rollbackFor = Exception.class)
    private JSONObject insert(tryInsertStoreDto tryInsertStoreDto){
        logger.info("insert");
        //값 검증
        checkValues(tryInsertStoreDto);
        return null;
    }
    private storeVo checkValues(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("checkValues");
        try {
        
        } catch (NullPointerException e) {
           throw utillService.makeRuntimeEX("빈칸이 존재 합니다", "checkValues");
        }
        //사업자등록번호검사
       // checkSnum(vo.getSnum());
       // checkOpenAndCloseTime(vo);
        /*if(utillService.checkOnlyNum(vo.getStel())||utillService.checkOnlyNum(vo.getSphone())){
            throw utillService.makeRuntimeEX("전화번호 혹은 휴대폰번호는 숫자만 가능합니다", "checkValues");
        }*/
        return null;
    }
    private void checkSnum(String snum) {
        logger.info("checkSnum");
        int count=0;
        try {
            count=storeDao.countBySnum(Integer.parseInt(snum));
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 입력해주세요 ", "checkSnum");
        }
        //같은 사업자 번호로 회원 가입한 회사가 있어야함
        if(count==0){
            logger.info("사업자 번호로 회원가입 한 회사가없음");
            throw utillService.makeRuntimeEX("사업자 번호로 회원가입 한 기업이 없습니다", "checkSnum");
        }
        //사업자 번호 검사는 일단 회사로 회원가입 후에 하는 시스템이므로 여기서 안해줘도 된다
        logger.info("사업자 번호 유효성 검사 통과");
    }
    private void checkOpenAndCloseTime(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("checkOpenAndCloseTime");
        //요청시간 꺼내기
        String openTime=tryInsertStoreDto.getOpenTime();
        String closeTime=tryInsertStoreDto.getCloseTime();
        logger.info("시작시간: "+openTime);
        logger.info("종료시간: "+closeTime);
        //시간분리
        List<Integer>times=new ArrayList<>();
        try {
            for(String s:openTime.split(":")){
                times.add(Integer.parseInt(s));
            }
            for(String s:closeTime.split(":")){
                times.add(Integer.parseInt(s));
            } 
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("시간값이 잘못되었습니다", "checkOpenAndCloseTime");
        }
        //음수가 있는지 검사
        for(int i:times){
            logger.info("시/분: "+i);
            if(i<0){
                throw utillService.makeRuntimeEX("시간은 0보다 작을수 없습니다", "checkTime");
            }
        }
        //시작시간보다 종료시간이 빠른지 검사
        if(times.get(0)>times.get(2)||(times.get(0)==times.get(2)&&times.get(1)>=times.get(3))){
            throw utillService.makeRuntimeEX("종료시간이 시작시간보다 빠를 수없습니다", "checkTime");
        }
        logger.info("시간 유효성검사 통과");
    }
}
