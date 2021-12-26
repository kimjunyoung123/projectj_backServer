package com.kimcompay.projectjb.users.user;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.company.compayDao;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class userService {
    private Logger logger=LoggerFactory.getLogger(userService.class);
    @Value("${refresh_token_cookie}")
    private String refresh_token_cookie_name;
    @Value("${access_token_cookie}")
    private String access_token_cookie_name;
    @Autowired
    private jungbuService jungbuService;
    @Autowired
    private kakaoMapService kakaoMapService;
    @Autowired
    private userdao userdao;
    @Autowired
    private securityConfig securityConfig;
    @Autowired
    private compayDao compayDao;
    @Autowired
    private sqsService sqsService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    
    public JSONObject selectUserAction(String action,HttpServletRequest request) {
        logger.info("selectUserAction");
        if(action.equals(senums.checkt.get())){
            logger.info("로그인 조회 요청");
            return checkLogin(request, request.getParameter("detail"));
        }else if(action.equals("logout")){
            logger.info("로그아웃요청");
            return logOut(request);
        }else if(action.equals("checkEmail")){
            logger.info("이메일 중복검사");
            return utillService.getJson(checkSamEmail(request.getParameter("email")),"");
            
        }
        throw utillService.makeRuntimeEX("잘못된 요청", "selectUserAction");
    }
    private boolean checkSamEmail(String email) {
        logger.info("checkSamEmail");
        Map<String,Object>map=userdao.countByEmail(email,email);
        for(Entry<String,Object>m:map.entrySet()){
            if(Integer.parseInt(m.getValue().toString())!=0){
                return true;
            }
        }
        return false;
    }
    private JSONObject logOut(HttpServletRequest request) {
        logger.info("logOut");
        //쿠키제거
        List<String>cookieNames=new ArrayList<>();
        cookieNames.add(access_token_cookie_name);
        cookieNames.add(refresh_token_cookie_name);
        for(String cookieName:cookieNames){
            utillService.deleteCookie(cookieName, request,utillService.getHttpSerResponse());
        }
        //redis제거
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email=principalDetails.getUsername();
        logger.info("로그아웃 이메일: "+email);
        cookieNames.clear();
        cookieNames.add(email);
        cookieNames.add(utillService.getCookieValue(request, refresh_token_cookie_name));
        redisTemplate.delete(cookieNames);
        return utillService.getJson(true, "로그아웃완료");
    }
    public JSONObject changePwdForLost(String scope,tryUpdatePwdDato tryUpdatePwdDato) {
        logger.info("changePwdForLost");
        try {
            //요청 redis에서 꺼내기
            String token=tryUpdatePwdDato.getToken();
            Map<Object,Object>redis=checkRequest(token);
            //비밀번호 변경
            updatePwd(redis.get("email").toString(),tryUpdatePwdDato.getPwd(),tryUpdatePwdDato.getPwd2());
            redisTemplate.delete(token);
            return utillService.getJson(true, "변경되었습니다");
        } catch (NullPointerException e) {
            throw utillService.makeRuntimeEX("빈칸이 존재합니다", "changePwdForLost");
        }
    }
    private Map<Object,Object> checkRequest(String token) {
        logger.info("checkRequest");
        Map<Object,Object>redis=redisTemplate.opsForHash().entries(token);
            //요청이 있었는지검사
            if(utillService.checkEmthy(redis)){
                throw utillService.makeRuntimeEX("잘못된 요청입니다", "changePwdForLost");
            }
            //요청기간검사
            Timestamp timestamp=Timestamp.valueOf(redis.get("expire").toString().replace("T", " "));
            logger.info("비밀번호 변경유효 기간: "+timestamp);
            if(LocalDateTime.now().isAfter(timestamp.toLocalDateTime())){
                throw utillService.makeRuntimeEX("유효기간이 지난 요청입니다", "changePwdForLost");
            }
        return redis;
    }
    private void updatePwd(String email,String pwd,String pwd2) {
        logger.info("updatePwd");
        if(!pwd.equals(pwd2)){
            throw utillService.makeRuntimeEX("비밀번호가 일치하지 않습니다", "updatePwd");
        }
        Map<String,Object>map=userdao.countByEmail(email, email);
        String table=null;
        for(Entry<String, Object> m:map.entrySet()){
            if(Integer.parseInt(m.getValue().toString())!=0){
                table=m.getKey();
                logger.info("비밀번호 변경 테이블: "+table);
                break;
            }
        }
        String hashPwd=securityConfig.pwdEncoder().encode(pwd);
        try {
            if(table.equals("uc")){
                logger.info("유저 비밀번호변경");
                userdao.updateUserPwd(hashPwd, email);
            }else {
                logger.info("회사 비밀번호 변경");
                userdao.updateCompanyPwd(hashPwd, email);
            }
        } catch (Exception e) {
            utillService.makeRuntimeEX(senums.defaultFailMessage.get(), "updatePwd");
        }
    }
    private void updateUserInfor() {
        logger.info("updateUserInfor");

    }
    public Map<String,Object> selectPhoneByEmail(String email) {
        logger.info("selectPhoneByEmail");
        return userdao.findPhoneByEmail(email, email);
    }
    public Map<String,Object> selectEmailByPhone(String phone) {
        logger.info("selectEmailByPhone");
        return userdao.findEmailByPhone(phone, phone);
    }
    private JSONObject checkLogin(HttpServletRequest request,String detail) {
        logger.info("checkLogin");
        logger.info("detail: "+detail);
        try {
             //시큐리티 세션 꺼내기
            principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            //요청분류
            if(detail.equals("email")){
                logger.info("이메일만 전달");
                return utillService.getJson(true, principalDetails.getUsername());
            }else if(detail.equals(senums.allt.get())){
                logger.info("비밀번호 제외 후 전달");
                Map<Object,Object>map=principalDetails.getPrinci();
                logger.info(map.toString());
                map.put(refresh_token_cookie_name, null);//refresh token제거 비밀번호는 로그인시 애초에 redis에 저장하지 않음
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("message", map);
                jsonObject.put("flag", true);
                return jsonObject;
            }else{
                return utillService.getJson(false, "잘못된 요청");
            }
        } catch (NullPointerException |ClassCastException e) {
            throw utillService.makeRuntimeEX("로그인 정보없음", "checkLogin");
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject checkLogin(HttpServletRequest request,HttpServletResponse response) {
        logger.info("checkLoginAuth");
        boolean flag=Boolean.parseBoolean(request.getParameter("flag"));
        System.out.println(flag);
        if(flag){
            //로그인일자 수정해주기
            updateLoginDate(request.getParameter("date"),request.getParameter("kind"));
            return utillService.getJson(flag, "로그인완료");
        }
        return utillService.getJson(flag, request.getParameter("cause"));
    }
    @Async
    public void updateLoginDate(String date,String kind) {
        logger.info("updateLoginDate");
        //이메일 꺼내기
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email=principalDetails.getUsername();
        //로그인날짜로 수정
        Timestamp loginDate=Timestamp.valueOf(date);
        logger.info("로그인일자: "+loginDate);
        if(kind.equals(senums.persnal.get())){
            userdao.updateUserLoginDate(loginDate, email);
        }else if(kind.equals(senums.company.get())){
            userdao.updateCompanyLoginDate(loginDate, email);
        }else{
            logger.info("로그인 일자 갱신 실패");
        }
        logger.info("로그인일자 갱신완료");
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryInsertDto tryInsertDto,HttpSession session) {
        logger.info("insert");
        //휴대폰/이메일 인증했는지 검사
        checkAuth(tryInsertDto, session);
        //유효성검사
        checkValues(tryInsertDto);
        //인서트 시도
        try_insert(tryInsertDto);
        //세션비우기
        session.removeAttribute(senums.auth.get()+senums.emailt.get());
        session.removeAttribute(senums.auth.get()+senums.phonet.get());
        //회원가입 이메일 비동기 전송 
        sqsService.sendEmailAsync("장보고에 회원가입해주셔서 진심으로 감사합니다", tryInsertDto.getEmail());
        return utillService.getJson(true, "회원가입에 성공하였습니다");
    }
    private void try_insert(tryInsertDto tryInsertDto) {
        logger.info("try_insert");
        String hash_pwd=securityConfig.pwdEncoder().encode(tryInsertDto.getPwd());
        String post_code=tryInsertDto.getPost_code();
        if(tryInsertDto.getScope_num()==0){
            logger.info("일반 회원 가입 ");
            userVo vo=userVo.builder().email(tryInsertDto.getEmail()).uaddress(tryInsertDto.getAddress()).udetail_address(tryInsertDto.getDetail_address())
                                        .uphone(tryInsertDto.getPhone()).upostcode(post_code).upwd(hash_pwd).urole(senums.user_role.get())
                                        .usleep(0).build();
                                        userdao.save(vo);
        }else{
            logger.info("기업 회원가입");
            //추가 검사 
            checkCompanyValues(tryInsertDto);
            checkTimeAndOther(tryInsertDto);
            checkComNum(jungbuService.getCompanyNum(tryInsertDto.getCompany_num(),tryInsertDto.getStart_dt() ,tryInsertDto.getName()));
            comVo vo=comVo.builder().cdetail_address(tryInsertDto.getDetail_address()).caddress(tryInsertDto.getAddress()).cemail(tryInsertDto.getEmail()).ckind(tryInsertDto.getScope_num()).cnum(tryInsertDto.getCompany_num())
                                    .store_name(tryInsertDto.getStore_name()).crole(senums.company_role.get()).cphone(tryInsertDto.getPhone()).cpostcode(post_code).cpwd(hash_pwd).close_time(tryInsertDto.getClose_time()).csleep(0).ctel(tryInsertDto.getTel()).start_time(tryInsertDto.getOpen_time()).build();
                                    compayDao.save(vo);
        }
    }
    private void checkComNum(JSONObject response) {
        logger.info("checkComNum");
        JSONArray jsons=(JSONArray)response.get("data");
        JSONObject jsonObject=(JSONObject)jsons.get(0);
        if(!jsonObject.get("valid").equals("01")){
            throw utillService.makeRuntimeEX("사업자 조회에 실패했습니다", "checkComNum");
        }
        JSONObject stauts=(JSONObject)jsonObject.get("status");
        logger.info("사업자 상태: "+stauts);
        if(!stauts.get("b_stt_cd").equals("01")){
            throw utillService.makeRuntimeEX("휴업중이거나 폐업한 사업자번호입니다", "checkComNum");
        }
        logger.info("사업자등록 유효성검사 통과");
    }
    private void checkTimeAndOther(tryInsertDto tryInsertDto) {
        logger.info("checkTimeAndOther");
        //요청시간 꺼내기
        String open_time=utillService.getValue(tryInsertDto.getOpen_time(),"시간값이 규격에 맞지 않습니다", "checkTime");
        String close_time=utillService.getValue(tryInsertDto.getClose_time(),"시간값이 규격에 맞지 않습니다", "checkTime");
        logger.info("시작시간: "+open_time);
        logger.info("종료시간: "+close_time);
        //시간분리
        List<Integer>times=new ArrayList<>();
        try {
            for(String s:open_time.split(":")){
                times.add(Integer.parseInt(s));
            }
            for(String s:close_time.split(":")){
                times.add(Integer.parseInt(s));
            } 
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("시간값은 숫자만가능합니다", "checkTime");
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
        //전화번호 유효성검사
        if(utillService.checkOnlyNum(tryInsertDto.getTel())||utillService.checkOnlyNum(tryInsertDto.getPhone())){
            throw utillService.makeRuntimeEX("전화번호는 숫자만 입력가능합니다", "checkTimeAndOther");
        }
        logger.info("전화번호 유효성 통과");
    }
    private void checkCompanyValues(tryInsertDto tryInsertDto){
        logger.info("checkCompanyValues");
        //값꺼내기
        int company_num=0;
        try {
            //null검사를 받을 때 안하므로 여기서 한다
            company_num=Integer.parseInt(Optional.ofNullable(tryInsertDto.getCompany_num()).orElseThrow(()-> utillService.makeRuntimeEX("기업회원은 사업자 등록번호가 필수입니다", "checkCompanyValues")));
            //JSONObject res=jungbuService.getCompanyNum(company_num);
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 적어주세요", "try_insert");
        }
        String tel=null;
        try {
            tel=Optional.ofNullable(tryInsertDto.getTel()).orElseThrow(()->utillService.makeRuntimeEX("회사번호를 입력해주세요", "checkCompanyValues"));
        } catch (Exception e) {
            throw utillService.makeRuntimeEX(e.getMessage(), "checkCompanyValues");
        }
        //회사전화 사업자 등록번호 중복확인
        Map<String,Object>count=userdao.countByCnumNative(company_num,tel);
        logger.info("사업자번호 조회결과: "+count.toString());
        if(Integer.parseInt(count.get("cn").toString())!=0){
            throw utillService.makeRuntimeEX("이미등록된 사업자번호입니다 ", "try_insert");
        }else if(Integer.parseInt(count.get("ct").toString())!=0){
            throw utillService.makeRuntimeEX("이미등록된 회사번호입니다 ", "checkCompanyValues");
        }
        String store_name=Optional.ofNullable(tryInsertDto.getStore_name()).orElseThrow(()->utillService.makeRuntimeEX("회사명을 입력해 주세요","checkCompanyValues"));
        if(utillService.checkBlank(store_name)){
            throw utillService.makeRuntimeEX("회사명을 입력해 주세요","checkCompanyValues");
        }
        String start_dt=Optional.ofNullable(tryInsertDto.getStart_dt()).orElseThrow(()->utillService.makeRuntimeEX("개업일자를 입력해 주세요","checkCompanyValues"));
        if(utillService.checkBlank(start_dt)){
            throw utillService.makeRuntimeEX("개업일자가 비어있거나 숫자만 입력해주세요","checkCompanyValues");
        }
        if(utillService.checkOnlyNum(start_dt)){
            throw utillService.makeRuntimeEX("개업일자는 숫자만 주세요","checkCompanyValues");
        }
        logger.info("사업자유효성통과");
    }
    public Map<String,Object> getCount(String val) {
        return userdao.findByPhoneAndEmailJoinCompany(val,val,val,val);
    }
    private void checkValues(tryInsertDto tryInsertDto ) {
        logger.info("checkValues");
        //어떤 유형의 회원인지검사
        String scope=tryInsertDto.getScope();
        String scope_num=null;
        try {
            scope_num=senums.valueOf(scope).get();
            
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("존재하는 회원 유형이 아닙니다", "checkValues");
        }
        //이메일,전화번호 중복검사 validation 어노테이션으로 null검사는 다하고 온다
        String email=tryInsertDto.getEmail();
        String phone=tryInsertDto.getPhone();
        Map<String,Object>email_phone_count=userdao.findByPhoneAndEmailJoinCompany(phone, phone,email,email);
        for(Entry<String, Object> entry:email_phone_count.entrySet()){
            logger.info(entry.getValue().toString());
            if(Integer.parseInt(entry.getValue().toString())!=0){
                throw utillService.makeRuntimeEX("이미 존재하는 "+ senums.valueOf(entry.getKey()).get()+"입니다", "checkValues");
            }
        }
        //비밀번호 일치검사 자리수,공백검사는 validation으로 한다
        String pwd=tryInsertDto.getPwd();
        if(!pwd.equals(tryInsertDto.getPwd2())){
            throw utillService.makeRuntimeEX("비밀번호가 일치 하지 않습니다", "checkValues");
        }
        //주소검사 자리수,공백검사는 validation으로 한다
        JSONObject krespon=kakaoMapService.getAddress(tryInsertDto.getAddress());
        logger.info("주소 조회결과: "+krespon.toString());
        LinkedHashMap<String,Object>meta=(LinkedHashMap<String, Object>)krespon.get("meta");
        if(Integer.parseInt(meta.get("total_count").toString())==0){
            logger.info("주소 검색결과 미존재");
            throw utillService.makeRuntimeEX("주소검색결과가 없습니다", "checkValues");
        }
        if(Optional.ofNullable(tryInsertDto.getName()).orElseGet(()->null)==null){
            throw utillService.makeRuntimeEX("이름을 입력해주세요 ", "checkCompanyValues");
        }
        tryInsertDto.setScope_num(Integer.parseInt(scope_num));
        
        logger.info("회원가입 유효성검사 통과");
    }
    private void checkAuth(tryInsertDto tryInsertDto,HttpSession httpSession) {
        logger.info("checkAuth");
        boolean check_email=false;
        boolean check_phone=false;
        Map<String,Object>auth_phone=new HashMap<>();
        Map<String,Object>auth_email=new HashMap<>();
        String phone=null;
        String email=null;
        //세션에서 인증정보가져오기
        try {
            auth_phone=(Map<String,Object>)httpSession.getAttribute(senums.auth.get()+senums.phonet.get());
            logger.info("휴대폰인증내역: "+auth_phone);
            phone=auth_phone.get("val").toString();
            check_phone=Boolean.parseBoolean(auth_phone.get("res").toString());
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("휴대폰 인증을 먼저 해주세요", "checkAuth");
        }
        try {
            auth_email=(Map<String,Object>)httpSession.getAttribute(senums.auth.get()+senums.emailt.get());
            logger.info("이메일인증내역: "+auth_email);
            email=auth_email.get("val").toString();
            check_email=Boolean.parseBoolean(auth_email.get("res").toString());
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("이메일 인증을 먼저 해주세요", "checkAuth");
        }
        //인증완료 했는지 검사
        if(!check_email){
            throw utillService.makeRuntimeEX("이메일 인증이 되지 않았습니다", "checkAuth");
        }else if(!check_phone){
            throw utillService.makeRuntimeEX("휴대폰 인증이 되지 않았습니다", "checkAuth");
        }else if(!tryInsertDto.getPhone().equals(phone)){
            throw utillService.makeRuntimeEX("인증한 휴대폰과 번호가 다릅니다", "checkAuth");
        }else if(!tryInsertDto.getEmail().equals(email)){
            throw utillService.makeRuntimeEX("인증한 이메일과 번호가 다릅니다", "checkAuth");
        }
        logger.info("인증 유효성검사 통과");
    }
    public JSONObject findChangePwdToken(String token) {
        logger.info("findChangePwdToken");
        logger.info("토큰: "+token);
        Map<Object, Object>map=redisTemplate.opsForHash().entries(token);
        Timestamp timestamp=Timestamp.valueOf(Optional.ofNullable(map.get("expire").toString().replace("T", " ")).orElseThrow(()->utillService.makeRuntimeEX("유효하지 않는 요청입니다","findChangePwdToken" )));
        logger.info("토큰 유효 날짜: "+timestamp);
        if(LocalDateTime.now().isAfter(timestamp.toLocalDateTime())){
            throw utillService.makeRuntimeEX("유효기간이 만료된 요청입니다", "findChangePwdToken");
        }
        return utillService.getJson(true, "유효한 요청");
    }
}
