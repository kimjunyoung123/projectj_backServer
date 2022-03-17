package com.kimcompay.projectjb.apis.settle;

import com.kimcompay.projectjb.payments.model.card.cardDao;
import com.kimcompay.projectjb.payments.model.card.cardVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class cardService {
    
    @Autowired
    private cardDao cardDao;

    public boolean cancle(settleDto settleDto) {
        
    }
    public void insert(settleDto settleDto) {
        cardDao.save(dtoToVo(settleDto));   
    }
    private cardVo dtoToVo(settleDto settleDto) {
        cardVo vo=cardVo.builder().authNo(settleDto.getAuthNo()).cancleFlag(0).fnCd(settleDto.getFnCd()).fnNm(settleDto.getFnNm()).intMon(settleDto.getIntMon())
                                    .mchtTrdNo(settleDto.getMchtTrdNo()).orgTrdNo(settleDto.getTrdNo()).paymentId(settleDto.getMchtTrdNo()).build();
                                    return vo;
    }

}
