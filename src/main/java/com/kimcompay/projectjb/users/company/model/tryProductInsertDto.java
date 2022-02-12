package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Data
public class tryProductInsertDto {
    private int storeId;
    @NotBlank(message = "가격을 입력해주세요")
    private String price;
    @NotBlank(message = "상품이름을 입력해주세요")
    private String productName;
    private boolean eventFlag;
    //따로 유효성 검사
    private List<Map<String,Object>>eventInfors;
    //null가능
    private String text;
    @NotBlank(message = "카테고리가 누락되었습니다")
    private String category;
    @NotBlank(message = "전단 이름이 누락되었습니다")
    private String flyerName;
    @NotBlank(message = "전단이미지 경로가 누락되었습니다")
    private String flyerPath;
    @NotBlank(message = "상품이미지가 누락되었습니다")
    private String productImgPath;
    @NotBlank(message = "원산지를 입력해주세요")
    private String origin;

}
