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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoMapService;
import com.kimcompay.projectjb.apis.kakao.kakaoPayService;
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
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class paymentService {
    private final int pageSize=2;
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
    private kakaoPayService kakaoPayService;
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;
    @Autowired
    private orderService orderService;

    public List<Map<String,Object>> getPaymentAndOrdersUseDeliver(String mchtTrdNo,int storeId) {
        return paymentDao.findByMchtTrdNoAndStoreIdJoinOrders(storeId, mchtTrdNo);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject cancleByStore(int orderId,int storeId) {
        Map<String,Object>orderAndPayments=new HashMap<>();
        orderAndPayments=paymentDao.findByJoinCardVbankKpayAndPayment(orderId, storeId);
        int cancleAllFlag=Integer.parseInt(orderAndPayments.get("cancle_all_flag").toString());
        int totalPrice=Integer.parseInt(orderAndPayments.get("payment_total_price").toString());
        int minusPrice=Integer.parseInt(orderAndPayments.get("order_price").toString());
        int orderCancleFlag=Integer.parseInt(orderAndPayments.get("oder_cancle_flag").toString());
        totalPrice=totalPrice-minusPrice;
        if(orderAndPayments.isEmpty()){
            throw utillService.makeRuntimeEX("내역이 존재하지 않습니다", "cancleByStore");
        }else if(cancleAllFlag==1||totalPrice<=0||orderCancleFlag==1){
            throw utillService.makeRuntimeEX("이미 전액 환불된 제품입니다", "cancleByStore");
        }else if(totalPrice<0){
            throw utillService.makeRuntimeEX("남은 금액보다 요청금액이 큽니다 \n남은금액: "+(totalPrice+minusPrice)+"\n요청금액: "+minusPrice, "cancleByStore");
        }
        //db수정
        int cancleTime=Integer.parseInt(orderAndPayments.get("cncl_ord").toString())+1;
        String mchtTrdNo=orderAndPayments.get("order_mcht_trd_no").toString();
        updatePirceAndCancleTime(totalPrice, cancleTime, mchtTrdNo);
        orderService.changeStateById(orderId, 1);
        //pg사대응
        String method=orderAndPayments.get("method").toString();
        String  message="";
        if(method.equals(senums.cardText.get())||method.equals(senums.vbankText.get())){
            message=settleService.cancleByStore(orderAndPayments, method);
        }else if(method.equals(senums.kpayText.get())){
            kakaoPayService.cancleKpay(orderAndPayments.get("tid").toString(), minusPrice, 0);
            message="정상처리되었습니다";
        }else{
            throw utillService.makeRuntimeEX("지원하지 않는 결제 수단입니다", "cancleByStore");
        }
        return utillService.getJson(true, message);
    }
    public void updatePirceAndCancleTime(int price,int cancleTime,String mchtTrdNo) {
        if(price<=0){
            paymentDao.updatePriceAndCancleTimeZero(cancleTime, price,1,mchtTrdNo);
        }else{
            paymentDao.updatePriceAndCancleTime(cancleTime, price, mchtTrdNo);
        }
    }
    public JSONObject getPaymentsByStoreId(int page,String start,String end,int storeId) {
        List<Map<String,Object>>paymentVos=getVos(page, start, end, storeId);
        utillService.checkDaoResult(paymentVos, "내역이 존재하지 않습니다", "getPaymentsByStoreId");
        System.out.println(paymentVos.toString());
        int totalPage=utillService.getTotalPage(Integer.parseInt(paymentVos.get(0).get("totalCount").toString()), pageSize); 
        JSONObject response=new JSONObject();
        response.put("flag", true);
        response.put("message", paymentVos);
        response.put("totalPage", totalPage);
        return response;
    }
    private List<Map<String,Object>> getVos(int page,String start,String end,int storeId) {
        return paymentDao.findJoinByStoreId(storeId,storeId,utillService.getStart(page, pageSize)-1, pageSize);
    }
    public JSONObject getPayments(int page,String start,String end) {
        return utillService.getJson(true, getDtosByUserId(page, start, end));
    }
    private List<Map<String,Object>> getDtosByUserId(int page,String start,String end) {
        return paymentDao.findJoinCardVbankKpayOrder(utillService.getLoginId());
    }
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public JSONObject tryOrder(tryOrderDto tryOrderDto,String action) {
        int userId=utillService.getLoginId();
        //장바구니 들고오가
        List<Map<String,Object>>basketAndProducts=new ArrayList<>();
        if(action.equals("all")){
            basketAndProducts=basketService.getBasketsAndProduct(userId);
        }else if(action.equals("choice")){
            List<Map<String,Object>>baskets=tryOrderDto.getCoupons();
            for(Map<String,Object>basket:baskets){
                int basketId=Integer.parseInt(basket.get("id").toString());
                Map<String,Object>basketVo=basketService.getBasketAndProductByBasketId(basketId);
                if(basketVo.isEmpty()){
                    throw utillService.makeRuntimeEX("존재하지 않는 장바구니입니다 \n번호: "+basketId, "tryOrder");
                }else if(Integer.parseInt(basketVo.get("user_id").toString())!=userId){
                    throw utillService.makeRuntimeEX("본인의 장바구니가 아닙니다 \n번호: "+basketId, "tryOrder");
                }
                basketAndProducts.add(basketVo);
            }
        }else{
            throw utillService.makeRuntimeEX("잘못된 장바구니 선택옵션입니다 \n관리자에게 문의 해주세요", "tryOrder");
        }
        //매장별로 나누기
        Map<Integer,List<Map<String,Object>>>divisionByStoreIds=divisionByStoreId(basketAndProducts);
        //요청 검증하기
        Map<String,Object>ordersAndPayment=confrimByStore(divisionByStoreIds,tryOrderDto);
        //insert시도
        paymentVo paymentVo=(paymentVo)ordersAndPayment.get("payment");
        List<orderVo>orders=(List<orderVo>)ordersAndPayment.get("orders");
        redisTemplate.opsForHash().put(paymentVo.getMchtTrdNo(),paymentVo.getMchtTrdNo(), paymentVo);
        redisTemplate.opsForHash().put(paymentVo.getMchtTrdNo()+senums.basketsTextReids.get(),paymentVo.getMchtTrdNo()+senums.basketsTextReids.get(), orders);
       /* LinkedHashMap<String,Object> paymentVo2=(LinkedHashMap)redisTemplate.opsForHash().entries(paymentVo.getMchtTrdNo());
        LinkedHashMap<String,Object> orders2=(LinkedHashMap)redisTemplate.opsForHash().entries(paymentVo.getMchtTrdNo()+senums.basketsTextReids.get());
        System.out.println(paymentVo2.toString());
        System.out.println(orders2.toString());
        /*for(orderVo order:orders){
            String basketId=Integer.toString(order.getBasketId());
            redisTemplate.opsForHash().put(basketId,basketId, order);
        }*/
        //매장별 조건검증 후 값 만들어서 전송
        if(tryOrderDto.getPayKind().equals("kpay")){
            int totalCount=Integer.parseInt(ordersAndPayment.get("totalCount").toString());
            return kakaoPayService.requestPay(ordersAndPayment.get("productNames").toString(), paymentVo, orders, totalCount);
        }
        String mchtId="nxca_jt_il";
        //vbank시 추가
        String expire=null;
        if(paymentVo.getMethod().equals("vbank")){
            mchtId="nx_mid_il";
            expire=LocalDateTime.now().plusMinutes(15).toString().replaceAll("[:,T,-]", "").substring(0, 14);
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
        int totalCount=0;
        //주문고유번호 생성
        String mchtTrdNo=getMchtTrdNo();
        //주문정보 만들기
        for(Entry<Integer, List<Map<String, Object>>> divisionByStoreId:divisionByStoreIds.entrySet()){
            int storeId=divisionByStoreId.getKey();
            storeVo storeVo=storeService.getVo(storeId);
            //매장 바뀔때 총액 0원으로 해야함
            int totalPrice=0;
            LocalDateTime now=LocalDateTime.now();
            String nows=now.toString().split("T")[0];
            Timestamp openTime=Timestamp.valueOf(nows+" "+storeVo.getOpenTime()+":00");
            Timestamp closeTime=Timestamp.valueOf(nows+" "+storeVo.getCloseTime()+":00");
            System.out.println("오픈타임: "+openTime);
            //배달가능 거리 계산,영업시간 검증   
            if(checkDeliverRadius(storeVo.getSaddress(),storeVo.getDeliverRadius(),tryOrderDto.getAddress())){
                throw utillService.makeRuntimeEX("배달 최대반경을 초과합니다\n매장이름:"+storeVo.getSname()+"\n제품이름:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n최대반경:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
            }else if(LocalDateTime.now().isAfter(closeTime.toLocalDateTime())||LocalDateTime.now().isBefore(openTime.toLocalDateTime())){
                throw utillService.makeRuntimeEX("매장엽업 시간이 아닙니다\n매장이름:"+storeVo.getSname()+"\n제품이름:"+divisionByStoreId.getValue().get(0).get("product_name")+"\n최대반경:"+storeVo.getDeliverRadius()+"km", "checkDeliverRadius");
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
                totalCount+=count;
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
                            .basketId(basketId).count(count).storeId(storeId).userId(userId).build();
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
        orderAndPayment.put("totalCount", totalCount);
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
