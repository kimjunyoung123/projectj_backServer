package com.kimcompay.projectjb.payments.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.basket.basketVo;
import com.kimcompay.projectjb.payments.model.basket.tryInsertDto;
import com.kimcompay.projectjb.users.company.model.products.productVo;
import com.kimcompay.projectjb.users.company.service.productService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class basketService {
    private final int pageSize=2;

    @Autowired
    private basketDao basketDao;
    @Autowired
    private productService productService;

    public JSONObject tryDelete(int basketId) {
        basketDao.deleteIdAndUserId(basketId,utillService.getLoginId());
        return utillService.getJson(true, "삭제되었습니다");
    }
    @Transactional
    public JSONObject tryUpadte(int basketId,int count) {
        checkCount(count);
        basketVo basketVo=basketDao.findById(basketId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 장바구니 품목입니다", "tryUpadte"));
        utillService.checkOwner(basketVo.getUserId(), "상품 주인이아닙니다");
        basketVo.setCount(count);
        return utillService.getJson(true, ((productVo)productService.getProduct(basketVo.getProductId()).get("message")).getPrice()*count);   
        
    }
    private void checkCount(int count) {
        if(count<=0){
            throw utillService.makeRuntimeEX("수량은 0보다 커야합니다", "tryUpadte");
        }
    }
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
        int totalCount=Integer.parseInt(baskets.get(0).get("totalCount").toString());
        for(int i=0;i<size;i++){
            Map<String,Object>basket=new HashMap<>();
            productVo prouctVo=(productVo)productService.getProduct(Integer.parseInt(baskets.get(i).get("product_id").toString())).get("message");
            int count=Integer.parseInt( baskets.get(i).get("basket_count").toString());
            basket.put("price", prouctVo.getPrice()*count);
            basket.put("product_name", prouctVo.getProductName());
            basket.put("product_img_path", prouctVo.getProductImgPath());
            basket.put("basket_count", count);
            basket.put("id", baskets.get(i).get("basket_id"));
            basket.put("basket_created", baskets.get(i).get("basket_created"));
            baskets.set(i, basket);
        }
        JSONObject response=new JSONObject();
        response.put("baskets", baskets);
        response.put("totalPage", utillService.getTotalPage(totalCount, pageSize));
        response.put("flag", true);
        return response;    
    }
}
