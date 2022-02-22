package com.kimcompay.projectjb.users.company.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.board.service.boardService;
import com.kimcompay.projectjb.users.company.model.productDao;
import com.kimcompay.projectjb.users.company.model.productEventDao;
import com.kimcompay.projectjb.users.company.model.productEventVo;
import com.kimcompay.projectjb.users.company.model.productVo;
import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class productService {
    @Autowired
    private productDao productDao;
    @Autowired
    private productEventDao productEventDao;
    @Autowired
    private fileService fileService;
    @Autowired
    private boardService boardService;

    @Transactional(rollbackFor = Exception.class)
    public JSONObject tryUpdate(int productId,tryProductInsertDto tryProductInsertDto) {
        Map<String,Object>product=getProductInArr(getProductAndEvenArr(productId));
        Boolean changeFlag=false;
        //이번엔 update문으로 해보자 좀 노가다 이긴한데
        //제품이름변경
        if(!product.get("product_name").equals(tryProductInsertDto.getProductName())){
            changeFlag=true;
            utillService.writeLog("상품이름 변경요청", productService.class);
            productDao.updateProductNameById(tryProductInsertDto.getProductName(), productId);
        }
        //제품가격변경
        if(Integer.parseInt(product.get("price").toString())!=tryProductInsertDto.getPrice()){
            changeFlag=true;
            utillService.writeLog("상품가격 변경요청", productService.class);
            productDao.updatePriceById(tryProductInsertDto.getPrice(), productId);
        }
        //제품원산지변경
        if(!product.get("origin").equals(tryProductInsertDto.getOrigin())){
            changeFlag=true;
            utillService.writeLog("상품원산지 변경요청", productService.class);
            productDao.updateOriginById(tryProductInsertDto.getOrigin(), productId);
        }
        //제품사진변경
        if(!product.get("product_img_path").equals(tryProductInsertDto.getProductImgPath())){
            changeFlag=true;
            utillService.writeLog("상품사진 변경요청", productService.class);
            productDao.updateImgPathById(tryProductInsertDto.getProductImgPath(), productId);
            fileService.deleteFile(utillService.getImgNameInPath(product.get("product_img_path").toString(), 4));
        }
        //제품설명변경
        if(!product.get("text").equals(tryProductInsertDto.getText())){
            changeFlag=true;
            utillService.writeLog("상품설명 변경요청", productService.class);
            productDao.updateTextById(tryProductInsertDto.getText(), productId);
            boardService.deleteOriginImgInText(product.get("text").toString(), tryProductInsertDto.getText());
        }
        //제품카테고리변경
        if(!product.get("category").equals(tryProductInsertDto.getCategory())){
            changeFlag=true;
            utillService.writeLog("상품카테고리 변경요청", productService.class);
            productDao.updateCategoryById(tryProductInsertDto.getCategory(), productId);
        }
        //제품 이벤트 변경판별
        List<Map<String,Object>>eventInfors=(List<Map<String,Object>>)tryProductInsertDto.getEventInfors();
        changeFlag=checkChangeEvent(tryProductInsertDto.getEventFlag(),productId,eventInfors);
        //변경되었는지판별
        if(changeFlag){
            return utillService.getJson(true, "변경이 완료되었습니다");
        }
        return utillService.getJson(true, "변경된 사항이 없습니다");
    }
    private Boolean checkChangeEvent(int eventFlag,int productId,List<Map<String,Object>>eventInfors) {  
        productEventDao.deleteEventsByProductId(productId);
        checkEvent(eventInfors);
        insertEvents(eventInfors,productId);
        return true;
    }
    private List<Map<String,Object>> getProductAndEvenArr(int productId) {
        List<Map<String,Object>>productAndEvnets=getProductAndEventsCore(productId);
        utillService.checkDaoResult(productAndEvnets,"존재하지 않는 상품입니다", "getProductAndEvents");
        return productAndEvnets;
    }
    private Map<String,Object> getProductInArr(List<Map<String,Object>>productAndEvnets) {
        Map<String,Object>product=new HashMap<>();
        product.put("category", productAndEvnets.get(0).get("category"));
        product.put("origin", productAndEvnets.get(0).get("origin"));
        product.put("id", productAndEvnets.get(0).get("product_id"));
        product.put("product_img_path", productAndEvnets.get(0).get("product_img_path"));
        product.put("product_name", productAndEvnets.get(0).get("product_name"));
        product.put("text", productAndEvnets.get(0).get("text"));
        product.put("price", productAndEvnets.get(0).get("price"));
        return product;
    }
    public JSONObject getProductAndEvents(int productId) {
        List<Map<String,Object>>productAndEvnets=getProductAndEvenArr(productId);
        JSONObject response=new JSONObject();
        //제품정보 꺼내기
        response.put("product", getProductInArr(productAndEvnets));
        //이벤트 조회
        Boolean eventFlag=false;
        if(Integer.parseInt(productAndEvnets.get(0).get("event_state").toString())==1){
            eventFlag=true;           
            response.put("events", getEventsInArr(productAndEvnets));
        }
        response.put("event_flag", eventFlag);
        return utillService.getJson(true, response);
    }
    private List<Map<String,Object>> getEventsInArr(List<Map<String,Object>>productAndEvnets) {
        List<Map<String,Object>>events=new ArrayList<>();
        for(Map<String,Object>productAndEvnet:productAndEvnets){
            Map<String,Object>event=new HashMap<>();
            event.put("event_date", productAndEvnet.get("product_event_date"));
            event.put("id", productAndEvnet.get("product_event_id"));
            event.put("event_price", productAndEvnet.get("product_event_price"));
            events.add(event);
        }
        return events;
    }
    private List<Map<String,Object>> getProductAndEventsCore(int productId) {
        return productDao.findByIdJoinEvent(productId);
    }
    public List<productEventVo> getProductEvent(int productId) {
        return productEventDao.findByProductId(productId);
    }
    public List<productVo> getByFlyerId(int flyerId) {
        List<productVo>products=productDao.findByFlyerId(flyerId);
        return products;
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryProductInsertDto tryProductInsertDto) {
        int flyerId=tryProductInsertDto.getFlyerId();
        checkExist(flyerId);
        //상품 insert
        productVo vo=productVo.builder().storeId(tryProductInsertDto.getStoreId()).category(tryProductInsertDto.getCategory()).text(tryProductInsertDto.getText()).flyerId(flyerId).origin(tryProductInsertDto.getOrigin()).price(tryProductInsertDto.getPrice()).productImgPath(tryProductInsertDto.getProductImgPath()).productName(tryProductInsertDto.getProductName()).eventFlag(tryProductInsertDto.getEventFlag()).build();
        productDao.save(vo);
        //이벤트 여부 검사
        if(tryProductInsertDto.getEventFlag()==1){
            List<Map<String,Object>>eventInfors=(List<Map<String,Object>>)tryProductInsertDto.getEventInfors();
            checkEvent(eventInfors);
            insertEvents(eventInfors,vo.getId());
        } 
        return utillService.getJson(true, "상품등록에 성공 하였습니다");
    }
    private void insertEvents(List<Map<String,Object>>eventInfors,int productId) {
        for(Map<String,Object>eventInfor:eventInfors){
            productEventVo vo2=productEventVo.builder().date(eventInfor.get("date").toString()).productId(productId).eventPrice(Integer.parseInt(eventInfor.get("price").toString())).build();
            productEventDao.save(vo2);
        }
    }
    private void checkExist(int flyerId) {
        if(productDao.countFlyerByFlyerId(flyerId)==0){
            throw utillService.makeRuntimeEX("존재하지 않는 전단입니다", "checkExist");
        }
    }
    private boolean checkPrice(int price) {
        if(price<=0){
            return true;
        }
        return false;
    }
    private void checkEvent(List<Map<String,Object>>eventInfors) {
            for(Map<String,Object>eventInfor:eventInfors){
                try {
                    if(checkPrice(Integer.parseInt(eventInfor.get("price").toString()))){
                        throw utillService.makeRuntimeEX("이벤트가격이 0원 보다 작거나 같습니다 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                    }
                } catch (NumberFormatException e) {
                    throw utillService.makeRuntimeEX("가격은 숫자만입렵해주세요 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                }
                
            }    
    }
}
