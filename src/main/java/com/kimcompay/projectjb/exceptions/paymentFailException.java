package com.kimcompay.projectjb.exceptions;

import com.kimcompay.projectjb.payments.model.pay.settleDto;

import org.json.simple.JSONObject;

public class paymentFailException extends Exception {
    
    private String kind=null;
    private settleDto settleDto=new settleDto();
    private JSONObject kakaoPayDto= new JSONObject();
    private String message=null;
    
    public void setMessage(String message) {
        this.message=message;
    }
    public String getMessage() {
        return this.message;
    }
    public void setKind(String kind) {
     this.kind=kind;   
    }
    public String getKind() {
        return this.kind;
    }
    public void setDto(settleDto settleDto) {
        this.settleDto=settleDto;
    }
    public settleDto getDto() {
        return this.settleDto;
    }
    public void setDto(JSONObject kpayDto) {
        this.kakaoPayDto=kpayDto;
    }
    public JSONObject getKpayDto() {
        return this.kakaoPayDto;
    }
    
}
