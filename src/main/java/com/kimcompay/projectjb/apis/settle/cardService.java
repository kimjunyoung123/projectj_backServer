package com.kimcompay.projectjb.apis.settle;

import com.kimcompay.projectjb.payments.model.card.cardVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;

import org.springframework.stereotype.Service;

@Service
public class cardService {
    
    public void insert(settleDto settleDto) {
        //cardVo vo=cardVo.builder().authNo(settleDto.getAuthNo()).cancleFlag(0).fnCd(settleDto.getFnCd())
    }
}
