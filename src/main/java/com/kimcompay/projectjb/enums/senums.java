package com.kimcompay.projectjb.enums;

public enum senums {
    
    auth("auth"),
    find("find"),
    persnal("0"),
    buyer("1"),
    seller("2"),
    phone("up,cp"),
    email("ue,ce"),
    phonet("phone"),
    emailt("email"),
    pwd("ue,ce"),
    pwtt("pwd"),
    up("휴대폰번호"),
    cp("휴대폰번호"),
    ue("유저 이메일"),
    ce("이메일"),
    Authorization("Authorization"),
    user_role("role_user"),
    company_role("role_company"),
    logint("login"),
    checkt("check"),
    allt("all"),
    noInRedis("n"),
    newToken("y"),
    sqsEndPoint("https://sqs.ap-northeast-2.amazonaws.com/527222691614/"),
    newpwd("newpwd"),
    changepwd("changepwd"),
    kakao("kakao"),
    naver("naver");

    private String s;
    senums(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    }
}
