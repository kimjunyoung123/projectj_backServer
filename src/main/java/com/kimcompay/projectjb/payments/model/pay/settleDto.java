package com.kimcompay.projectjb.payments.model.pay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Data
public class settleDto {
    String mchtId;//상점아이디
        String outStatCd;          //결과코드
        String outRsltCd;          //거절코드
        String outRsltMsg;         //결과메세지
        String method;             //결제수단
        String mchtTrdNo;          //상점주문번호
        String mchtCustId;         //상점고객아이디
        String trdNo;                           //세틀뱅크 거래번호
        String trdAmt;                         //거래금액
        String mchtParam;          //상점 예약필드
        String authDt;                         //승인일시
        String authNo;                         //승인번호
        String reqIssueDt;     	       	//채번요청일시
        String intMon;                         //할부개월수
        String fnNm;                             //카드사명
        String fnCd;                             //카드사코드
        String pointTrdNo;                 //포인트거래번호
        String pointTrdAmt;               //포인트거래금액
        String cardTrdAmt;                 //신용카드결제금액
        String vtlAcntNo;          //가상계좌번호
        String expireDt;                     //입금기한
        String cphoneNo;                     //휴대폰번호
        String billKey;    
        String dpstrNm;  
        
        //결제완료시 가맹점 포인트 여부
        int point;
        ///환불요청시 써야하는 변수
        int cnclOrd;

}
