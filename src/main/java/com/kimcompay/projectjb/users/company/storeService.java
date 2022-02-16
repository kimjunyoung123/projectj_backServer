package com.kimcompay.projectjb.users.company;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.delivery.deliveryService;
import com.kimcompay.projectjb.enums.intenums;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.model.flyerDao;
import com.kimcompay.projectjb.users.company.model.flyerVo;
import com.kimcompay.projectjb.users.company.model.storeDao;
import com.kimcompay.projectjb.users.company.model.storeVo;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.tryUpdateStoreDto;

import org.checkerframework.checker.units.qual.K;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class storeService {
    private Logger logger=LoggerFactory.getLogger(storeService.class);
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
    private deliveryService deliveryService;
    @Autowired
    private flyerService flyerService;
    
    public JSONObject authGetsActionHub(String kind,int page,String keyword) {
        logger.info("authGetsActionHub");
        if(kind.equals("stores")){
            return getStoresByEmail(page,keyword);
        }else if(kind.equals("deliver")){
            List<String>dates=utillService.getDateInStrgin(keyword);
            HttpServletRequest request=utillService.getHttpServletRequest();
            return deliveryService.getDelivers(page,dates.get(0),dates.get(1),Integer.parseInt(request.getParameter("storeId")), Integer.parseInt(request.getParameter("state")));
        }else if(kind.equals("flyers")){
            List<String>dates=utillService.getDateInStrgin(keyword);
            HttpServletRequest request=utillService.getHttpServletRequest();
            List<Map<String,Object>>flyerVos=flyerService.getFlyerArr(Integer.parseInt(request.getParameter("storeId")),dates.get(0),dates.get(1),page);
            if(utillService.checkEmthy(flyerVos)){
                throw utillService.makeRuntimeEX("등록한 전단(상품)이 없습니다", "authGetsActionHub");
            }
            int totalPage=utillService.getTotalPage(Integer.parseInt(flyerVos.get(0).get("totalCount").toString()), pageSize);
            JSONObject respose=new JSONObject();
            respose.put("totalPage", totalPage);
            respose.put("message", flyerVos);
            respose.put("flag", true);
            return respose;
        }else {
            throw utillService.makeRuntimeEX("잘못된요청입니다", "authGetsActionHub");
        }
    }
    public JSONObject authGetActionHub(String kind,int id) {
        logger.info("authGetActionHub");
        if(kind.equals("store")){
            return getStore(id);
        }else if(kind.equals("deliver")){
            return deliveryService.getDeliverAddress(id);
        }else if(kind.equals("flyer")){
            return flyerService.getFlyerAndProducts(id);
        }else{
            throw utillService.makeRuntimeEX("잘못된요청입니다", "authGetsActionHub");
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject updateSleepOrOpen(int flag,int storeId) {
        logger.info("storeSleepOrOpen");
        int loginId=utillService.getLoginId();
        storeVo storeVo=storeDao.findById(storeId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 매장입니다", "storeSleepOrOpen"));
        if(storeVo.getCid()!=loginId){
            throw utillService.makeRuntimeEX("회사소유의 매장이 아닙니다", "storeSleepOrOpen");
        }
        storeVo.setSsleep(flag);
        return utillService.getJson(true, "엽업상태가 변경되었습니다"); 
    }
    @Transactional(rollbackFor =  Exception.class)
    public JSONObject tryUpdate(tryUpdateStoreDto  tryUpdateStoreDto) {
        logger.info("tryUpdate");
        //수정요청 가게 정보 가져오기
        storeVo storeVo=storeDao.findById(tryUpdateStoreDto.getId()).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 상품입니다", "tryUpdate"));
        boolean updateFlag=false;
        //주인이 맞는지 검사
        utillService.checkOwner(storeVo.getCid(),"매장 소유자의 계정이 아닙니다");
        //매장이름
        String storeName=tryUpdateStoreDto.getStoreName();
        if(!storeName.equals(tryUpdateStoreDto.getStoreName())){
            logger.info("매장이름 변경");
            updateFlag=true;
            updateStoreName(storeVo, storeName);
        }
        //시간이 변경되었나 검사
        String openTime=tryUpdateStoreDto.getOpenTime();
        String closeTime=tryUpdateStoreDto.getCloseTime();
        if(!openTime.equals(storeVo.getOpenTime())||!closeTime.equals(storeVo.getCloseTime())){
            logger.info("시간병경 요청");
            updateFlag=true;
            checkOpenAndCloseTime(openTime, closeTime);
            updateTime(storeVo, openTime, closeTime);
        }
        //배달반경
        int radius=tryUpdateStoreDto.getDeliverRadius();
        if(storeVo.getDeliverRadius()!=radius){
            logger.info("배달반경범위가 변경되었습니다");
            updateFlag=true;
            updateDeliverRadius(storeVo, radius);
        }
        //최소배달금액
        int minPrice=tryUpdateStoreDto.getMinPrice();
        if(storeVo.getMinPrice()!=minPrice){
            logger.info("배달 최소금액 변경");
            updateFlag=true;
            updateMinPrice(storeVo, minPrice);
        }
        //가게설명
        String text=tryUpdateStoreDto.getText();
        if(!text.equals(storeVo.getText())){
            logger.info("가게설명이 변경되었습니다");
            updateFlag=true;
            System.out.println(text);
            System.out.println(storeVo.getText());
            updateStoreText(storeVo, text);
        }
        //썸네일
        String thumbNail=tryUpdateStoreDto.getThumbNail();
        if(!thumbNail.equals(storeVo.getSimg())){
            logger.info("썸네일이 변경되었습니다");
            updateFlag=true;
            updateThumbNail(storeVo, thumbNail);
        }
        //주소
        String postCode=tryUpdateStoreDto.getPostcode();
        String address=tryUpdateStoreDto.getAddress();
        String detailAddress=tryUpdateStoreDto.getDetailAddress();
        if(!postCode.equals(storeVo.getSpostcode())||!address.equals(storeVo.getSaddress())||!detailAddress.equals(storeVo.getSdetail_address())){
            logger.info("주소가 변경되었습니다");
            updateFlag=true;
            updateAddress(storeVo, address, postCode, detailAddress);
        }
        //휴대폰/일반전화
        String phone=tryUpdateStoreDto.getPhone();
        String tel=tryUpdateStoreDto.getTel();
        if(!tel.equals(storeVo.getStel())){
            logger.info("tel 변경되었습니다");
            updateFlag=true;
            updateTel(storeVo, tel);
        }
        if(!phone.equals(storeVo.getSphone())){
            logger.info("휴대폰번호가 변경되었습니다");
            updateFlag=true;
            //인증검증로직필요함
            checkAuth(phone);
            updatePhone(storeVo, phone);
        }
        //사업자번호
        String companyNum=tryUpdateStoreDto.getNum();
        if(!companyNum.equals(storeVo.getSnum())){
            logger.info("사업자 번호가 변경되었습니다");
            updateFlag=true;
            //사업자번호 검증로직필요
            checkCompayNum(companyNum);
            updateCompanyNum(storeVo, companyNum);
        }
        //가게정보가 수정되면 알림메세지/이메일전송
        if(updateFlag){
           doneUpdate(tryUpdateStoreDto);
            return utillService.getJson(true, "변경이 완료되었습니다");
        }else{
            return utillService.getJson(false, "변경사항이 없습니다");
        }
    }
    @Async
    public void doneUpdate(tryUpdateStoreDto tryUpdateStoreDto) {
        logger.info("doneUpdate");
        String updateMessage="가게정보가 수정되었습니다";
        try {
            tryInsertStoreDto dto=new tryInsertStoreDto();
            dto.setPhone(tryUpdateStoreDto.getPhone());
            dto.setText(tryUpdateStoreDto.getText());
            dto.setThumbNail(tryUpdateStoreDto.getThumbNail());
            doneInsert(dto, updateMessage);
        } catch (Exception e) {
            logger.info("doneUpdate 처리중 예외발생");
        }
    }
    private void updateStoreName(storeVo storeVo,String storeName) {
        logger.info("updateStoreName");
        storeVo.setSname(storeName);
        logger.info("매장이름 변경완료");
    }
    private void updateCompanyNum(storeVo storeVo,String companyNum) {
        logger.info("updateCompanyNum");
        storeVo.setSnum(companyNum);
        logger.info("사업자번호 변경완료");
    }
    private void updatePhone(storeVo storeVo,String phone) {
        logger.info("updatePhone");
        storeVo.setSphone(phone);
        logger.info("휴대폰 변경완료");
    }
    private void updateTel(storeVo storeVo,String tel) {
        logger.info("updateTel");
        storeVo.setStel(tel);
        logger.info("일반전화 변경완료");
    }
    private void updateAddress(storeVo storeVo,String address,String postCode,String detailAddress) {
        logger.info("updateAddress");
        storeVo.setSpostcode(postCode);
        storeVo.setSaddress(address);
        storeVo.setSdetail_address(detailAddress);
        logger.info("주소 변경완료");
    }
    private void updateThumbNail(storeVo storeVo,String thumbNail) {
        logger.info("updateThumbNail");
        storeVo.setSimg(thumbNail);
        logger.info("썸네일 변경완료");
    }
    private void updateStoreText(storeVo storeVo,String text) {
        logger.info("updateStoreText");
        storeVo.setText(text);
        logger.info("가게설명 변경완료");
    }
    private void updateMinPrice(storeVo storeVo,int minPrice) {
        logger.info("updateMinPrice");
        storeVo.setMinPrice(minPrice);
        logger.info("배달최소금액 변경완료");
    }
    private void updateDeliverRadius(storeVo storeVo,int radius) {
        logger.info("updateDeliverRadius");
        storeVo.setDeliverRadius(radius);
        logger.info("배달반경 변경완료");
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
    private JSONObject getStore(int id) {
        logger.info("getStore");
        logger.info("조회 매장 고유번호: "+id);
        storeVo storeVo=storeDao.findById(id).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 매장입니다", "getStore"));
        logger.info("메징조회 정보: "+storeVo.toString());
        //매장 소유 회사 계정인지 검사
        utillService.checkOwner(storeVo.getCid(),"매장 소유자의 계정이 아닙니다");
        //유저 고유번호 노출방지
        storeVo.setCid(0);
        return utillService.getJson(true, storeVo);
    }
    private JSONObject getStoresByEmail(int page,String keyword) {
        logger.info("getStoresByEmail");
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
    private List<Map<String,Object>> getStoresInfor(int cid,String keyword,int requestPage) {
        logger.info("getStoresInfor");
        logger.info("검색키워드: "+keyword);
        if(utillService.checkBlank(keyword)){
            return storeDao.findByStoreNameNokeyword(cid,cid,utillService.getStart(requestPage, pageSize)-1,pageSize);
        }
        return storeDao.findByStoreInKeyword(cid,keyword,cid,keyword,utillService.getStart(requestPage, pageSize)-1,pageSize);
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
            String insertMessage="매장등록을 해주셔서 진심으로 감사합니다 \n 매장이름: "+tryInsertStoreDto.getStoreName()+"\n매장위치: "+tryInsertStoreDto.getAddress();
            doneInsert(tryInsertStoreDto,insertMessage);
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
                    .cid(utillService.getLoginId()).ssleep(intenums.NOT_FLAG.get()).stel(tryInsertStoreDto.getTel()).text(tryInsertStoreDto.getText()).build();
                    storeDao.save(vo);            
    }
    @Async
    public void doneInsert(tryInsertStoreDto tryInsertStoreDto,String doneMessage) {
        logger.info("doneInsert");
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<Object,Object>map=principalDetails.getPrinci();
        sqsService.sendEmailAsync(doneMessage,principalDetails.getUsername());//회사이메일
        sqsService.sendPhoneAsync(doneMessage, map.get("phone").toString());//회사휴대폰
        sqsService.sendPhoneAsync(doneMessage, tryInsertStoreDto.getPhone());//매장휴대폰
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
