package com.kimcompay.projectjb.users.company;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.users.company.model.flyerDao;
import com.kimcompay.projectjb.users.company.model.flyerVo;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class flyerService {
    private Logger logger=LoggerFactory.getLogger(flyerService.class);
    private final int pageSize=2;
    @Autowired
    private flyerDao flyerDao;
    @Autowired
    private fileService fileService;
    
    public JSONObject getFlyerAndProducts(int flyerId) {
        logger.info("getFlyerAndProducts");
        Map<String,Object>infors=getFlyerJoinProductAndEvent(flyerId);
        if(infors.isEmpty()){
            throw utillService.makeRuntimeEX("등록된 상품,전단,이벤트가 없습니다", "getFlyerAndProducts");
        }
        return utillService.getJson(true, infors);
    }
    private Map<String,Object> getFlyerJoinProductAndEvent(int flyerId) {
        logger.info("getFlyerJoinProductAndEvent");
        return flyerDao.findByFlyerJoinProductAndEvent(flyerId);
    }
    public List<Map<String,Object>> getFlyerArr(int storeId,String startDate,String endDate,int page) {
        logger.info("getByStoreId");
        Map<String,Object>result=utillService.checkRequestDate(startDate, endDate);
        if((boolean)result.get("flag")){
            Timestamp start=Timestamp.valueOf(result.get("start").toString());
            Timestamp end=Timestamp.valueOf(result.get("end").toString());
            return flyerDao.findByDay(start, end, storeId,start, end, storeId,utillService.getStart(page, pageSize)-1,pageSize);

        }
        return flyerDao.findByStoreId(storeId,storeId,page,pageSize);
    }
    public void checkExists(int flyerId) {
        if(!flyerDao.existsById(flyerId)){
            throw utillService.makeRuntimeEX("존재하지 않는 전단입니다", "checkExists");
        }
    }
    public flyerVo getFlyer(int flyerId) {
        logger.info("getFlyer");
        return flyerDao.findById(flyerId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 전단입니다", "getFlyer"));
    }
    private int insert(String imgPath,int storeId) {
        flyerVo vo=flyerVo.builder().img_path(imgPath).defaultSelect(0).storeId(storeId).companyId(utillService.getLoginId()).build();
        flyerDao.save(vo);
        return vo.getFid();
    }
    public JSONObject ocrAndUpload(MultipartHttpServletRequest request,int storeId) {
        JSONObject response=new JSONObject();
        File file=fileService.convert(request.getFile("upload"));
        //aws upload
        response=fileService.upload(file);
        //글자추출
        try {
            response.put("ocr",ocrService.detectText(file.toPath().toString()));
        } catch (Exception e) {
            logger.info("ocr 글자 추출 실패");
            e.printStackTrace();
        }
        //로컬 파일 삭제
        file.delete();
        //전단지 insert 
        response.put("id",insert(response.get("message").toString(),storeId));
        return response;
    }
}
