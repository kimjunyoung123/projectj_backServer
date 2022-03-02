package com.kimcompay.projectjb.payments.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.print.DocFlavor.STRING;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.coupon.couponVo;
import com.kimcompay.projectjb.payments.model.pay.tryOrderDto;
import com.kimcompay.projectjb.users.company.model.products.productVo;
import com.kimcompay.projectjb.users.company.model.stores.storeVo;
import com.kimcompay.projectjb.users.company.service.productService;
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
    @Autowired
    private productService productService;

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
        Map<Integer,couponVo>coupons=confrimCoupone(tryOrderDto.getCoupons());
        //주문정보 만들기
        for(Entry<Integer, List<Map<String, Object>>> divisionByStoreId:divisionByStoreIds.entrySet()){
            int storeId=divisionByStoreId.getKey();
            storeVo storeVo=storeService.getVo(storeId);
            //매장 바뀔때 총액 0원으로 해야함
            int totalPrice=0;
            //배달가능 거리 계산 
            if(checkDeliverRadius(storeVo.getSaddress(),storeVo.getDeliverRadius())){
                throw utillService.makeRuntimeEX("배달 최대반경을 초과합니다\n매장이름:"+storeVo.getSname()+"\n제품이름:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n최대반경:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }
            System.out.println("storeId"+storeId);
            //해당 매장 결제요청 총액확인 및 제품검증
            List<Map<String, Object>>basketAndProducts=divisionByStoreId.getValue(); 
            for(Map<String,Object>basketAndProduct:basketAndProducts){
                int basketId=Integer.parseInt(basketAndProduct.get("basket_id").toString());
                int productId=Integer.parseInt(basketAndProduct.get("product_id").toString());
                int count=Integer.parseInt(basketAndProduct.get("basket_count").toString());
                int price=0;
                //이벤트 판별후 금액가져오기
                productVo productVo=(productVo)productService.getProduct(productId).get("message");
                price=productVo.getPrice()*count;
                //총액 검사 배달 최소금액 때문에
                totalPrice+=price;
                //쿠폰적용 여러장 받기로 다시 만들어야하나... 나중에 일단 이렇게 만들자 
                if(coupons.get(basketId)!=null){
                    couponVo couponVo=coupons.get(basketId);
                    //쿠폰 사용 매장 검사
                    if(couponVo.getStoreId()!=storeId){
                        throw utillService.makeRuntimeEX("선택 상품 매장 쿠폰이 아닙니다 \n쿠폰이름:"+couponVo.getName(), "confrimByStore");
                    }
                    int action=couponVo.getKind();
                    int discountNum=couponVo.getNum();
                    //집가서 enum으로 교체
                    if(action==0){
                        price-=discountNum;
                    }else if(action==1){
                        //너무 개이득 구조아니냐....
                        int totalDiscount=(int)((int)productVo.getPrice()*(0.01*discountNum));
                        price-=totalDiscount*count;
                    }
                    //0보다 작으면 결제 최소금액 
                    if(price<=0){
                        price=100;
                    }
                    System.out.println(coupons.get(basketId));
                }
                System.out.println("basketId:"+basketId);
                System.out.println(price);
            }
            //총액 검사
            if(storeVo.getMinPrice()>totalPrice){
                throw utillService.makeRuntimeEX("매장 배달 최소금액 미달입니다 \n 매장이름: "+storeVo.getSname(),"confrimByStore");
            }
        }
    }
    private Map<Integer,couponVo> confrimCoupone(List<Map<String,Object>>coupons) {
        Map<Integer,couponVo>couponInfors=new HashMap<>();
        Map<Integer,String>conponNames=new HashMap<>();
        for(Map<String,Object>coupon:coupons){
            //null이라면 통과
            if(Optional.ofNullable(coupon.get("coupon")).orElseGet(()->null)==null){
                continue;
            }
            String couponName=coupon.get("coupon").toString();
            if(conponNames.containsValue(couponName)){
                throw utillService.makeRuntimeEX("중복 쿠폰 발견:"+couponName, "confrimByStore");
            }
            couponVo couponVo=couponService.CheckAndGet(couponName);
            couponInfors.put(Integer.parseInt(coupon.get("id").toString()),couponVo);
        }
        return couponInfors;
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
