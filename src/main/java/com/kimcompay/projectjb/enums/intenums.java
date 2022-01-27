package com.kimcompay.projectjb.enums;

public enum intenums {
    DONE_FLAG(1),
    NOT_FLAG(0);


    private int s;
    intenums(int s){
        this.s=s;
    }
    public  int get() {
        return s;
    } 
}
