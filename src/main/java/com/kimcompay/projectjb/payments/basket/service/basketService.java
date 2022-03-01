package com.kimcompay.projectjb.payments.basket.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.basket.model.basketDao;
import com.kimcompay.projectjb.payments.basket.model.basketVo;
import com.kimcompay.projectjb.payments.basket.model.tryInsertDto;
import com.kimcompay.projectjb.users.company.model.products.productVo;
import com.kimcompay.projectjb.users.company.service.productService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class basketService {
    private final int pageSize=2;

    @Autowired
    private basketDao basketDao;
    @Autowired
    private productService productService;

    public JSONObject tryInsert(tryInsertDto tryInsertDto) {
        basketVo vo=basketVo.builder().count(tryInsertDto.getCount()).productId(tryInsertDto.getProductId()).userId(utillService.getLoginId()).build();
        basketDao.save(vo);
        return utillService.getJson(true, "장바구니에 상품을 담았습니다");
    }
    public JSONObject getBaskets(int page) {
        int userId=utillService.getLoginId();
        List<Map<String,Object>>baskets=basketDao.findByUserId(userId,userId, utillService.getStart(page, pageSize)-1, pageSize);
        utillService.checkDaoResult(baskets, "불러올 제품이 없습니다", "getBaskets");
        //제품 이벤트 판단후 가격 계산
        int size=baskets.size();
        for(int i=0;i<size;i++){
            Map<String,Object>basket=new HashMap<>();
            productVo prouctVo=(productVo)productService.getProduct(Integer.parseInt(baskets.get(i).get("product_id").toString())).get("message");
            basket.put("price", prouctVo.getPrice());
            basket.put("product_name", prouctVo.getProductName());
            basket.put("product_img_path", prouctVo.getProductImgPath());
            baskets.set(i, basket);
        }
        JSONObject response=new JSONObject();
        response.put("baskets", baskets);
        response.put("totalPage", utillService.getTotalPage(Integer.parseInt(baskets.get(0).get("totalCount").toString()), pageSize));
        response.put("flag", true);
        return response;    
    }
}
