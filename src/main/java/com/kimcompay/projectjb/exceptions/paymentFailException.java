package com.kimcompay.projectjb.exceptions;

import com.kimcompay.projectjb.payments.model.pay.settleDto;

import org.json.simple.JSONObject;

public class paymentFailException extends Exception {
    
    private String kind=null;
    //private settleDto settleDto=new settleDto();
    //private JSONObject kakaoPayDto= new JSONObject();
    private String message=null;
    private settleDto dto=new settleDto();

    public paymentFailException(settleDto dto,String kind,String message){
        System.out.println();
        this.dto=dto;
        this.kind=kind;
        this.message=message;
    }

    public String getMessage() {
        return this.message;
    }
    public String getKind() {
        return this.kind;
    }
    public settleDto getDto() {
        return this.dto;
    }
    /*public void setDto(JSONObject kpayDto) {
        this.kakaoPayDto=kpayDto;
    }
    public JSONObject getKpayDto() {
        return this.kakaoPayDto;
    }*/
    
}
