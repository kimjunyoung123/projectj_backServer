package com.kimcompay.projectjb.enums;

public enum kenum {
    loginPage("login"),
    payPage("pay");

    private String s;
    kenum(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    } 
}
