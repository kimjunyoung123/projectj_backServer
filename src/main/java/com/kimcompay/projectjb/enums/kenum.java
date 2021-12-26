package com.kimcompay.projectjb.enums;

public enum kenum {
    loginPage("login"),
    payPage("pay"),
    KOE320("중복요청 입니다 다시 시도 바랍니다");

    private String s;
    kenum(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    } 
}
