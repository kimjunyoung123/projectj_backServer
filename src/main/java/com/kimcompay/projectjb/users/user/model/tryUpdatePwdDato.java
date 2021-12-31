package com.kimcompay.projectjb.users.user;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryUpdatePwdDato {
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4,max = 10,message = "비밀번호는 최소 4 최대 10자리 입니다")
    private String pwd;
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4,max = 10,message = "비밀번호는 최소 4 최대 10자리 입니다")
    private String pwd2;

    @NotBlank(message = "잘못된 요청입니다")
    private String token;
}
