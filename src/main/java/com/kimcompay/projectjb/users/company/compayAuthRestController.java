package com.kimcompay.projectjb.users.company;





import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kimcompay.projectjb.users.company.model.flyers.tryInsertFlyerDto;
import com.kimcompay.projectjb.users.company.model.products.tryProductInsertDto;
import com.kimcompay.projectjb.users.company.model.stores.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.stores.tryUpdateStoreDto;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.delivery.service.deliveryService;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.service.orderService;
import com.kimcompay.projectjb.payments.service.paymentService;
import com.kimcompay.projectjb.users.company.service.flyerService;
import com.kimcompay.projectjb.users.company.service.productService;
import com.kimcompay.projectjb.users.company.service.storeService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping(value = "/auth/store")
public class compayAuthRestController {

    @Autowired
    private storeService storeService;
    @Autowired
    private deliveryService deliveryService;
    @Autowired
    private flyerService flyerService;
    @Autowired
    private productService productService;
    @Autowired
    private paymentService paymentService;
    @Autowired
    private orderService orderService;



    //매장등록
    @RequestMapping(value = "/join",method = RequestMethod.POST)
    public JSONObject storeInsert(@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto,HttpServletRequest request) {
        return storeService.insert(tryInsertStoreDto);
    }
    //매장,배달리스트 조화
    @RequestMapping(value = "/gets/{kind}/{page}/{keyword}",method = RequestMethod.GET)
    public JSONObject getsActionHub(@PathVariable int page,@PathVariable String keyword,@PathVariable String kind) {
        if(kind.equals("stores")){
            return storeService.getStores(page, keyword);
        }else if(kind.equals("deliver")){
            HttpServletRequest request=utillService.getHttpServletRequest();
            return deliveryService.getDelivers(page,keyword,Integer.parseInt(request.getParameter("storeId")), Integer.parseInt(request.getParameter("state")));
        }else if(kind.equals("flyers")){
            HttpServletRequest request=utillService.getHttpServletRequest();
            return flyerService.getFlyers(Integer.parseInt(request.getParameter("storeId")), page, keyword);
        }else {
            throw utillService.makeRuntimeEX("잘못된요청입니다", "authGetsActionHub");
        }
    }
    //매장,배달 디테일 정보조회
    @RequestMapping(value = "/get/{kind}/{id}",method = RequestMethod.GET)
    public JSONObject getActionHub(@PathVariable int id,@PathVariable String kind) {
        if(kind.equals("store")){
            return storeService.getStore(id);//안에서 정보로 검사하니까 aop에서 주인확인 안해도됨
        }else if(kind.equals("deliver")){
            return deliveryService.getDeliverAddress(id);
        }else if(kind.equals("flyer")){
            return flyerService.getFlyerAndProducts(id);
        }else if(kind.equals("product")){
            return productService.getProductAndEvents(id);
        }else{
            throw utillService.makeRuntimeEX("잘못된요청입니다", "authGetsActionHub");
        }
    }
    //매장 정보수정 주인검사는 안에서 하므로 aop에서 하지않는다
    @RequestMapping(value = "/infor/change",method = RequestMethod.PUT)
    public JSONObject storeUpdate(@Valid @RequestBody tryUpdateStoreDto tryUpdateStoreDto,HttpServletRequest request) {
        return storeService.tryUpdate(tryUpdateStoreDto);
    }
    //매장 영업상태 수정 주인검사는 안에서 하므로 aop에서 하지않는다
    @RequestMapping(value = "/infor/sleep/{flag}/{storeId}",method = RequestMethod.PUT)
    public JSONObject storeSleepOrOpen(@PathVariable int flag,@PathVariable int storeId) {
        return storeService.updateSleepOrOpen(flag, storeId);
    }
    //전단업로드 글자 추출
    @RequestMapping(value = "/uploadAndGet/{storeId}",method = RequestMethod.POST)
    public JSONObject uploadAndOcr(MultipartHttpServletRequest request,@PathVariable int storeId) {
        return flyerService.ocrAndUpload(request,storeId);
    }
    //전단등록
    @RequestMapping(value = "/flyer/insert/{storeId}",method = RequestMethod.POST)
    public JSONObject tryInsertFlyer(@PathVariable int storeId,HttpServletRequest httpServletRequest,@Valid @RequestBody tryInsertFlyerDto tryInsertFlyerDto) {
        return flyerService.insert(tryInsertFlyerDto, storeId);
    }
    //전단수정
    @RequestMapping(value = "/flyer/update/{flyerId}/{storeId}",method = RequestMethod.PUT)
    public JSONObject tryUpdateFlyer(@PathVariable int storeId,@PathVariable int flyerId,@Valid @RequestBody tryInsertFlyerDto tryInsertFlyerDto,HttpServletRequest request) {
        return flyerService.tryUpdate(flyerId, tryInsertFlyerDto,storeId);
    }
    //상품등록
    @RequestMapping(value = "/product/insert/{storeId}",method = RequestMethod.POST)
    public JSONObject tryInsertProduct(@Valid @RequestBody tryProductInsertDto tryProductInsertDto,HttpServletRequest request,@PathVariable int storeId) {
        return productService.insert(tryProductInsertDto);
    }
    //상품수정
    @RequestMapping(value = "/product/update/{productId}/{storeId}",method = RequestMethod.PUT)
    public JSONObject updateProductController(@Valid @RequestBody tryProductInsertDto productInsertDto,@PathVariable int productId,HttpServletRequest httpServletRequest,@PathVariable int storeId) {
        return productService.tryUpdate(productId, productInsertDto);
    }
    //상품,전단삭제
    @RequestMapping(value = "/{kind}/{storeId}/{targetId}",method = RequestMethod.DELETE)
    public JSONObject deleteActionHub(@PathVariable String kind,@PathVariable int storeId,@PathVariable int targetId) {
        String message=null;
        if(kind.equals("product")){
            message=productService.deleteWithEvents(targetId);
        }else if(kind.equals("flyerDetail")){
            message=flyerService.tryDeleteDetail(targetId);
        }else if(kind.equals("flyer")){
            message=flyerService.deleteWithAll(targetId);
        }else{
            return utillService.getJson(false, "잘못된 요청입니다");
        }
        return utillService.getJson(true, message);
    }
    //주문내역 조회
    @RequestMapping(value = "/orders/{storeId}/{page}/{keyword}",method = RequestMethod.GET )
    public JSONObject getOrders(@PathVariable int storeId,@PathVariable int page,@PathVariable String keyword) {
        return paymentService.getPaymentsByStoreId(page, keyword, keyword, storeId);
    }
    //주문내역 상세 조회 
    @RequestMapping(value = "/order/{storeId}/{mcht_trd_no}",method = RequestMethod.GET )
    public JSONObject getOrder(@PathVariable int storeId,@PathVariable String mcht_trd_no) {
        return orderService.getOrders(mcht_trd_no, storeId);
    }
    //장보기 상태변환
    @RequestMapping(value = "/order/state/{storeId}/{mcht_trd_no}/{state}",method = RequestMethod.PUT )
    public JSONObject changeOrderState(@PathVariable int storeId,@PathVariable String mcht_trd_no,@PathVariable int state) {
        return orderService.changeOrderState(mcht_trd_no, storeId, state);
    }
    //매장 전용 환불 
    @RequestMapping(value = "/order/cancle/{storeId}/{orderId}",method = RequestMethod.PUT)
    public JSONObject canclePayment(@PathVariable int storeId,@PathVariable int orderId) {
        return paymentService.cancleByStore(orderId,storeId);
    }
}
