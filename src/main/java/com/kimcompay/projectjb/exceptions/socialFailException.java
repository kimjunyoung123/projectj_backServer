package com.kimcompay.projectjb.exceptions;

import lombok.Getter;

@Getter
public class socialFailException extends Exception {
    private Object body;
    private String company;
    private String action;
    private String message;

    public socialFailException(Object body,String company,String action,String message){
        this.body=body;
        this.company=company;
        this.action=action;
        this.message=message;
    }
}
