package com.kimcompay.projectjb.payments.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.pay.tryOrderDto;
import com.kimcompay.projectjb.users.company.model.stores.storeVo;
import com.kimcompay.projectjb.users.company.service.storeService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class paymentService {

    @Autowired
    private basketService basketService;
    @Autowired
    private kakaoMapService kakaoMapService;
    @Autowired
    private storeService storeService;
    @Autowired
    private couponService couponService;

    public JSONObject tryOrder(tryOrderDto tryOrderDto) {
        int userId=utillService.getLoginId();
        //장바구니 들고오가
        List<Map<String,Object>>basketAndProducts=basketService.getBasketsAndProduct(userId);
        //매장별로 나누기
        Map<Integer,List<Map<String,Object>>>divisionByStoreIds=divisionByStoreId(basketAndProducts);
        //매장별 조건검증
        confrimByStore(divisionByStoreIds,tryOrderDto);
        JSONObject respons=new JSONObject();
        respons.put("price", aes256.encrypt( "1000"));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText("nxca_jt_il", "card", "1234567", "20220301", "153500", "1000")));
        respons.put("flag", true);
        return respons;
    }
    private void confrimByStore(Map<Integer,List<Map<String,Object>>>divisionByStoreIds,tryOrderDto tryOrderDto) {
        //쿠폰 중복확인
        Map<Integer,Object>coupons=confrimCoupone(tryOrderDto.getCoupons());
        //주문정보 만들기
        for(Entry<Integer, List<Map<String, Object>>> divisionByStoreId:divisionByStoreIds.entrySet()){
            int storeId=divisionByStoreId.getKey();
            storeVo storeVo=storeService.getVo(storeId);
            //배달가능 거리 계산 
            if(checkDeliverRadius(storeVo.getSaddress(),storeVo.getDeliverRadius())){
                throw utillService.makeRuntimeEX("배달 최대반경을 초과합니다\n매장이름:"+storeVo.getSname()+"\n제품이름:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n최대반경:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }
            //해당 매장 결제요청 총액확인 및 제품/쿠폰 검증
            List<Map<String, Object>>basketAndProducts=divisionByStoreId.getValue(); 
            for(Map<String,Object>basketAndProduct:basketAndProducts){
                System.out.println(basketAndProduct.get("basket_id"));
            }
        }
    }
    private Map<Integer,Object> confrimCoupone(List<Map<String,Object>>coupons) {
        Map<Integer,Object>conponNames=new HashMap<>();
        for(Map<String,Object>coupon:coupons){
            //null이라면 통과
            if(Optional.ofNullable(coupon.get("coupon")).orElseGet(()->null)==null){
                continue;
            }
            String couponName=coupon.get("coupon").toString();
            if(conponNames.containsValue(couponName)){
                throw utillService.makeRuntimeEX("중복 쿠폰 발견:"+couponName, "confrimByStore");
            }
            couponService.checkExist(couponName);
            conponNames.put(Integer.parseInt(coupon.get("id").toString()),couponName);
        }
        return conponNames;
    }
    private Map<Integer,List<Map<String,Object>>> divisionByStoreId(List<Map<String,Object>>basketAndProducts) {
        Map<Integer,List<Map<String,Object>>>divisionByStoreId=new HashMap<>();
        for(Map<String,Object>basket:basketAndProducts){
            //System.out.println(basket.get("product_id"));
            int storeId=Integer.parseInt(basket.get("store_id").toString());
            //System.out.println("s:"+storeId);
            if(storeId==0){
                throw utillService.makeRuntimeEX("잘못된 상품이있습니다 \n"+basket.get("product_name"), "divisionByStoreId");
            }else if(divisionByStoreId.containsKey(storeId)){
                divisionByStoreId.get(storeId).add(basket);
            }else{
                List<Map<String,Object>>basketAndProduct=new ArrayList<>();
                basketAndProduct.add(basket);
                divisionByStoreId.put(storeId, basketAndProduct);
            }
        }
        return divisionByStoreId;
    }
    public boolean checkDeliverRadius(String storeAddress,double storeRadius) {
        double result=getWay(storeAddress);
        System.out.println(result);
        if(storeRadius<result){
            return true;
        }
        return false;
    }
    private double getWay(String storeAddress) {
        List<LinkedHashMap<String,Object>> userAddressInfor=kakaoMapService.checkAddress(utillService.getLoginInfor().get("address").toString());
        List<LinkedHashMap<String,Object>> storeAddressInfor=kakaoMapService.checkAddress(storeAddress);
        double userLat=Double.parseDouble(userAddressInfor.get(0).get("y").toString());
        double userLon=Double.parseDouble(userAddressInfor.get(0).get("x").toString());
        double storeLat=Double.parseDouble(storeAddressInfor.get(0).get("y").toString());
        double storeLon=Double.parseDouble(storeAddressInfor.get(0).get("x").toString());
        return utillService.distance(userLat, userLon, storeLat, storeLon); 
    }
    
}
