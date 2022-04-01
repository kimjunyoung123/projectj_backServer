package com.kimcompay.projectjb.delivery.model;

import java.util.List;

import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryInsertDto {
    @Size(min = 1,message = "배달방에 추가할 배달을 선택해주세요")
    List<String>mchtTrdNos;
}
