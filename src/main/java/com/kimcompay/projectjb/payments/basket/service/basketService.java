package com.kimcompay.projectjb.payments.basket.service;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.basket.model.basketDao;
import com.kimcompay.projectjb.payments.basket.model.basketVo;
import com.kimcompay.projectjb.payments.basket.model.tryInsertDto;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class basketService {
    @Autowired
    private basketDao basketDao;

    public JSONObject tryInsert(tryInsertDto tryInsertDto) {
        basketVo vo=basketVo.builder().count(tryInsertDto.getCount()).productId(tryInsertDto.getProductId()).userId(utillService.getLoginId()).build();
        basketDao.save(vo);
        return utillService.getJson(true, "장바구니에 상품을 담았습니다");
    }
}
