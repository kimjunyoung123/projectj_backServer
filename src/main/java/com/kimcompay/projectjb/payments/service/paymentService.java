package com.kimcompay.projectjb.payments.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import com.kimcompay.projectjb.apis.settle.settleService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.basket.basketDao;
import com.kimcompay.projectjb.payments.model.coupon.couponVo;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentDao;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
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
    @Autowired
    private paymentDao paymentDao;
    @Autowired
    private settleService settleService;
    @Autowired
    private orderDao orderDao;

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public JSONObject tryOrder(tryOrderDto tryOrderDto) {
        int userId=utillService.getLoginId();
        //장바구니 들고오가
        List<Map<String,Object>>basketAndProducts=basketService.getBasketsAndProduct(userId);
        //매장별로 나누기
        Map<Integer,List<Map<String,Object>>>divisionByStoreIds=divisionByStoreId(basketAndProducts);
        Map<String,Object>ordersAndPayment=confrimByStore(divisionByStoreIds,tryOrderDto);
        //insert시도
        paymentVo paymentVo=(paymentVo)ordersAndPayment.get("payment");
        List<orderVo>orders=(List<orderVo>)ordersAndPayment.get("orders");
        paymentDao.save(paymentVo);
        for(orderVo order:orders){
            orderDao.save(order);
        }
        //매장별 조건검증 후 값 만들어서 전송
        if(tryOrderDto.getPayKind().equals("kpay")){
            return null;
        }
        String mchtId="nxca_jt_il";
        //vbank시 추가
        String expire=null;
        if(paymentVo.getMethod().equals("vbank")){
            mchtId="nx_mid_il";
            expire=LocalDateTime.now().toString().replaceAll("[:,T,-]", "").substring(0, 14);
        }
        return settleService.makeRequestPayInfor(ordersAndPayment.get("productNames").toString(),paymentVo,orders,mchtId,expire);
    }
    private Map<String,Object> confrimByStore(Map<Integer,List<Map<String,Object>>>divisionByStoreIds,tryOrderDto tryOrderDto) {
        int userId=utillService.getLoginId();
        String productNames="";
        List<orderVo>orderVos=new ArrayList<>();
        Map<String,Object>orderAndPayment=new HashMap<>();
        //쿠폰 중복확인
        Map<Integer,List<couponVo>>coupons=confrimCoupone(tryOrderDto.getCoupons());
        //할인까지 적용한 전체금액
        int realTotalPrice=0;
        //주문고유번호 생성
        String mchtTrdNo=getMchtTrdNo();
        //주문정보 만들기
        for(Entry<Integer, List<Map<String, Object>>> divisionByStoreId:divisionByStoreIds.entrySet()){
            int storeId=divisionByStoreId.getKey();
            storeVo storeVo=storeService.getVo(storeId);
            //매장 바뀔때 총액 0원으로 해야함
            int totalPrice=0;
            //배달가능 거리 계산 
            if(checkDeliverRadius(storeVo.getSaddress(),storeVo.getDeliverRadius(),tryOrderDto.getAddress())){
                throw utillService.makeRuntimeEX("배달 최대반경을 초과합니다\n매장이름:"+storeVo.getSname()+"\n제품이름:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n최대반경:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }
            //System.out.println("storeId"+storeId);
            //해당 매장 결제요청 총액확인 및 제품검증
            List<Map<String, Object>>basketAndProducts=divisionByStoreId.getValue(); 
            for(Map<String,Object>basketAndProduct:basketAndProducts){ 
                int basketId=Integer.parseInt(basketAndProduct.get("basket_id").toString());
                int productId=Integer.parseInt(basketAndProduct.get("product_id").toString());
                int count=Integer.parseInt(basketAndProduct.get("basket_count").toString());
                int price=0;
                //이벤트 판별후 금액가져오기
                productVo productVo=(productVo)productService.getProduct(productId).get("message");
                price=productVo.getPrice();
                //총액 검사 배달 최소금액 때문에 원래 가격으로 하고 
                totalPrice+=price*count;
                //쿠폰적용 여러장 받기
                String couponName=null;
                if(coupons.get(basketId)!=null){
                    List<couponVo>counponInfors=coupons.get(basketId);
                    //주문 개수 와 쿠폰 개수 비교
                    if(counponInfors.size()>count){
                        throw utillService.makeRuntimeEX("쿠폰개수가 수량을 초과합니다 \n제품이름: "+productVo.getProductName(), "confrimByStore");
                    }
                    List<Integer>discountPrices=new ArrayList<>();
                    couponName="";
                    for(couponVo couponVo:counponInfors){
                        price=productVo.getPrice();
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
                            System.out.println("discountNum:"+discountNum);
                            int totalDiscount=(int)((int)productVo.getPrice()*(0.01*discountNum));
                            System.out.println("discountNum:"+discountNum);
                            price-=totalDiscount;
                        }
                        //0보다 작으면 결제 최소금액 
                        if(price<=0){
                            price=100;
                        }
                        couponName=couponName+couponVo.getName()+",";
                        //System.out.println("discountprice: "+price);
                        discountPrices.add(price);
                    }
                    //가격 계산
                    price=productVo.getPrice();
                    //System.out.println("productCount: "+count);
                    //System.out.println("discountPrices: "+discountPrices);
                    price=price*(count-discountPrices.size());//쿠폰 적용 개수만큼 마이너스
                    //System.out.println("originprice: "+price);
                    for(int discountPrice:discountPrices){//쿠폰 적용 가격들 더해주기
                        System.out.println("discountPrice"+discountPrice);
                        price+=discountPrice;
                    }
                }else{
                    price*=count;
                }
                //System.out.println("basketId:"+basketId);
                //System.out.println(price);
                orderVo vo=orderVo.builder().cancleFlag(0).mchtTrdNo(mchtTrdNo).coupon(couponName).price(price).productId(productVo.getId())
                            .basketId(basketId).storeId(storeId).userId(userId).build();
                //System.out.println(vo.toString());
                realTotalPrice+=price;
                orderVos.add(vo);
                productNames=productNames+productVo.getProductName()+",";
            }
            //총액 검사
            if(storeVo.getMinPrice()>totalPrice){
                throw utillService.makeRuntimeEX("매장 배달 최소금액 미달입니다 \n 매장이름: "+storeVo.getSname(),"confrimByStore");
            }
        }
        //결제방식,품절시 대체 행동 검사
        String method=tryOrderDto.getPayKind();
        checkPayKind(method);
        String soldOutAction=tryOrderDto.getSoldOut();
        checkSoldOutAction(soldOutAction);
        paymentVo vo=paymentVo.builder().cancleAllFlag(0).cnclOrd(0).mchtTrdNo(mchtTrdNo).method(method).soldOurAction(soldOutAction)
                    .totalPrice(realTotalPrice).userId(userId).address(tryOrderDto.getAddress()).postcode(tryOrderDto.getPostcode()).detailAddress(tryOrderDto.getDetailAddress()).build();
        //System.out.println(vo.toString());
        orderAndPayment.put("orders", orderVos);
        orderAndPayment.put("payment", vo);
        orderAndPayment.put("productNames", productNames);
        //System.out.println(orderAndPayment.toString());
        return orderAndPayment;
    }
    private void checkSoldOutAction(String soldOutAction) {
        if(!soldOutAction.equals("replace")&&!soldOutAction.equals("cancle")&&!soldOutAction.equals("contact")){
            throw utillService.makeRuntimeEX("지원하지 않는 품절시 요청입니다", "checkSoldOutAction");
        }
    }
    private void checkPayKind(String payKind) {
        if(!payKind.equals("card")&&!payKind.equals("vbank")&&!payKind.equals("kpay")){
            throw utillService.makeRuntimeEX("지원하지 않는 결제 방식입니다", "checkPayKind");
        }
    }
    private String getMchtTrdNo() {
        //같은 고유값이 존재한는지
        while(true){
            String mchtTrdNo=utillService.getRandomNum(10);
            if(!paymentDao.existsByMchtTrdNo(mchtTrdNo)){
                return  mchtTrdNo;
            }
        }
    }
    private Map<Integer,List<couponVo>> confrimCoupone(List<Map<String,Object>>coupons) {
        Map<Integer,List<couponVo>>couponInfors=new HashMap<>();
        Map<String,String>conponNames=new HashMap<>();
        for(Map<String,Object>coupon:coupons){
            //null이라면 통과
            if(Optional.ofNullable(coupon.get("coupon")).orElseGet(()->null)==null){
                continue;
            }
            // , 로 나눠 쿠폰 한장한장 검사
            String[] couponSplit=coupon.get("coupon").toString().split(",");
            List<couponVo>couponAll=new ArrayList<>();
            for(String couponName:couponSplit){
                if(conponNames.containsValue(couponName)){
                    throw utillService.makeRuntimeEX("중복 쿠폰 발견:"+couponName, "confrimByStore");
                }
                couponVo couponVo=couponService.CheckAndGet(couponName);
                couponAll.add(couponVo);
                conponNames.put(couponName, couponName);
            }
            couponInfors.put(Integer.parseInt(coupon.get("id").toString()),couponAll);
        }
        //System.out.println("--------------------");
        //System.out.println(couponInfors.toString());
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
    public boolean checkDeliverRadius(String storeAddress,double storeRadius,String userAddress) {
        double result=getWay(storeAddress,userAddress);
        System.out.println(result);
        if(storeRadius<result){
            return true;
        }
        return false;
    }
    private double getWay(String storeAddress,String userAddress) {
        List<LinkedHashMap<String,Object>> userAddressInfor=kakaoMapService.checkAddress(userAddress);
        List<LinkedHashMap<String,Object>> storeAddressInfor=kakaoMapService.checkAddress(storeAddress);
        double userLat=Double.parseDouble(userAddressInfor.get(0).get("y").toString());
        double userLon=Double.parseDouble(userAddressInfor.get(0).get("x").toString());
        double storeLat=Double.parseDouble(storeAddressInfor.get(0).get("y").toString());
        double storeLon=Double.parseDouble(storeAddressInfor.get(0).get("x").toString());
        return utillService.distance(userLat, userLon, storeLat, storeLon); 
    }
    
}
