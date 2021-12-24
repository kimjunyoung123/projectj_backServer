package com.kimcompay.projectjb.enums;

public enum nenum {
    loginPage("login");

    private String s;
    nenum(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    } 
}
