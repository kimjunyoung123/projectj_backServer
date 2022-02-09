package com.kimcompay.projectjb.users.company;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.google.gson.JsonObject;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.delivery.deliveryService;
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
import org.springframework.web.multipart.MultipartFile;
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
    private fileService fileService;

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
    @RequestMapping(value = "/uploadAndGet",method = RequestMethod.POST)
    public JSONObject uploadAndOcr(MultipartHttpServletRequest request) {
        logger.info("uploadAndOcr");
        JSONObject response=new JSONObject();
        response=fileService.upload(request);
        String imgPath=fileService.uploadLocal(request.getFile("upload"));
        try {
            response.put("ocr",ocrService.detectText(imgPath));
        } catch (Exception e) {
            logger.info("ocr 글자 추출 실패");
            e.printStackTrace();
        }
        return response;
    }
    @RequestMapping(value = "/testimg",method = RequestMethod.POST)
    public JSONObject testimg() {
        logger.info("testimg");
        try {
           return ocrService.detectText();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    @RequestMapping(value = "/gets/deliver/{page}/{storeId}/{startDate}/{endDate}",method = RequestMethod.GET)//매장 배달 현황 조회
    public JSONObject getDelivers(@PathVariable int page,@PathVariable int storeId,@PathVariable String startDate,@PathVariable String endDate) {
        logger.info("getDelivers controller");
        
        return deliveryService.getDelivers(page,startDate,endDate, storeId);
    }
    @RequestMapping(value = "/get/deliver/{roomId}",method = RequestMethod.GET)//매장 배달 디테일 조회
    public JSONObject getDeliverAddress(@PathVariable int roomId) {
        logger.info("getDeliverAddress controller");
        
        return deliveryService.getDeliverAddress(roomId);
    }
}
