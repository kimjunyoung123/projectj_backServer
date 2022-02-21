package com.kimcompay.projectjb.users.company;





import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;
import com.kimcompay.projectjb.delivery.service.deliveryService;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.tryUpdateStoreDto;
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



    //매장등록
    @RequestMapping(value = "/join",method = RequestMethod.POST)
    public JSONObject storeInsert(@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto,HttpServletRequest request) {
        return storeService.insert(tryInsertStoreDto);
    }
    //매장,배달리스트 조화
    @RequestMapping(value = "/gets/{kind}/{page}/{keyword}",method = RequestMethod.GET)
    public JSONObject getStores(@PathVariable int page,@PathVariable String keyword,@PathVariable String kind) {
        return storeService.authGetsActionHub(kind,page,keyword);
    }
    //매장,배달 디테일 정보조회
    @RequestMapping(value = "/get/{kind}/{id}",method = RequestMethod.GET)
    public JSONObject getStore(@PathVariable int id,@PathVariable String kind) {
        return storeService.authGetActionHub(kind,id);
    }
    //매장 정보수정
    @RequestMapping(value = "/infor/change",method = RequestMethod.PUT)
    public JSONObject storeUpdate(@Valid @RequestBody tryUpdateStoreDto tryUpdateStoreDto,HttpServletRequest request) {
        return storeService.tryUpdate(tryUpdateStoreDto);
    }
    //매장 영업상태 수정
    @RequestMapping(value = "/infor/sleep/{flag}/{storeId}",method = RequestMethod.PUT)
    public JSONObject storeSleepOrOpen(@PathVariable int flag,@PathVariable int storeId) {
        return storeService.updateSleepOrOpen(flag, storeId);
    }
    //전단등록
    @RequestMapping(value = "/uploadAndGet/{storeId}",method = RequestMethod.POST)
    public JSONObject uploadAndOcr(MultipartHttpServletRequest request,@PathVariable int storeId) {
        return flyerService.ocrAndUpload(request,storeId);
    }
    //상품등록
    @RequestMapping(value = "/flyer/insert",method = RequestMethod.POST)
    public JSONObject insertFlyerAndProducts(@Valid @RequestBody tryProductInsertDto tryProductInsertDto,HttpServletRequest request) {
        return productService.insert(tryProductInsertDto);
    }
    //상품수정
    @RequestMapping(value = "/product/update/{productId}",method = RequestMethod.PUT)
    public JSONObject updateProductController(@Valid @RequestBody tryProductInsertDto productInsertDto,@PathVariable int productId) {
        return productService.tryUpdate(productId, productInsertDto);
    }
}
