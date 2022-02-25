package com.kimcompay.projectjb.users.company.model.stores;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class tryUpdateStoreDto {

    
    @NotBlank(message = "주소가 빈칸입니다")
    private String address;
    @NotBlank(message = "상세주가 빈칸입니다")
    private String detailAddress;
    @NotBlank(message = "우편번호가 빈칸입니다")
    private String postcode;
    @NotBlank(message = "휴대폰번호가 빈칸입니다")
    @Size(min = 10,max = 11 ,message = "휴대폰은 10~11자리입니다")
    private String phone;
    @NotBlank(message = "매장전화번호가 빈칸입니다")
    private String tel;
    @NotBlank(message = "매장이름이 빈칸입니다")
    private String storeName;
    @NotBlank(message = "매장사업자번호가 빈칸입니다")
    private String num;
    @NotBlank(message = "썸네일을 선택해주세요")
    private String thumbNail;
    @NotBlank(message = "매장오픈 시간을 선택해주세요")
    private String openTime;
    @NotBlank(message = "매장종료 시간을 선택해주세요")
    private String closeTime;
    @Min(value = 0,message = "최소배달금액은 0원 입니다")
    private int minPrice;
    @Min(value = 0,message = "최대배달반경은 0보다 커야합니다")
    private int deliverRadius;
    @NotBlank(message = "간단한 가게 설명을 적어주세요")
    private String text;
    
    private int id;
    private String roadAddress;

}
