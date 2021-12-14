package com.kimcompay.projectjb.enums;

public enum senums {
    
    auth("0"),
    find("1"),
    persnal("persnal"),
    buyer("buyer"),
    seller("seller");
    
    private String s;
    senums(String s){
        this.s=s;
    }
    public  String get() {
        return s;
    }
}
