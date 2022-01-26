package com.kimcompay.projectjb.users.company;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.google.gson.JsonObject;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.users.company.model.tryInsertStoreDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth/store")
public class compayAuthRestController {
    private Logger logger=LoggerFactory.getLogger(compayAuthRestController.class);

    @Autowired
    private storeService storeService;
    

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
    public JSONObject storeUpdate(@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto) {
        logger.info("storeUpdate controller");
        return storeService.tryUpdate(tryInsertStoreDto);
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
}
