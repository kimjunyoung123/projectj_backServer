package com.kimcompay.projectjb.users.company;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class tryInsertStoreDto {

    
    @NotBlank(message = "주소가 빈칸입니다")
    private String address;
    @NotBlank(message = "상세주가 빈칸입니다")
    private String detailAddress;
    @NotBlank(message = "우편번호가 빈칸입니다")
    private String postcode;
    @NotBlank(message = "휴대폰번호가 빈칸입니다")
    @Size(min = 10,max = 11 ,message = "휴대폰은 10~11자리입니다")
    private String phone;
    @NotBlank(message = "매장번호가 빈칸입니다")
    private String tel;
    @NotBlank(message = "매장이름이 빈칸입니다")
    private String name;
    @NotBlank(message = "매장사업자번호가 빈칸입니다")
    private String num;
    @NotBlank(message = "썸네일을 선택해주세요")
    private String thumbNail;
    @NotBlank(message = "매장오픈 시간을 선택해주세요")
    private String openTime;
    @NotBlank(message = "매장종료 시간을 선택해주세요")
    private String closeTime;
    @NotBlank(message = "최소배달금액이 빈칸입니다")
    private int minPrice;
    @NotBlank(message = "최대배달반경이 빈칸입니다")
    private int maxRadius;
}
