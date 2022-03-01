package com.kimcompay.projectjb.users.company.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.board.service.boardService;
import com.kimcompay.projectjb.enums.intenums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.model.stores.storeDao;
import com.kimcompay.projectjb.users.company.model.stores.storeReviewsDao;
import com.kimcompay.projectjb.users.company.model.stores.storeVo;
import com.kimcompay.projectjb.users.company.model.stores.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.stores.tryUpdateStoreDto;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class storeService {
    private final int  pageSize=2;
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
    @Autowired
    private boardService boardService;
    @Autowired
    private storeReviewsDao storeReviewsDao;

    public storeVo getVo(int storeId) {
        return storeDao.findById(storeId).orElseThrow(()->utillService.makeRuntimeEX("존재하지않는 매장입니다", "getVo"));
    }
    public JSONObject getReviews(int storeId,int page) {
        List<Map<String,Object>>reviews=storeReviewsDao.findByStoreIdLimit(storeId,storeId, utillService.getStart(page, pageSize)-1, pageSize);
        if(reviews.isEmpty()){
            throw utillService.makeRuntimeEX("불러오는데 실패했습니다", "getReviews");
        }
        JSONObject response=new JSONObject();
        response.put("reviews", reviews);
        response.put("totalPage", utillService.getTotalPage(Integer.parseInt(reviews.get(0).get("totalCount").toString()), pageSize));
        return utillService.getJson(true, response);
    }
    public JSONObject getStore(String address,String storeName,int page) {
        List<Map<String,Object>> storeAndReviews=storeDao.findJoinReviewsSaddressAndSname(address.trim(), storeName.trim(),utillService.getStart(page, pageSize)-1,pageSize);
        if(storeAndReviews.isEmpty()){
            throw utillService.makeRuntimeEX("등록되지 않는 매장입니다", "getStore");
        }
        //유효성검사
        LocalDateTime now=LocalDateTime.now();
        String snow=now.toString();
        String openTime=storeAndReviews.get(0).get("open_time").toString();
        String closeTime=storeAndReviews.get(0).get("close_time").toString();
        Timestamp tOpenTime=Timestamp.valueOf(snow.split("T")[0]+" "+openTime+":00");
        Timestamp tCloseTime=Timestamp.valueOf(snow.split("T")[0]+" "+closeTime+":00");
        if(Integer.parseInt(storeAndReviews.get(0).get("store_sleep").toString())==1){
            throw utillService.makeRuntimeEX("영업을 준비중인 매장입니다", "getStore");
        }else if(now.isBefore(tOpenTime.toLocalDateTime())||now.isAfter(tCloseTime.toLocalDateTime())){
            throw utillService.makeRuntimeEX("영업시간이 아닙니다", "getStore");
        }
        //페이지 검사
        int totalPage=utillService.getTotalPage(Integer.parseInt(storeAndReviews.get(0).get("totalCount").toString()), pageSize);
        if(totalPage<page){
            throw utillService.makeRuntimeEX("전체페이지보다 요청페이지가 큽니다", "getStore");
        }
        //매장 정보 꺼내기
        JSONObject response=new JSONObject();
        response.put("totalPage", totalPage);
        response.put("storeName", storeAndReviews.get(0).get("store_name"));
        response.put("storeAddress", storeAndReviews.get(0).get("store_address"));
        response.put("storeRoadAddress", storeAndReviews.get(0).get("store_road_address"));
        response.put("storeDetailAddress", storeAndReviews.get(0).get("store_detail_address"));
        response.put("storeNum", storeAndReviews.get(0).get("store_name"));
        response.put("storePhone", storeAndReviews.get(0).get("store_phone"));
        response.put("storeTel", storeAndReviews.get(0).get("store_tel"));
        response.put("openTime",openTime);
        response.put("closeTime", closeTime);
        response.put("thumbNail", storeAndReviews.get(0).get("thumb_nail"));        
        response.put("deliverRadius", storeAndReviews.get(0).get("deliver_radius"));
        response.put("minPrice", storeAndReviews.get(0).get("min_price"));
        response.put("text", storeAndReviews.get(0).get("store_text"));
        response.put("id", storeAndReviews.get(0).get("store_id"));
        //리뷰 꺼내기
        Boolean flag=false;
        List<JSONObject>reviews=new ArrayList<>();
        for(Map<String,Object>storeAndReview:storeAndReviews){
            if(Optional.ofNullable(storeAndReview.get("store_review_id")).orElseGet(()->null)==null){
                break;
            }else{
                flag=true;
                JSONObject review=new JSONObject();
                review.put("text", storeAndReview.get("store_review_text"));
                review.put("id", storeAndReview.get("store_review_id"));
                review.put("created", storeAndReview.get("store_review_created"));
                review.put("writer", storeAndReview.get("store_review_writer"));
                reviews.add(review);
            }
        }
        if(flag){
            response.put("reviews", reviews);
        }
        response.put("reviewFlag", flag);
        return utillService.getJson(true, response);
    }
    public void checkExist(int storeId) {
        storeVo storeVo=getStoreCore(storeId);
        utillService.checkOwner(storeVo.getCid(), "본인 소유의 매장이 아닙니다");
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject updateSleepOrOpen(int flag,int storeId) {
        storeVo storeVo=storeDao.findById(storeId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 매장입니다", "storeSleepOrOpen"));
        utillService.checkOwner(storeVo.getCid(), "회사소유의 매장이 아닙니다");
        storeVo.setSsleep(flag);
        return utillService.getJson(true, "엽업상태가 변경되었습니다"); 
    }
    @Transactional(rollbackFor =  Exception.class)
    public JSONObject tryUpdate(tryUpdateStoreDto  tryUpdateStoreDto) {
        //수정요청 가게 정보 가져오기
        storeVo storeVo=storeDao.findById(tryUpdateStoreDto.getId()).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 상품입니다", "tryUpdate"));
        boolean updateFlag=false;
        //주인이 맞는지 검사
        utillService.checkOwner(storeVo.getCid(),"매장 소유자의 계정이 아닙니다");
        //매장이름
        String storeName=tryUpdateStoreDto.getStoreName();
        if(!storeName.equals(tryUpdateStoreDto.getStoreName())){
            utillService.writeLog("매장이름변경요청",storeService.class);
            updateFlag=true;
            updateStoreName(storeVo, storeName);
        }
        //시간이 변경되었나 검사
        String openTime=tryUpdateStoreDto.getOpenTime();
        String closeTime=tryUpdateStoreDto.getCloseTime();
        if(!openTime.equals(storeVo.getOpenTime())||!closeTime.equals(storeVo.getCloseTime())){
            utillService.writeLog("시간병경 요청",storeService.class);
            updateFlag=true;
            checkOpenAndCloseTime(openTime, closeTime);
            updateTime(storeVo, openTime, closeTime);
        }
        //배달반경
        int radius=tryUpdateStoreDto.getDeliverRadius();
        if(storeVo.getDeliverRadius()!=radius){
            utillService.writeLog("배달반경범위가 변경되었습니다",storeService.class);
            updateFlag=true;
            updateDeliverRadius(storeVo, radius);
        }
        //최소배달금액
        int minPrice=tryUpdateStoreDto.getMinPrice();
        if(storeVo.getMinPrice()!=minPrice){
            utillService.writeLog("배달 최소금액 변경",storeService.class);
            updateFlag=true;
            updateMinPrice(storeVo, minPrice);
        }
        //가게설명
        String text=tryUpdateStoreDto.getText();
        String originText=storeVo.getText();
        if(!text.equals(originText)){
            utillService.writeLog("가게설명이 변경되었습니다",storeService.class);
            updateFlag=true;
            updateStoreText(storeVo, text);
            boardService.deleteOriginImgInText(text, originText);
        }
        //썸네일
        String thumbNail=tryUpdateStoreDto.getThumbNail();
        String oringImgPath=storeVo.getSimg();
        if(!thumbNail.equals(oringImgPath)){
            utillService.writeLog("썸네일이 변경되었습니다",storeService.class);
            updateFlag=true;
            updateThumbNail(storeVo, thumbNail);
            fileService.deleteFile(utillService.getImgNameInPath(oringImgPath, 4) );
        }
        //주소
        String postCode=tryUpdateStoreDto.getPostcode();
        String address=tryUpdateStoreDto.getAddress();
        String detailAddress=tryUpdateStoreDto.getDetailAddress();
        if(!postCode.equals(storeVo.getSpostcode())||!address.equals(storeVo.getSaddress())||!detailAddress.equals(storeVo.getSdetail_address())){
            utillService.writeLog("주소가 변경되었습니다",storeService.class);
            updateFlag=true;
            updateAddress(storeVo, address, postCode, detailAddress,tryUpdateStoreDto.getRoadAddress());
        }
        //휴대폰/일반전화
        String phone=tryUpdateStoreDto.getPhone();
        String tel=tryUpdateStoreDto.getTel();
        if(!tel.equals(storeVo.getStel())){
            utillService.writeLog("전화번호가 변경되었습니다",storeService.class);
            updateFlag=true;
            updateTel(storeVo, tel);
        }
        if(!phone.equals(storeVo.getSphone())){
            utillService.writeLog("휴대폰번호가 변경되었습니다",storeService.class);
            updateFlag=true;
            //인증검증로직필요함
            checkAuth(phone);
            updatePhone(storeVo, phone);
        }
        //사업자번호
        String companyNum=tryUpdateStoreDto.getNum();
        if(!companyNum.equals(storeVo.getSnum())){
            utillService.writeLog("사업자 번호가 변경되었습니다",storeService.class);
            updateFlag=true;
            //사업자번호 검증로직필요
            checkCompayNum(companyNum);
            updateCompanyNum(storeVo, companyNum);
        }
        //가게정보가 수정되면 알림메세지/이메일전송
        if(updateFlag){
            doneInsertOrUpdate(tryUpdateStoreDto.getPhone(), "가게정보가 수정되었습니다");
            return utillService.getJson(true, "변경이 완료되었습니다");
        }else{
            return utillService.getJson(false, "변경사항이 없습니다");
        }
    }
    private void updateStoreName(storeVo storeVo,String storeName) {
        storeVo.setSname(storeName);
    }
    private void updateCompanyNum(storeVo storeVo,String companyNum) {
        storeVo.setSnum(companyNum);
    }
    private void updatePhone(storeVo storeVo,String phone) {
        storeVo.setSphone(phone);
    }
    private void updateTel(storeVo storeVo,String tel) {
        storeVo.setStel(tel);
    }
    private void updateAddress(storeVo storeVo,String address,String postCode,String detailAddress,String roadAddress) {
        storeVo.setSpostcode(postCode);
        storeVo.setSaddress(address);
        storeVo.setSdetail_address(detailAddress);
        storeVo.setRoadAddress(roadAddress);
    }
    private void updateThumbNail(storeVo storeVo,String thumbNail) {
        storeVo.setSimg(thumbNail);
    }
    private void updateStoreText(storeVo storeVo,String text) {
        storeVo.setText(text);
    }
    private void updateMinPrice(storeVo storeVo,int minPrice) {
        storeVo.setMinPrice(minPrice);
    }
    private void updateDeliverRadius(storeVo storeVo,int radius) {
        storeVo.setDeliverRadius(radius);
    }
    private void updateTime(storeVo storeVo,String openTime,String closeTime) {
        storeVo.setOpenTime(openTime);
        storeVo.setCloseTime(closeTime);
    }
    public String findDeliver(String loginId) {
        return redisTemplate.opsForValue().get(loginId+"delivery");
    }
    public JSONObject getStore(int id) {
        storeVo storeVo=getStoreCore(id);
        //매장 소유 회사 계정인지 검사
        utillService.checkOwner(storeVo.getCid(),"매장 소유자의 계정이 아닙니다");
        return utillService.getJson(true, storeVo);
    }
    private storeVo getStoreCore(int id) {
        return storeDao.findById(id).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 매장입니다", "getStore"));
    }
    public JSONObject getStores(int page,String keyword) {
        int requestPage=page;
        List<Map<String,Object>>storeSelectInfor=getStoresInfor(utillService.getLoginId(), keyword, requestPage);
        if(utillService.checkEmthy(storeSelectInfor)){
            if(utillService.checkBlank(keyword)){
                throw utillService.makeRuntimeEX("가게 목록이 없습니다 등록해주세요", "getStoresByEmail");
            }
            throw utillService.makeRuntimeEX("검색결과가 없습니다", "getStoresByEmail");
        }
        int totalPage=utillService.getTotalPage(Integer.parseInt(storeSelectInfor.get(0).get("totalCount").toString()),pageSize);
        if(utillService.checkEmthy(storeSelectInfor)){
            if(totalPage<requestPage){
                throw utillService.makeRuntimeEX("요청페이지가 전체페이지를 초과합니다", "getStoresByEmail");
            }
            throw utillService.makeRuntimeEX("매장이 존재하지 않습니다", "getStoresByEmail");
        }
        JSONObject reponse=utillService.getJson(true, storeSelectInfor);
        reponse.put("totalPage", totalPage);
        return utillService.getJson(true, reponse);
    }
    private List<Map<String,Object>> getStoresInfor(int cid,String keyword,int requestPage) {
        if(utillService.checkBlank(keyword)){
            return storeDao.findByStoreNameNokeyword(cid,cid,utillService.getStart(requestPage, pageSize)-1,pageSize);
        }
        return storeDao.findByStoreInKeyword(cid,keyword,cid,keyword,utillService.getStart(requestPage, pageSize)-1,pageSize);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryInsertStoreDto tryInsertStoreDto){
        //휴대폰 인증 검증
        checkAuth(tryInsertStoreDto.getPhone());
        //값 검증
        checkValues(tryInsertStoreDto);
        //저장시도
        tryInsert(tryInsertStoreDto);
        //결과전송
        try {
            String insertMessage="매장등록을 해주셔서 진심으로 감사합니다 \n 매장이름: "+tryInsertStoreDto.getStoreName()+"\n매장위치: "+tryInsertStoreDto.getAddress();
            doneInsertOrUpdate(tryInsertStoreDto.getPhone(),insertMessage);
        } catch (Exception e) {
            utillService.writeLog("등록되었으므로 예외무시",storeService.class);
        }
        return utillService.getJson(true, "매장등록이 완료되었습니다");
    }
    private void checkAuth(String phone) {
        String sPhone=utillService.checkAuthPhone("auth2");
        if(!phone.trim().equals(sPhone.trim())){
            throw utillService.makeRuntimeEX("인증받은 전화번화 일치하지 않습니다", "checkAuth");
        }
    }
    private void tryInsert(tryInsertStoreDto tryInsertStoreDto) {
        storeVo vo=storeVo.builder().closeTime(tryInsertStoreDto.getCloseTime()).openTime(tryInsertStoreDto.getOpenTime())
                    .saddress(tryInsertStoreDto.getAddress()).sdetail_address(tryInsertStoreDto.getDetailAddress()).simg(tryInsertStoreDto.getThumbNail())
                    .sname(tryInsertStoreDto.getStoreName()).snum(tryInsertStoreDto.getNum()).sphone(tryInsertStoreDto.getPhone()).spostcode(tryInsertStoreDto.getPostcode())
                    .minPrice(Integer.parseInt(tryInsertStoreDto.getMinPrice())).deliverRadius(Integer.parseInt(tryInsertStoreDto.getDeliverRadius()))
                    .cid(utillService.getLoginId()).ssleep(intenums.NOT_FLAG.get()).stel(tryInsertStoreDto.getTel()).text(tryInsertStoreDto.getText())
                    .roadAddress(tryInsertStoreDto.getRoadAddress()).build();
                    storeDao.save(vo);            
    }
    @Async
    public void doneInsertOrUpdate(String storePhone,String doneMessage) {
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<Object,Object>map=principalDetails.getPrinci();
        sqsService.sendEmailAsync(doneMessage,principalDetails.getUsername());//회사이메일
        sqsService.sendPhoneAsync(doneMessage, map.get("phone").toString());//회사휴대폰
        sqsService.sendPhoneAsync(doneMessage, storePhone);//매장휴대폰
    }
    private void checkValues(tryInsertStoreDto tryInsertStoreDto) {
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
    }
    private void checkPhoneAndTel(String phone ,String tel) {
        if(utillService.checkOnlyNum(tel)||utillService.checkOnlyNum(phone)){
            throw utillService.makeRuntimeEX("전화번호 혹은 휴대폰번호는 숫자만 가능합니다", "checkValues");
        }
    }
    private void checkSameStore(String storeName,String companyNum,String address) {
        if(storeDao.countBySnameAndAddress(storeName,companyNum, address)!=0){
            throw utillService.makeRuntimeEX("같은위치에서 같은사업자번호로 이미 등록된 매장발견", "checkSameStore");
        }
    }
    private void checkCompayNum(String snum) {
        int count=0;
        try {
            count=storeDao.countBySnum(Long.parseLong(snum));
        } catch (NumberFormatException e) {
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 입력해주세요 ", "checkCompayNum");
        }
        //같은 사업자 번호로 회원 가입한 회사가 있어야함
        if(count==0){
            throw utillService.makeRuntimeEX("사업자 번호로 회원가입한 기업이 없습니다", "checkCompayNum");
        }
        //사업자 번호 검사는 일단 회사로 회원가입 후에 하는 시스템이므로 여기서 안해줘도 된다
    }
    private void checkOpenAndCloseTime(String openTime,String closeTime) {
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
            if(i<0){
                throw utillService.makeRuntimeEX("시간은 0보다 작을수 없습니다", "checkTime");
            }
        }
        //시작시간보다 종료시간이 빠른지 검사
        if(times.get(0)>times.get(2)||(times.get(0)==times.get(2)&&times.get(1)>=times.get(3))){
            throw utillService.makeRuntimeEX("마감시간이 오픈시간보다 빠를 수없습니다", "checkTime");
        }
    }
}
