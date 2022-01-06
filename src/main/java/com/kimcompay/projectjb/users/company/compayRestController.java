package com.kimcompay.projectjb.users.company;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
public class compayRestController {
    private Logger logger=LoggerFactory.getLogger(compayRestController.class);

    @Autowired
    private storeService storeService;

    //매장등록
    @RequestMapping(value = "/auth/store/join",method = RequestMethod.POST)
    public JSONObject storeInsert(@Valid @RequestBody tryInsertStoreDto tryInsertStoreDto) {
        logger.info("storeInsert");
        return storeService.insert(tryInsertStoreDto);
    }
    @RequestMapping(value = "/auth/store/gets/{page}/{keyword}",method = RequestMethod.GET)
    public JSONObject getStores(@PathVariable String page,@PathVariable String keyword) {
        logger.info("getStores");
        return storeService.getStoresByEmail(page,keyword);
    }
    @RequestMapping(value = "/auth/store/get/{id}",method = RequestMethod.GET)
    public JSONObject getStore(@PathVariable int id) {
        logger.info("getStore");
        return storeService.getStore(id);
    }
}
