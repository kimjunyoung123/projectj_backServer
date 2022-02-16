package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Data
public class tryProductInsertDto {
    private int storeId;
    @Min(value = 0,message = "가격은 0원이 될 수 없습니다")
    private int price;
    @NotBlank(message = "상품이름을 입력해주세요")
    private String productName;
    @Min(value = 0,message = "이벤트 플래그가 상이합니다 관리자에게 문의해주세요")
    @Max(value = 1,message = "이벤트 플래그가 상이합니다 관리자에게 문의해주세요")
    private int eventFlag;
    //따로 유효성 검사
    private List<Map<String,Object>>eventInfors;
    //null가능
    private String text;
    @NotBlank(message = "카테고리가 누락되었습니다")
    private String category;
    private int flyerId;
    @NotBlank(message = "상품이미지가 누락되었습니다")
    private String productImgPath;
    @NotBlank(message = "원산지를 입력해주세요")
    private String origin;

  

}
