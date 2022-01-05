package com.kimcompay.projectjb.users.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.model.storeDao;
import com.kimcompay.projectjb.users.company.model.storeVo;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class storeService {
    private Logger logger=LoggerFactory.getLogger(storeService.class);
    private int pageSize=2;
    @Autowired
    private storeDao storeDao;
    @Autowired
    private sqsService sqsService;
    @Autowired
    private kakaoMapService kakaoMapService;
    
    public JSONObject getStoresByEmail(String page) {
        logger.info("getStoresByEmail");
        Object email=utillService.getLoginInfor().get("email");
        int requestPage=Integer.parseInt(page);
        List<Map<String,Object>>storeSelectInfor=storeDao.findByEmail(email,email,utillService.getStart(requestPage, pageSize)-1,pageSize);
        int totalPage=utillService.getTotalPage(Integer.parseInt(storeSelectInfor.get(0).get("totalCount").toString()),pageSize);
        if(utillService.checkEmthy(storeSelectInfor)){
            logger.info("스토어 결과 미존재");
            if(totalPage<requestPage){
                logger.info("페이지 범위 초과");
                throw utillService.makeRuntimeEX("요청페이지가 전체페이지를 초과합니다", "getStoresByEmail");
            }
            throw utillService.makeRuntimeEX("매장이 존재하지 않습니다", "getStoresByEmail");
        }
        JSONObject reponse=utillService.getJson(true, storeSelectInfor);
        reponse.put("totalPage", totalPage);
        return utillService.getJson(true, reponse);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryInsertStoreDto tryInsertStoreDto){
        logger.info("insert");
        logger.info("요청정보: "+tryInsertStoreDto);
        //휴대폰 인증 검증
        checkAuth(tryInsertStoreDto.getPhone());
        //값 검증
        checkValues(tryInsertStoreDto);
        //저장시도
        tryInsert(tryInsertStoreDto);
        //결과전송
        try {
            doneInsert(tryInsertStoreDto);
        } catch (Exception e) {
            logger.info("등록 되었으므로 예외무시");
        }
        return utillService.getJson(true, "매장등록이 완료되었습니다");
    }
    private void checkAuth(String phone) {
        logger.info("checkAuth");
        String sPhone=utillService.checkAuthPhone("auth2");
        if(!phone.trim().equals(sPhone.trim())){
            throw utillService.makeRuntimeEX("인증받은 전화번화 일치하지 않습니다", "checkAuth");
        }
        logger.info("휴대폰 유효성검사 통과");
    }
    private void tryInsert(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("dtoToVo");
        storeVo vo=storeVo.builder().closeTime(tryInsertStoreDto.getCloseTime()).openTime(tryInsertStoreDto.getOpenTime())
                    .saddress(tryInsertStoreDto.getAddress()).sdetail_address(tryInsertStoreDto.getDetailAddress()).simg(tryInsertStoreDto.getThumbNail())
                    .sname(tryInsertStoreDto.getStoreName()).snum(tryInsertStoreDto.getNum()).sphone(tryInsertStoreDto.getPhone()).spostcode(tryInsertStoreDto.getPostcode())
                    .semail(utillService.getLoginInfor().get("email").toString()).ssleep(0).stel(tryInsertStoreDto.getTel()).text(tryInsertStoreDto.getText()).build();
                    storeDao.save(vo);
                    
    }
    @Async
    public void doneInsert(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("doneInsert");
        String insertMessage="매장등록을 해주셔서 진심으로 감사합니다 \n 매장이름: "+tryInsertStoreDto.getStoreName()+"\n매장위치: "+tryInsertStoreDto.getAddress();
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<Object,Object>map=principalDetails.getPrinci();
        sqsService.sendEmailAsync(insertMessage,principalDetails.getUsername());
        sqsService.sendPhoneAsync(insertMessage, map.get("phone").toString());
        sqsService.sendPhoneAsync(insertMessage, tryInsertStoreDto.getPhone());
        throw new RuntimeException();
    }
    private void checkValues(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("checkValues");
        //사업자등록번호검사
        checkCompayNum(tryInsertStoreDto.getNum());
        //매장오픈/마감시간검사
        checkOpenAndCloseTime(tryInsertStoreDto);
        //같은매장이있는지 검사
        checkSameStore(tryInsertStoreDto);
        //일반/휴대전화검사
        checkPhoneAndTel(tryInsertStoreDto.getPhone(),tryInsertStoreDto.getTel());
        //주소검사
        kakaoMapService.checkAddress(tryInsertStoreDto.getAddress());
        logger.info("매장등록 유효성검사 통과");
    }
    private void checkPhoneAndTel(String phone ,String tel) {
        logger.info("checkPhoneAndTel");
        if(utillService.checkOnlyNum(tel)||utillService.checkOnlyNum(phone)){
            throw utillService.makeRuntimeEX("전화번호 혹은 휴대폰번호는 숫자만 가능합니다", "checkValues");
        }
        logger.info("휴대/일반전화 유효성검사 통과");
    }
    private void checkSameStore(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("checkSameStore");
        if(storeDao.countBySnameAndAddress(tryInsertStoreDto.getStoreName(),tryInsertStoreDto.getNum(), tryInsertStoreDto.getAddress())!=0){
            logger.info("같은위치에서 같은사업자번호로 이미 등록된 매장발견");
            throw utillService.makeRuntimeEX("같은위치에서 같은사업자번호로 이미 등록된 매장발견", "checkSameStore");
        }
    }
    private void checkCompayNum(String snum) {
        logger.info("checkCompayNum");
        int count=0;
        try {
            logger.info(snum);
            count=storeDao.countBySnum(Long.parseLong(snum));
        } catch (NumberFormatException e) {
            logger.info("사업자등록번호중 문자발견");
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 입력해주세요 ", "checkCompayNum");
        }
        //같은 사업자 번호로 회원 가입한 회사가 있어야함
        if(count==0){
            logger.info("사업자 번호로 회원가입 한 회사가없음");
            throw utillService.makeRuntimeEX("사업자 번호로 회원가입한 기업이 없습니다", "checkCompayNum");
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
        } catch (NumberFormatException e) {
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
            logger.info("마감시간이 오픈시간보다 빠름");
            throw utillService.makeRuntimeEX("마감시간이 오픈시간보다 빠를 수없습니다", "checkTime");
        }
        logger.info("시간 유효성검사 통과");
    }
}
