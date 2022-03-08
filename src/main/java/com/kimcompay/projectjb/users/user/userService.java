package com.kimcompay.projectjb.users.user;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class userService {
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
    private RedisTemplate<String,Object>redisTemplate;
    @Autowired
    private userAdapter userAdapter;
    @Autowired
    private jwtService jwtService;

    public JSONObject getAuthActionHub(String action,HttpServletRequest request) {
        if(action.equals(senums.checkt.get())){
            return checkLogin(request, request.getParameter("detail"));
        }else if(action.equals("logout")){
            return logOut(request);
        }
        throw utillService.makeRuntimeEX("잘못된 요청", "getActionHub");
    }
    public JSONObject getActionHub(String action,HttpServletRequest request) {
        if(action.equals("checkEmail")){
            return utillService.getJson(checkSamEmail(request.getParameter("email")),"");
        }
        throw utillService.makeRuntimeEX("잘못된 요청", "getActionHub");
    }
    public void oauthLogin(String email,userVo userVo) {
        try {
            oauthLogin(userVo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String message=e.getMessage();
            if(!message.startsWith("메")){
                message=senums.defaultFailMessage.get();
            }
            throw utillService.makeRuntimeEX(message, "oauthLogin");
        }
        //로그인시간갱신
        updateLoginDate(senums.persnal.get(),email);
    }
    private void oauthLogin(userVo userVo) {
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
            userVo=userdao.findByEmail(email).orElseThrow(()->new IllegalAccessError("찾을 수없는 이메일"));//에러가터질확률이 없을거같다  그래서 no trycatch
        }else if(Integer.parseInt(map.get("up").toString())!=0){
            userVo=userdao.findByUphone(phone);//에러가터질확률이 없을거같다  그래서 no trycatch
        }else{
            userVo.setUpwd(securityConfig.pwdEncoder().encode(userVo.getUpwd()));
            userdao.save(userVo);
        }
        //vo->map
        userVo.setUlogin_date(Timestamp.valueOf(LocalDateTime.now()));//로그인시간 넣어주기
        Map<Object,Object>result=voToMap(userVo);
        String userId=Integer.toString(userVo.getUid());
        //redis에 유저정보 담기
        result.put("provider",userVo.getProvider());//naver/kakao다양한 소셜로그인시 대응 
        result.put("pwd", null);//비밀번호 지우기
        redisTemplate.opsForHash().put(userId+senums.loginTextRedis.get(), senums.loginTextRedis.get(),result);
        Map<Object, Object>loginInfor=redisTemplate.opsForHash().entries(userId+senums.loginTextRedis.get());
        System.out.println("로그인정보: "+loginInfor.toString());
        //로그인처리
        String accessToken=jwtService.get_access_token(userId);
        String refreshToken=jwtService.get_refresh_token();
        //리프레시토큰넣어주기
        redisTemplate.opsForValue().set(refreshToken,userId);
        redisTemplate.opsForHash().put(refreshTokenCookieName, refreshTokenCookieName, refreshToken);
        utillService.makeLoginCookie(accessToken,refreshToken,accessTokenCookieName,refreshTokenCookieName);

    }
    private Map<Object, Object> voToMap(userVo userVo) {
        userAdapter.adapterUser(userVo);
        return userAdapter.getMap();
    }
    private boolean checkSamEmail(String email) {
        Map<String,Object>map=userdao.countByEmail(email,email);
        for(Entry<String,Object>m:map.entrySet()){
            if(Integer.parseInt(m.getValue().toString())!=0){
                return true;
            }
        }
        return false;
    }
    private JSONObject logOut(HttpServletRequest request) {
        //쿠키제거
        List<String>cookieNames=new ArrayList<>();
        cookieNames.add(accessTokenCookieName);
        cookieNames.add(refreshTokenCookieName);
        for(String cookieName:cookieNames){
            utillService.deleteCookie(cookieName, request,utillService.getHttpSerResponse());
        }
        //redis제거
        cookieNames.clear();//배열하나로 돌려쓰기
        String id=Integer.toString(utillService.getLoginId());
        cookieNames.add(id);
        String refreshTokenValue=utillService.getCookieValue(request, refreshTokenCookieName);
        cookieNames.add(refreshTokenValue);
        redisTemplate.delete(cookieNames);
        return utillService.getJson(true, "로그아웃완료");
    }
    public JSONObject changePwdForLost(tryUpdatePwdDato tryUpdatePwdDato) {
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
        Map<Object,Object>redis=redisTemplate.opsForHash().entries(token);
            //요청이 있었는지검사
            if(utillService.checkEmthy(redis)){
                throw utillService.makeRuntimeEX("잘못된 요청입니다", "changePwdForLost");
            }
            //요청기간검사
            Timestamp timestamp=Timestamp.valueOf(redis.get("expire").toString().replace("T", " "));
            if(LocalDateTime.now().isAfter(timestamp.toLocalDateTime())){
                throw utillService.makeRuntimeEX("유효기간이 지난 요청입니다", "changePwdForLost");
            }
        return redis;
    }
    private void updatePwd(String email,String pwd,String pwd2) {
        if(!pwd.equals(pwd2)){
            throw utillService.makeRuntimeEX("비밀번호가 일치하지 않습니다", "updatePwd");
        }
        Map<String,Object>map=userdao.countByEmail(email, email);
        String table=null;
        for(Entry<String, Object> m:map.entrySet()){
            if(Integer.parseInt(m.getValue().toString())!=0){
                table=m.getKey();
                break;
            }
        }
        String hashPwd=securityConfig.pwdEncoder().encode(pwd);
        try {
            if(table.equals("uc")){
                userdao.updateUserPwd(hashPwd, email);
            }else {
                userdao.updateCompanyPwd(hashPwd, email);
            }
        } catch (Exception e) {
            utillService.makeRuntimeEX(senums.defaultFailMessage.get(), "updatePwd");
        }
    }
    public Map<String,Object> selectPhoneByEmail(String email) {
        return userdao.findPhoneByEmail(email, email);
    }
    public Map<String,Object> selectEmailByPhone(String phone) {
        return userdao.findEmailByPhone(phone, phone);
    }
    private JSONObject checkLogin(HttpServletRequest request,String detail) {
        JSONObject jsonObject=new JSONObject();
        try {
             //시큐리티 세션 꺼내기
            principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<Object,Object>map=principalDetails.getPrinci();
            //System.out.println("detial:"+detail);
            //요청분류
            if(detail.equals("email")){
                return utillService.getJson(true, principalDetails.getUsername()+","+principalDetails.getRole()+","+principalDetails.getPrinci().get("id"));
            }else if(detail.equals(senums.allt.get())){
                map.put(refreshTokenCookieName, null);//refresh token제거 비밀번호는 로그인시 애초에 redis에 저장하지 않음
                jsonObject.put("message", map);
                return jsonObject;
            }else if(detail.equals("role")){
                return utillService.getJson(true, map.get("role").toString());
            }else if(detail.equals("address")){
                jsonObject.put("detailAddress", map.get("detail_address"));
                jsonObject.put("postcode", map.get("post_code"));
                jsonObject.put("address", map.get("address"));
            }else{
                return utillService.getJson(false, "잘못된 요청");
            }
        } catch (NullPointerException |ClassCastException e) {
            throw utillService.makeRuntimeEX("로그인 정보없음", "checkLogin");
        }
        jsonObject.put("flag", true);
        return jsonObject;
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject checkLogin(HttpServletRequest request,HttpServletResponse response) {
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
        //로그인날짜로 수정
        Timestamp date=Timestamp.valueOf(LocalDateTime.now());
        if(kind.equals(senums.persnal.get())){
            userdao.updateUserLoginDate(date, email);
        }else if(kind.equals(senums.company.get())){
            userdao.updateCompanyLoginDate(date, email);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryInsertDto tryInsertDto,HttpSession session) {
        //휴대폰/이메일 인증했는지 검사
        checkAuth(tryInsertDto, session);
        //유효성검사
        checkValues(tryInsertDto);
        //인서트 시도
        try_insert(tryInsertDto);
        //회원가입 이메일 비동기 전송 
        try {
            sqsService.sendEmailAsync("장보고에 회원가입해주셔서 진심으로 감사합니다",tryInsertDto.getEmail());
        } catch (Exception e) {
            utillService.writeLog("회원가입 후 알림메세지 전송실패",userService.class);
        }
        return utillService.getJson(true, "회원가입에 성공하였습니다");
    }
    private void try_insert(tryInsertDto tryInsertDto) {
       String hash_pwd=securityConfig.pwdEncoder().encode(tryInsertDto.getPwd());
        String post_code=tryInsertDto.getPost_code();
        if(tryInsertDto.getScope_num()==0){
            userVo vo=userVo.builder().email(tryInsertDto.getEmail()).uaddress(tryInsertDto.getAddress()).udetail_address(tryInsertDto.getDetail_address())
                                        .uphone(tryInsertDto.getPhone()).upostcode(post_code).upwd(hash_pwd).urole(senums.user_role.get())
                                        .provider(senums.defaultProvider.get()).ubirth(tryInsertDto.getBirth()).usleep(0).build();
                                        userdao.save(vo);
        }else{
            //추가 검사 
            checkCompanyValues(tryInsertDto);
            checkComNum(jungbuService.getCompanyNum(tryInsertDto.getCompany_num(),tryInsertDto.getStart_dt().replace("-", ""),tryInsertDto.getName()));
            comVo vo=comVo.builder().cdetail_address(tryInsertDto.getDetail_address()).caddress(tryInsertDto.getAddress()).cemail(tryInsertDto.getEmail()).ckind(tryInsertDto.getScope_num()).cnum(tryInsertDto.getCompany_num())
                                    .store_name(tryInsertDto.getStore_name()).crole(senums.company_role.get()).cphone(tryInsertDto.getPhone()).cpostcode(post_code).cpwd(hash_pwd).csleep(0).ctel(tryInsertDto.getTel()).build();
                                    
                                    compayDao.save(vo);
        }
    }
    private void checkComNum(JSONObject response) {
        JSONArray jsons=(JSONArray)response.get("data");
        JSONObject jsonObject=(JSONObject)jsons.get(0);
        if(!jsonObject.get("valid").equals("01")){
            throw utillService.makeRuntimeEX("사업자 조회에 실패했습니다", "checkComNum");
        }
        JSONObject stauts=(JSONObject)jsonObject.get("status");
        if(!stauts.get("b_stt_cd").equals("01")){
            throw utillService.makeRuntimeEX("휴업중이거나 폐업한 사업자번호입니다", "checkComNum");
        }
    }
    private void checkCompanyValues(tryInsertDto tryInsertDto){
        //사업자번호 검사
        checkCompnayNum(tryInsertDto.getCompany_num());
        //회사명 검사
        String store_name=Optional.ofNullable(tryInsertDto.getStore_name()).orElseThrow(()->utillService.makeRuntimeEX("회사명을 입력해 주세요","checkCompanyValues"));
        if(utillService.checkBlank(store_name)){
            throw utillService.makeRuntimeEX("회사명을 입력해 주세요","checkCompanyValues");
        }
        //개업일자 검사
        String start_dt=Optional.ofNullable(tryInsertDto.getStart_dt()).orElseThrow(()->utillService.makeRuntimeEX("개업일자를 입력해 주세요","checkCompanyValues"));
        checkOpenDate(start_dt);
    }
    private void checkOpenDate(String date) {
        if(utillService.checkBlank(date)){
            throw utillService.makeRuntimeEX("개업일자가 비어있습니다","checkCompanyValues");
        }else if(LocalDateTime.now().isBefore(Timestamp.valueOf(date+" 23:59:59").toLocalDateTime())){
            throw utillService.makeRuntimeEX("개업일자가 현재보다 빠릅니다", "checkOpenDate");
        }
    }
    private void checkCompnayNum(String num) {
        try {
            //null검사를 받을 때 안하므로 여기서 한다
            Long num2=Long.parseLong(Optional.ofNullable(num).orElseThrow(()-> new NullPointerException()));
            int count=userdao.countByCnumNative(num2);
            if(count!=0){
                throw utillService.makeRuntimeEX("이미등록된 사업자번호입니다 ", "try_insert");
            }     
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 적어주세요", "try_insert");
        }catch( NullPointerException e2){
            throw utillService.makeRuntimeEX("기업회원은 사업자 등록번호가 필수입니다", "try_insert");
        }
    }
    public Map<String,Object> getCount(String val) {
        return userdao.findByPhoneAndEmailJoinCompany(val,val,val,val);
    }
    private void checkValues(tryInsertDto tryInsertDto ) {
        //어떤 유형의 회원인지검사
        String scope_num=selectKind(tryInsertDto.getScope());
        //이메일,전화번호 중복검사 validation 어노테이션으로 null검사는 다하고 온다
        checkExist(tryInsertDto.getPhone(), tryInsertDto.getEmail());
        //비밀번호 일치검사 자리수,공백검사는 validation으로 한다
        checkSamePwd(tryInsertDto.getPwd(), tryInsertDto.getPwd2());
        //주소검사 자리수,공백검사는 validation으로 한다
        kakaoMapService.checkAddress(tryInsertDto.getAddress());
        //회원가입 유형을 구분하기위해 지정
        tryInsertDto.setScope_num(Integer.parseInt(scope_num));
        //일반회원일시 미성년자인지 검사
        if(scope_num.equals(senums.persnal.get())){
            checkAge(tryInsertDto.getBirth());
        }
    }
    private void checkAge(String birch) {

            String[] birth=birch.split("-");
            int nowYear=LocalDateTime.now().getYear();
            int year=Integer.parseInt(birth[0]);
            int month=Integer.parseInt(birth[1]);
            int day=Integer.parseInt(birth[2]);
            if(nowYear-year<18){
                throw utillService.makeRuntimeEX("18이상 가입가능한 서비스입니다", "checkValues");
            }
            if(month>12||month<1){
                throw utillService.makeRuntimeEX("월은 1이상 12이하입니다", "checkValues");
            }else if(day<1||day>31){
                throw utillService.makeRuntimeEX("일은 1이상 31이하입니다", "checkValues");
            }
    }
    private void checkSamePwd(String pwd,String pwd2) {
        if(!pwd.equals(pwd2)){
            throw utillService.makeRuntimeEX("비밀번호가 일치 하지 않습니다", "checkValues");
        }
    }
    private void checkExist(String phone,String email) {
        Map<String,Object>emailPhoneCount=userdao.findByPhoneAndEmailJoinCompany(phone, phone,email,email);
        for(Entry<String, Object> entry:emailPhoneCount.entrySet()){
            if(Integer.parseInt(entry.getValue().toString())!=0){
                throw utillService.makeRuntimeEX("이미 존재하는 "+ senums.valueOf(entry.getKey()).get()+"입니다", "checkValues");
            }
        }
    }
    private String selectKind(String scope) {
        try {
            return senums.valueOf(scope).get();            
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("존재하는 회원 유형이 아닙니다", "checkValues");
        }
    }
    private void checkAuth(tryInsertDto tryInsertDto,HttpSession httpSession) {
        //휴대폰 번호 검사
        if(utillService.checkOnlyNum(tryInsertDto.getPhone())){
            throw utillService.makeRuntimeEX("휴대폰번호는 숫자만 입력가능합니다", "checkAuth");
        }
        //세션에서 인증정보가져오기
        String phone=utillService.checkAuthPhone(senums.auth.get());
        String email=utillService.checkAuthEmail();
        if(!tryInsertDto.getPhone().equals(phone)){
            throw utillService.makeRuntimeEX("인증한 휴대폰과 번호가 다릅니다", "checkAuth");
        }else if(!tryInsertDto.getEmail().equals(email)){
            throw utillService.makeRuntimeEX("인증한 이메일과 번호가 다릅니다", "checkAuth");
        }
    }
    public JSONObject findChangePwdToken(String token) {
        Map<Object, Object>map=redisTemplate.opsForHash().entries(token);
        Timestamp timestamp=Timestamp.valueOf(Optional.ofNullable(map.get("expire").toString().replace("T", " ")).orElseThrow(()->utillService.makeRuntimeEX("유효하지 않는 요청입니다","findChangePwdToken" )));
        if(LocalDateTime.now().isAfter(timestamp.toLocalDateTime())){
            throw utillService.makeRuntimeEX("유효기간이 만료된 요청입니다", "findChangePwdToken");
        }
        return utillService.getJson(true, "유효한 요청");
    }
}
