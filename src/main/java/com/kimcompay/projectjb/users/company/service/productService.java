package com.kimcompay.projectjb.users.company.service;

import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;

import com.kimcompay.projectjb.users.company.model.productDao;
import com.kimcompay.projectjb.users.company.model.productEventDao;
import com.kimcompay.projectjb.users.company.model.productEventVo;
import com.kimcompay.projectjb.users.company.model.productVo;
import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class productService {
    @Autowired
    private productDao productDao;
    @Autowired
    private productEventDao productEventDao;

    public List<productEventVo> getProductEvent(int productId) {
        return productEventDao.findByProductId(productId);
    }
    public List<productVo> getByFlyerId(int flyerId) {
        List<productVo>products=productDao.findByFlyerId(flyerId);
        return products;
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryProductInsertDto tryProductInsertDto) {
        int flyerId=tryProductInsertDto.getFlyerId();
        checkExist(flyerId);
        //상품 insert
        productVo vo=productVo.builder().storeId(tryProductInsertDto.getStoreId()).category(tryProductInsertDto.getCategory()).text(tryProductInsertDto.getText()).flyerId(flyerId).origin(tryProductInsertDto.getOrigin()).price(tryProductInsertDto.getPrice()).productImgPath(tryProductInsertDto.getProductImgPath()).productName(tryProductInsertDto.getProductName()).eventFlag(tryProductInsertDto.getEventFlag()).build();
        productDao.save(vo);
        //이벤트 여부 검사
        if(tryProductInsertDto.getEventFlag()==1){
            List<Map<String,Object>>eventInfors=(List<Map<String,Object>>)tryProductInsertDto.getEventInfors();
            checkEvent(eventInfors);
            for(Map<String,Object>eventInfor:eventInfors){
                productEventVo vo2=productEventVo.builder().date(eventInfor.get("date").toString()).productId(vo.getId()).eventPrice(Integer.parseInt(eventInfor.get("price").toString())).build();
                productEventDao.save(vo2);
            }
        } 
        return utillService.getJson(true, "상품등록에 성공 하였습니다");
    }
    private void checkExist(int flyerId) {
        if(productDao.countFlyerByFlyerId(flyerId)==0){
            throw utillService.makeRuntimeEX("존재하지 않는 전단입니다", "checkExist");
        }
    }
    private boolean checkPrice(int price) {
        if(price<=0){
            return true;
        }
        return false;
    }
    private void checkEvent(List<Map<String,Object>>eventInfors) {
            for(Map<String,Object>eventInfor:eventInfors){
                try {
                    if(checkPrice(Integer.parseInt(eventInfor.get("price").toString()))){
                        throw utillService.makeRuntimeEX("이벤트가격이 0원 보다 작거나 같습니다 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                    }
                } catch (NumberFormatException e) {
                    throw utillService.makeRuntimeEX("가격은 숫자만입렵해주세요 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                }
                
            }    
    }
}
