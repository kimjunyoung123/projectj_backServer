package com.kimcompay.projectjb.users.user;


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
import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.kimcompay.projectjb.apis.kakaos.kakaoMapService;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;
import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.company.compayDao;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class userService {
    private Logger logger=LoggerFactory.getLogger(userService.class);
    @Value("${refresh_token_cookie}")
    private String refresh_token_cookie_name;
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

    public JSONObject checkLogin(HttpServletRequest request,String detail) {
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
        } catch (NullPointerException e) {
            throw utillService.makeRuntimeEX("로그인 실패", "checkLogin");
        }
    }
    public JSONObject checkLogin(HttpServletRequest request,HttpServletResponse response) {
        logger.info("checkLoginAuth");
        boolean flag=Boolean.parseBoolean(request.getParameter("flag"));
        System.out.println(flag);
        if(flag){
            return utillService.getJson(flag, "로그인완료");
        }
        return utillService.getJson(flag, request.getParameter("cause"));
    }
    public JSONObject insert(tryInsertDto tryInsertDto,HttpSession session) {
        logger.info("insert");
        //휴대폰/이메일 인증했는지 검사
        //checkAuth(tryInsertDto, session);
        //유효성검사
        checkValues(tryInsertDto);
        //인서트 시도
        try_insert(tryInsertDto);
        //세션비우기
        session.removeAttribute(senums.auth.get()+senums.emailt.get());
        session.removeAttribute(senums.auth.get()+senums.phonet.get() );
        return utillService.getJson(false, "회원가입에 성공하였습니다");
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
            checkCompanyNum(tryInsertDto);
            checkTimeAndOther(tryInsertDto);
            jungbuService.getCompanyNum(tryInsertDto.getCompany_num(),tryInsertDto.getStart_date() ,tryInsertDto.getName()); 
            comVo vo=comVo.builder().cdetail_address(tryInsertDto.getDetail_address()).caddress(tryInsertDto.getAddress()).cemail(tryInsertDto.getEmail()).ckind(tryInsertDto.getScope_num()).cnum(tryInsertDto.getCompany_num())
                                    .crole(senums.company_role.get()).cphone(tryInsertDto.getPhone()).cpostcode(post_code).cpwd(hash_pwd).close_time(tryInsertDto.getClose_time()).csleep(0).ctel(tryInsertDto.getTel()).start_time(tryInsertDto.getOpen_time()).build();
                                    compayDao.save(vo);
        }
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
        if(times.get(0)>times.get(2)){
            throw utillService.makeRuntimeEX("종료시간이 시작시간보다 빠를 수없습니다", "checkTime");
        }
        logger.info("시간 유효성검사 통과");
        //전화번호 유효성검사
        if(utillService.checkOnlyNum(tryInsertDto.getTel())||utillService.checkOnlyNum(tryInsertDto.getPhone())){
            throw utillService.makeRuntimeEX("전화번호는 숫자만 입력가능합니다", "checkTimeAndOther");
        }
        logger.info("전화번호 유효성 통과");
    }
    private void checkCompanyNum(tryInsertDto tryInsertDto){
        logger.info("checkCompanyNum");
        //값꺼내기
        int company_num=0;
        try {
            //null검사를 받을 때 안하므로 여기서 한다
            company_num=Integer.parseInt(Optional.ofNullable(tryInsertDto.getCompany_num()).orElseThrow(()-> utillService.makeRuntimeEX("기업회원은 사업자 등록번호가 필수입니다", "checkCompanyNum")));
            //JSONObject res=jungbuService.getCompanyNum(company_num);
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("사업자 번호는 숫자만 적어주세요", "try_insert");
        }
        String tel=null;
        try {
            tel=Optional.ofNullable(tryInsertDto.getTel()).orElseThrow(()->utillService.makeRuntimeEX("회사번호를 입력해주세요", "checkCompanyNum"));
        } catch (Exception e) {
            //TODO: handle exception
        }
        //회사전화 사업자 등록번호 중복확인
        Map<String,Object>count=userdao.countByCnumNative(company_num,tel);
        logger.info("사업자번호 조회결과: "+count);
        if(Integer.parseInt(count.get("cn").toString())!=0){
            throw utillService.makeRuntimeEX("이미등록된 사업자번호입니다 ", "try_insert");
        }else if(Integer.parseInt(count.get("ct").toString())!=0){
            throw utillService.makeRuntimeEX("이미등록된 전화번호입니다 ", "checkCompanyNum");
        }
        logger.info("사업자번호/회사번호 유효성 통과");
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
            throw utillService.makeRuntimeEX("이름을 입력해주세요 ", "checkCompanyNum");
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
}
