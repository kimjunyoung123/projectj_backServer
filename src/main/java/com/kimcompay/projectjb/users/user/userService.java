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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.model.comVo;
import com.kimcompay.projectjb.users.company.model.compayDao;
import com.kimcompay.projectjb.users.user.model.tryInsertDto;
import com.kimcompay.projectjb.users.user.model.tryUpdatePwdDato;
import com.kimcompay.projectjb.users.user.model.userAdapter;
import com.kimcompay.projectjb.users.user.model.userVo;
import com.kimcompay.projectjb.users.user.model.userdao;

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
    private String refreshTokenCookieName;
    @Value("${access_token_cookie}")
    private String accessTokenCookieName;
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
    @Autowired
    private userAdapter userAdapter;
    @Autowired
    private jwtService jwtService;

    public JSONObject getAuthActionHub(String action,HttpServletRequest request) {
        logger.info("getAuthActionHub");
        if(action.equals(senums.checkt.get())){
            logger.info("로그인 조회 요청");
            return checkLogin(request, request.getParameter("detail"));
        }else if(action.equals("logout")){
            logger.info("로그아웃요청");
            return logOut(request);
        }
        throw utillService.makeRuntimeEX("잘못된 요청", "getActionHub");
    }
    public JSONObject getActionHub(String action,HttpServletRequest request) {
        logger.info("getActionHub");
        if(action.equals("checkEmail")){
            logger.info("이메일 중복검사");
            return utillService.getJson(checkSamEmail(request.getParameter("email")),"");
        }
        throw utillService.makeRuntimeEX("잘못된 요청", "getActionHub");
    }
    public void oauthLogin(String email,userVo userVo) {
        logger.info("oauthLogin");
        //로그인처리
        String accessToken=jwtService.get_access_token(email);
        String refreshToken=jwtService.get_refresh_token();
        try {
            oauthLogin(userVo,refreshToken);
        } catch (RuntimeException e) {
            logger.info("소셜로그인 에러 발생: "+e.getMessage());
            String message=e.getMessage();
            if(!message.startsWith("메")){
                message=senums.defaultFailMessage.get();
            }
            throw utillService.makeRuntimeEX(message, "oauthLogin");
        }
        //토큰 쿠키발급
        utillService.makeLoginCookie(accessToken,refreshToken,accessTokenCookieName,refreshTokenCookieName);
    }
    private void oauthLogin(userVo userVo,String refreshToken) {
        logger.info("oauthLogin");
        String email=userVo.getEmail();
        String phone=userVo.getUphone();
        Map<String,Object>map=userdao.findByPhoneAndEmailJoinCompany(phone, phone, email, email);
        int year=Integer.parseInt(userVo.getUbirth().split("-")[0]);
        int nowYear=LocalDateTime.now().getYear();
        // 소셜로그인은 보통유저만 가능 이미 사용하는 회사가 있다면 팅겨주기
        if(Integer.parseInt(map.get("ce").toString())!=0||Integer.parseInt(map.get("cp").toString())!=0){
            throw new RuntimeException("메세지: 이미 존재하는 회사의 이메일 혹은 전화번호입니다");
        }else if(nowYear-year<18){//나이검사
            throw new RuntimeException("메세지: 18세 미만은 소셜로그인이 불가합니다");
        }else if(Integer.parseInt(map.get("ue").toString())!=0){
            logger.info("이미 존재하는 이메일: "+email);
            userVo=userdao.findByEmail(email).orElseThrow(()->new IllegalAccessError("찾을 수없는 이메일"));//에러가터질확률이 없을거같다  그래서 no trycatch
        }else if(Integer.parseInt(map.get("up").toString())!=0){
            logger.info("이미 존재하는 휴대폰번호: "+phone);
            userVo.setUpwd(securityConfig.pwdEncoder().encode(userVo.getUpwd()));
            userVo=userdao.findByUphone(phone);//에러가터질확률이 없을거같다  그래서 no trycatch
        }else{
            logger.info("소셜로그인으로 첫로그인 요청 회원가입 진행");
            userdao.save(userVo);
        }
        logger.info("소셜 로그인중 찾은 유저정보: "+userVo);
        //vo->map
        userVo.setUlogin_date(Timestamp.valueOf(LocalDateTime.now()));//로그인시간 넣어주기
        Map<Object,Object>result=voToMap(userVo);
        //redis에 유저정보 담기
        //redis에 맞게 형변환 
        utillService.makeAllToString(result);
        result.put(refreshTokenCookieName, refreshToken);//리프레시토큰 함께 넣어주기
        result.put("pwd", null);//비밀번호 지우기
        redisTemplate.opsForHash().putAll(email,result);
        redisTemplate.opsForValue().set(refreshToken,email);//리프레시토큰 넣어주기
        //로그인시간갱신
        updateLoginDate(senums.persnal.get(),email);
        logger.info("소셜 로그인 완료");
    }
    private Map<Object, Object> voToMap(userVo userVo) {
        logger.info("voToMap");
        userAdapter.adapterUser(userVo);
        return userAdapter.getMap();
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
        cookieNames.add(accessTokenCookieName);
        cookieNames.add(refreshTokenCookieName);
        for(String cookieName:cookieNames){
            utillService.deleteCookie(cookieName, request,utillService.getHttpSerResponse());
        }
        //redis제거
        principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email=principalDetails.getUsername();
        logger.info("로그아웃 이메일: "+email);
        cookieNames.clear();
        cookieNames.add(email);
        cookieNames.add(utillService.getCookieValue(request, refreshTokenCookieName));
        redisTemplate.delete(cookieNames);
        return utillService.getJson(true, "로그아웃완료");
    }
    public JSONObject changePwdForLost(tryUpdatePwdDato tryUpdatePwdDato) {
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
                return utillService.getJson(true, principalDetails.getUsername()+","+principalDetails.getRole());
            }else if(detail.equals(senums.allt.get())){
                logger.info("비밀번호 제외 후 전달");
                Map<Object,Object>map=principalDetails.getPrinci();
                logger.info(map.toString());
                map.put(refreshTokenCookieName, null);//refresh token제거 비밀번호는 로그인시 애초에 redis에 저장하지 않음
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("message", map);
                jsonObject.put("flag", true);
                return jsonObject;
            }else if(detail.equals("role")){
                Map<Object,Object>map=principalDetails.getPrinci();
                return utillService.getJson(true, map.get("role").toString());
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
            principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            updateLoginDate(request.getParameter("kind"),principalDetails.getUsername());
            return utillService.getJson(flag, "로그인완료");
        }
        return utillService.getJson(flag, request.getParameter("cause"));
    }
    @Async
    public void updateLoginDate(String kind,String email) {
        logger.info("updateLoginDate");
        //로그인날짜로 수정
        Timestamp date=Timestamp.valueOf(LocalDateTime.now());
        if(kind.equals(senums.persnal.get())){
            userdao.updateUserLoginDate(date, email);
        }else if(kind.equals(senums.company.get())){
            userdao.updateCompanyLoginDate(date, email);
        }else{
            logger.info("로그인 일자 갱신 실패");
        }
        logger.info("로그인일자 갱신완료");
    }
    public void postActionHub() {
        logger.info("postActionHub");
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
        try {
            sqsService.sendEmailAsync("장보고에 회원가입해주셔서 진심으로 감사합니다", tryInsertDto.getEmail());
        } catch (Exception e) {
            logger.info("회원가입 후 알림메세지 전송실패");
        }
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
                                        .ubirth(tryInsertDto.getBirth()).usleep(0).build();
                                        userdao.save(vo);
        }else{
            logger.info("기업 회원가입");
            //추가 검사 
            checkCompanyValues(tryInsertDto);
            checkComNum(jungbuService.getCompanyNum(tryInsertDto.getCompany_num(),tryInsertDto.getStart_dt().replace("-", ""),tryInsertDto.getName()));
            comVo vo=comVo.builder().cdetail_address(tryInsertDto.getDetail_address()).caddress(tryInsertDto.getAddress()).cemail(tryInsertDto.getEmail()).ckind(tryInsertDto.getScope_num()).cnum(tryInsertDto.getCompany_num())
                                    .store_name(tryInsertDto.getStore_name()).crole(senums.company_role.get()).cphone(tryInsertDto.getPhone()).cpostcode(post_code).cpwd(hash_pwd).csleep(0).ctel(tryInsertDto.getTel()).build();
                                    
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
            throw utillService.makeRuntimeEX("개업일자가 비어있습니다","checkCompanyValues");
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
        if(scope_num.equals(senums.persnal.get())){
            logger.info("일반 회원이므로 나이 검사");
            //나이계산
            logger.info("생년월일: "+tryInsertDto.getBirth());
            String[] birth=tryInsertDto.getBirth().split("-");
            int nowYear=LocalDateTime.now().getYear();
            int year=Integer.parseInt(birth[0]);
            int month=Integer.parseInt(birth[1]);
            int day=Integer.parseInt(birth[2]);
            if(nowYear-year<18){
                logger.info("18세미만 가입요청");
                throw utillService.makeRuntimeEX("18이상 가입가능한 서비스입니다", "checkValues");
            }
            if(month>12||month<1){
                throw utillService.makeRuntimeEX("월은 1이상 12이하입니다", "checkValues");
            }else if(day<1||day>31){
                throw utillService.makeRuntimeEX("일은 1이상 31이하입니다", "checkValues");
            }
        }
        if(utillService.checkOnlyNum(tryInsertDto.getPhone())){
            throw utillService.makeRuntimeEX("휴대폰번호는 숫자만 입력가능합니다", "checkTimeAndOther");
        }
        logger.info("전화번호 유효성 통과");
        logger.info("기업 회원가입 유효성검사 통과");
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
