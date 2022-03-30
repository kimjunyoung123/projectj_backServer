package com.kimcompay.projectjb.enums;

public enum senums {
    
    defaultFailMessage("알 수 없는 에러발생"),
    auth("auth"),
    find("find"),
    persnal("0"),
    company("1"),
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
    user_role("ROLE_USER"),
    company_role("ROLE_COMPANY"),
    logint("login"),
    checkt("check"),
    allt("all"),
    noInRedis("n"),
    newToken("y"),
    sqsEndPoint("https://sqs.ap-northeast-2.amazonaws.com/527222691614/"),
    newpwd("newpwd"),
    changepwd("changepwd"),
    kakao("kakao"),
    naver("naver"),
    defaultPostcode("x"),
    defaultAddress("x"),
    defaultDetailAddress("x"),
    insert("insert"),
    update("update"),
    defaultMessage2("잘못된 요청입니다"),
    thumNail("thumNail"),
    imgSessionName("imgs"),
    phoneNull("휴대폰값을 입력해 주세요"),
    emailNull("이메일값을 입력해 주세요"),
    defaultProvider("no"),
    doneFlag("1"),
    notFlag("0"),
    settleKey("ST1009281328226982205"),
    settlekey2("pgSettle30y739r82jtd709yOfZ2yK5K"),
    basketsTextReids("basket"),
    loginTextRedis("login"),
    paySuc("0021"),
    paymentText("payment"),
    cardText("card"),
    kpayText("kpay"),
    vbankText("vbank");
    
    private String s;
    senums(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    }
}
