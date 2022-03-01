package com.kimcompay.projectjb.payments.model.pay;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryOrderDto {

    @Size(min = 1,message = "구매하실 제품을 선택해주세요")
    private List<Map<String,Object>>coupons;

    @NotBlank(message = "결제수단을 선택해주세요")
    private String payKind;    

    @NotBlank(message = "품절시 요청을 선택해주세요")
    private String soldOut;  
}
