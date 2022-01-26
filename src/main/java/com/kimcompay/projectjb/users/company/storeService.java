package com.kimcompay.projectjb.users.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.model.storeDao;
import com.kimcompay.projectjb.users.company.model.storeVo;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.tryUpdateStoreDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    @Autowired
    private fileService fileService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    
    @Transactional(rollbackFor =  Exception.class)
    public JSONObject tryUpdate(tryUpdateStoreDto  tryUpdateStoreDto) {
        logger.info("tryUpdate");
        //수정요청 가게 정보 가져오기
        storeVo storeVo=storeDao.findById(tryUpdateStoreDto.getId()).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 상품입니다", "tryUpdate"));
        //주인이 맞는지 검사
        if(utillService.getLoginId()!=storeVo.getCid()){
            throw utillService.makeRuntimeEX("매장 주인이 아닙니다", "tryUpdate");
        }
        //시간이 변경되었나 검사
        String openTime=tryUpdateStoreDto.getOpenTime();
        String closeTime=tryUpdateStoreDto.getCloseTime();
        if(!openTime.equals(storeVo.getOpenTime())||!closeTime.equals(storeVo.getCloseTime())){
            logger.info("시간병경 요청");
            checkOpenAndCloseTime(openTime, closeTime);
            updateTime(storeVo, openTime, closeTime);
        }
        //
        int radius=tryUpdateStoreDto.getDeliverRadius();
        if(storeVo.getDeliverRadius()!=radius){
            logger.info("배달반경범위가 변경되었습니다");
            updateDeliverRadius(storeVo, radius);
        }
        //
        int minPrice=tryUpdateStoreDto.getMinPrice();
        if(storeVo.getMinPrice()!=minPrice){
            logger.info("배달 최소금액 변경");
            updateMinPrice(storeVo, minPrice);
        }
        //
        String text=tryUpdateStoreDto.getText();
        if(text!=storeVo.getText()){
            logger.info("가게설명이 변경되었습니다");
            updateStoreText(storeVo, text);
        }
        //
        String thumbNail=tryUpdateStoreDto.getThumbNail();
        if(!thumbNail.equals(storeVo.getSimg())){
            logger.info("썸네일이 변경되었습니다");
            updateThumbNail(storeVo, thumbNail);
        }
        //
        String postCode=tryUpdateStoreDto.getPostcode();
        String address=tryUpdateStoreDto.getAddress();
        String detailAddress=tryUpdateStoreDto.getDetailAddress();
        if(!postCode.equals(storeVo.getSpostcode())||!address.equals(storeVo.getSaddress())||!detailAddress.equals(storeVo.getSaddress())){
            logger.info("주소가 변경되었습니다");
            updateAddress(storeVo, address, postCode, detailAddress);
        }
        //
        String phone=tryUpdateStoreDto.getPhone();
        String tel=tryUpdateStoreDto.getTel();
        if(!tel.equals(storeVo.getStel())){
            logger.info("tel 변경되었습니다");
            updateTel(storeVo, tel);
        }
        if(!phone.equals(storeVo.getSphone())){
            logger.info("휴대폰번호가 변경되었습니다");
            //인증검증로직필요함
            checkAuth(phone);
            updatePhone(storeVo, phone);
        }
        String companyNum=tryUpdateStoreDto.getNum();
        if(!companyNum.equals(storeVo.getSnum())){
            logger.info("사업자 번호가 변경되었습니다");
            //사업자번호 검증로직필요
            checkCompayNum(companyNum);
            updateCompanyNum(storeVo, companyNum);
        }
        return utillService.getJson(false, "변경이 완료되었습니다");
    }
    private void updateCompanyNum(storeVo storeVo,String companyNum) {
        logger.info("updateCompanyNum");
        storeVo.setSnum(companyNum);
    }
    private void updatePhone(storeVo storeVo,String phone) {
        logger.info("updatePhone");
        storeVo.setSphone(phone);
    }
    private void updateTel(storeVo storeVo,String tel) {
        logger.info("updateTel");
        storeVo.setStel(tel);
    }
    private void updateAddress(storeVo storeVo,String address,String postCode,String detailAddress) {
        logger.info("updateAddress");
        storeVo.setSpostcode(postCode);
        storeVo.setSaddress(address);
        storeVo.setSdetail_address(detailAddress);
    }
    private void updateThumbNail(storeVo storeVo,String thumbNail) {
        logger.info("updateThumbNail");
        storeVo.setSimg(thumbNail);
    }
    private void updateStoreText(storeVo storeVo,String text) {
        logger.info("updateStoreText");
        storeVo.setText(text);
    }
    private void updateMinPrice(storeVo storeVo,int minPrice) {
        logger.info("updateMinPrice");
        storeVo.setMinPrice(minPrice);
    }
    private void updateDeliverRadius(storeVo storeVo,int radius) {
        logger.info("updateDeliverRadius");
        storeVo.setDeliverRadius(radius);
    }
    private void updateTime(storeVo storeVo,String openTime,String closeTime) {
        logger.info("updateTime");
        storeVo.setOpenTime(openTime);
        storeVo.setCloseTime(closeTime);
        logger.info("시간 변경완료");    
    }
    public String findDeliver(String loginId) {
        logger.info("findDeliver");
        logger.info("배달목록 찾는 상점:  "+loginId);
        return redisTemplate.opsForValue().get(loginId+"delivery");
    }
    public JSONObject getStore(int id) {
        logger.info("getStore");
        logger.info("조회 매장 고유번호: "+id);
        storeVo storeVo=storeDao.findBySid(id).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 매장입니다", "getStore"));
        logger.info("메징조회 정보: "+storeVo.toString());
        //매장 소유 회사 계정인지 검사
        if(storeVo.getCid()!=utillService.getLoginId()){
            throw utillService.makeRuntimeEX("매장 소유자의 계정이 아닙니다", "getStore");
        }
        //유저 고유번호 노출방지
        storeVo.setCid(0);
        return utillService.getJson(true, storeVo);
    }
    public JSONObject getStoresByEmail(String page,String keyword) {
        logger.info("getStoresByEmail");
        String email=utillService.getLoginInfor().get("email").toString();
        int requestPage=Integer.parseInt(page);
        List<Map<String,Object>>storeSelectInfor=getStoresInfor(email, keyword, requestPage);
        if(utillService.checkEmthy(storeSelectInfor)){
            if(utillService.checkBlank(keyword)){
                throw utillService.makeRuntimeEX("가게 목록이 없습니다 등록해주세요", "getStoresByEmail");
            }
            throw utillService.makeRuntimeEX("검색결과가 없습니다", "getStoresByEmail");
        }
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
    private List<Map<String,Object>> getStoresInfor(String email,String keyword,int requestPage) {
        logger.info("getStoresInfor");
        logger.info("검색키워드: "+keyword);
        if(utillService.checkBlank(keyword)){
            return storeDao.findByStoreNameNokeyword(email,email,utillService.getStart(requestPage, pageSize)-1,pageSize);
        }
        return storeDao.findByStoreInKeyword(email,keyword,email,keyword,utillService.getStart(requestPage, pageSize)-1,pageSize);
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
                    .minPrice(Integer.parseInt(tryInsertStoreDto.getMinPrice())).deliverRadius(Integer.parseInt(tryInsertStoreDto.getDeliverRadius()))
                    .cid(utillService.getLoginId()).semail(utillService.getLoginInfor().get("email").toString()).ssleep(0).stel(tryInsertStoreDto.getTel()).text(tryInsertStoreDto.getText()).build();
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
        //인증 세션 비우기
        HttpSession httpSession=utillService.getHttpServletRequest().getSession();
        httpSession.removeAttribute(senums.auth.get()+senums.phonet.get());
        //사용하지 않는 사진 지우기
        //이미지가 있다면 path제거후 이름만 얻어내기
        List<String>usingImgs=utillService.getOnlyImgNames(tryInsertStoreDto.getText());
        usingImgs.add(tryInsertStoreDto.getThumbNail().split("/")[4]);
        fileService.deleteFile(httpSession,usingImgs);
        httpSession.removeAttribute(senums.imgSessionName.get());
    }
    private void checkValues(tryInsertStoreDto tryInsertStoreDto) {
        logger.info("checkValues");
        //사업자등록번호검사
        checkCompayNum(tryInsertStoreDto.getNum());
        //매장오픈/마감시간검사
        checkOpenAndCloseTime(tryInsertStoreDto.getOpenTime(),tryInsertStoreDto.getCloseTime());
        //같은매장이있는지 검사
        checkSameStore(tryInsertStoreDto.getStoreName(),tryInsertStoreDto.getNum(),tryInsertStoreDto.getAddress());
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
    private void checkSameStore(String storeName,String companyNum,String address) {
        logger.info("checkSameStore");
        if(storeDao.countBySnameAndAddress(storeName,companyNum, address)!=0){
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
    private void checkOpenAndCloseTime(String openTime,String closeTime) {
        logger.info("checkOpenAndCloseTime");
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
