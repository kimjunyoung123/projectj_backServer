package com.kimcompay.projectjb.users.company;


import java.io.IOException;



import javax.validation.Valid;


import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.delivery.deliveryService;
import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;
import com.kimcompay.projectjb.users.company.model.tryUpdateStoreDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger=LoggerFactory.getLogger(compayAuthRestController.class);

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
    public JSONObject storeInsert(@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto) {
        logger.info("storeInsert controller");
        return storeService.insert(tryInsertStoreDto);
    }
    @RequestMapping(value = "/gets/{page}/{keyword}",method = RequestMethod.GET)//보유매장조회
    public JSONObject getStores(@PathVariable String page,@PathVariable String keyword) {
        logger.info("getStores controller");
        return storeService.getStoresByEmail(page,keyword);
    }
    @RequestMapping(value = "/get/{id}",method = RequestMethod.GET)//매장정보상세조회
    public JSONObject getStore(@PathVariable int id) {
        logger.info("getStore controller");
        return storeService.getStore(id);
    }
    @RequestMapping(value = "/infor/change",method = RequestMethod.PUT)//매장 정보수정
    public JSONObject storeUpdate(@Valid @RequestBody tryUpdateStoreDto tryUpdateStoreDto) {
        logger.info("storeUpdate controller");
        return storeService.tryUpdate(tryUpdateStoreDto);
    }
    @RequestMapping(value = "/infor/sleep/{flag}/{storeId}",method = RequestMethod.PUT)//매장 영업상태 수정
    public JSONObject storeSleepOrOpen(@PathVariable int flag,@PathVariable int storeId) {
        logger.info("storeSleepOrOpen");
        return storeService.updateSleepOrOpen(flag, storeId);
    }
    //전단등록
    @RequestMapping(value = "/uploadAndGet/{storeId}",method = RequestMethod.POST)
    public JSONObject uploadAndOcr(MultipartHttpServletRequest request,@PathVariable int storeId) {
        logger.info("uploadAndOcr");
        return flyerService.ocrAndUpload(request,storeId);
    }
    //상품등록
    @RequestMapping(value = "/flyer/insert",method = RequestMethod.POST)
    public JSONObject insertFlyerAndProducts(@Valid @RequestBody tryProductInsertDto tryProductInsertDto) {
        logger.info("insertFlyerAndProducts controller");
        return productService.insert(tryProductInsertDto);
    }
    @RequestMapping(value = "/gets/deliver/{page}/{storeId}/{startDate}/{endDate}/{state}",method = RequestMethod.GET)//매장 배달 현황 조회
    public JSONObject getDelivers(@PathVariable int page,@PathVariable int storeId,@PathVariable String startDate,@PathVariable String endDate,@PathVariable int state) {
        logger.info("getDelivers controller");
        
        return deliveryService.getDelivers(page,startDate,endDate, storeId,state);
    }
    @RequestMapping(value = "/get/deliver/{roomId}",method = RequestMethod.GET)//매장 배달 디테일 조회
    public JSONObject getDeliverAddress(@PathVariable int roomId) {
        logger.info("getDeliverAddress controller");
        
        return deliveryService.getDeliverAddress(roomId);
    }
}
