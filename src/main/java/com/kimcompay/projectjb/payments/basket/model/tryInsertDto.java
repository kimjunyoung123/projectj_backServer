package com.kimcompay.projectjb.payments.basket.model;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryInsertDto {

    @Min(value = 1,message = "제품은 최소 1개이상 담아야합니다")
    private int count;

    private int productId;
}
