package com.kimcompay.projectjb.apis.settle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString@Data
public class reAccountDto {
    String mchtId;
    String mchtTrdNo;
    String bankCd;
    int acntType;
    String prdtNm;
    String sellerNm;
    String ordNm;
    int trdAmt;
    String dpstrNm;
    String mchtCustNm;
    String taxTypeCd;
    String escrAgrYn;
    String csrcIssReqYn;
}
