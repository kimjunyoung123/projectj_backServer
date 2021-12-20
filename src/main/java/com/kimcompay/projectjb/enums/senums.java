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
    up("전화번호"),
    cp("전화번호"),
    ue("유저 이메일"),
    ce("이메일"),
    Authorization("Authorization"),
    user_role("role_user"),
    company_role("role_company"),
    logint("login"),
    checkt("check"),
    allt("all");

    private String s;
    senums(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    }
}
