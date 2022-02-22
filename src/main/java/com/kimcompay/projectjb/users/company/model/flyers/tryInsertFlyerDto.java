package com.kimcompay.projectjb.users.company.model.flyers;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryInsertFlyerDto {
    @Size(min = 0,message = "업로드한 전단이 없습니다")
    private List<String>flyerImgs;

    @Min(value = 0,message = "디폴트 전단 값이 유효하지 않습니다 관리자에게 문의해주세요")
    @Max(value = 1,message = "디폴트 전단 값이 유효하지 않습니다 관리자에게 문의해주세요")
    private int defaultFlag;

    @NotBlank(message = "대표 이미지를 선택해주세요")
    private String thumbNail;

}
