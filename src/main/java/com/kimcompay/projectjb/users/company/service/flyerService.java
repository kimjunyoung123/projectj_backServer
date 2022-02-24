package com.kimcompay.projectjb.users.company.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.google.ocrService;
import com.kimcompay.projectjb.users.company.model.flyers.flyerDao;
import com.kimcompay.projectjb.users.company.model.flyers.flyerDetailVo;
import com.kimcompay.projectjb.users.company.model.flyers.flyerDetialDao;
import com.kimcompay.projectjb.users.company.model.flyers.flyerVo;
import com.kimcompay.projectjb.users.company.model.flyers.tryInsertFlyerDto;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class flyerService {
    private final int pageSize=2;
    @Autowired
    private flyerDao flyerDao;
    @Autowired
    private fileService fileService;
    @Autowired
    private flyerDetialDao flyerDetialDao;
    @Autowired
    private productService productService;
    
    @Transactional(rollbackFor = Exception.class)
    public String deleteWithAll(int flyerId) {
        flyerDao.deleteById(flyerId); 
        //삭제전 사진경로 모두 가져오기
        List<Map<String,Object>>flyerImgs=flyerDetialDao.findAllImgPathsByFlyerId(flyerId);
        flyerDetialDao.deleteByFlyerId(flyerId);
        productService.deleteAllByFlyerId(flyerId);
        //사진 모두 삭제
        if(!flyerImgs.isEmpty()){
            for(Map<String,Object>img:flyerImgs){
                try {
                    fileService.deleteFile(utillService.getImgNameInPath(img.get("flyer_img_path").toString(), 4));
                } catch (Exception e) {
                    utillService.writeLog("전단 전체 삭저중 이미지 삭제 실패", flyerService.class);
                }
            }
        }
        return  "전단 및 제품을 모두 삭제하였습니다";       
    }
    @Transactional(rollbackFor = Exception.class)
    public String tryDeleteDetail(int flyerDetailId) {
        //전단 조회
        flyerDetailVo flyerDetailVo=flyerDetialDao.findById(flyerDetailId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 전단입니다", "tryDelete"));
        //요청 전단 삭제
        flyerDetialDao.deleteById(flyerDetailId);
        try {
            fileService.deleteFile(utillService.getImgNameInPath(flyerDetailVo.getImgPath(), 4));
        } catch (Exception e) {
            //롤백할 필요없음
            utillService.writeLog("전단 이미지 삭제중 오류발생 ", flyerService.class);
        }
        return "전단이 삭제되었습니다";
    }
    public JSONObject getFlyers(int storeId,int page,String keyword) {
        List<String>dates=utillService.getDateInStrgin(keyword);
        List<Map<String,Object>>flyerVos=getFlyerArr(storeId,dates.get(0),dates.get(1),page);
        utillService.checkDaoResult(flyerVos, "등록한 전단이 없습니다", "getFlyers");
        int totalPage=utillService.getTotalPage(Integer.parseInt(flyerVos.get(0).get("totalCount").toString()), pageSize);
        JSONObject respose=new JSONObject();
        respose.put("totalPage", totalPage);
        respose.put("message", flyerVos);
        respose.put("flag", true);
        return respose;
    }
    public JSONObject getFlyerAndProducts(int flyerId) {
        JSONObject response=new JSONObject();
        //조회전단 검색
        List<Map<String,Object>>flyerAndProducts=flyerDao.findFlyerJoinProducts(flyerId);
        //빈값인지 검사
        utillService.checkDaoResult(flyerAndProducts,"존재하지 않는 전단입니다", "getFlyerAndProducts");  
        //소유자 검사
        utillService.checkOwner(Integer.parseInt(flyerAndProducts.get(0).get("company_id").toString()), "회사소유의 전단이 아닙니다");
        //flyer정보 꺼내기
        Map<String,Object>flyer=new HashMap<>();
        flyer.put("flyer_id", flyerAndProducts.get(0).get("flyer_id"));
        flyer.put("default", flyerAndProducts.get(0).get("default_select"));
        //전단 사진들 꺼내기& 전단에 있는 상품들 추출
        List<JSONObject>flyerDetails=new ArrayList<>();
        List<Map<String,Object>>products=new ArrayList<>();
        boolean productFlag=false;
        //join으로 인한 중복 체크를 위해
        //list contain 보다 map contain이 더빠르다고한다
        Map<String,Integer> flyerDetailIdsArr=new HashMap<>();
        Map<String,Integer> productIdsArr=new HashMap<>();
        for(Map<String,Object>flyerAndProduct:flyerAndProducts){
            //join으로 인한 중복 체크
            int id=Integer.parseInt(flyerAndProduct.get("flyer_details_id").toString());
            if(!flyerDetailIdsArr.containsKey(id+"flyerDetail")){
                flyerDetailIdsArr.put(id+"flyerDetail", id);
                JSONObject detail=new JSONObject();
                detail.put("flyer_img_path", flyerAndProduct.get("flyer_img_path"));
                detail.put("default", flyerAndProduct.get("flyer_detail_default"));
                detail.put("id", id);
                flyerDetails.add(detail);
            }
            id=Integer.parseInt(flyerAndProduct.get("product_id").toString());
            if(!productIdsArr.containsKey(id+"product")){
                productFlag=true;
                productIdsArr.put(id+"product", id);
                JSONObject product= new JSONObject();
                product.put("product_id",id);
                product.put("origin", flyerAndProduct.get("origin"));
                product.put("price", flyerAndProduct.get("price"));
                product.put("product_img_path", flyerAndProduct.get("product_img_path"));
                product.put("event_state", flyerAndProduct.get("event_state"));
                product.put("product_name", flyerAndProduct.get("product_name"));
                products.add(product);  
            }
        }  
        response.put("flyerDetail", flyerDetails);
        response.put("flyerFlag", true);
        response.put("flyer", flyer);
        if(productFlag){
            response.put("products", products);
        }
        response.put("productFlag", productFlag);

        return response;
    }
    private List<Map<String,Object>> getFlyerArr(int storeId,String startDate,String endDate,int page) {
        Map<String,Object>result=utillService.checkRequestDate(startDate, endDate);
        if((boolean)result.get("flag")){
            Timestamp start=Timestamp.valueOf(result.get("start").toString());
            Timestamp end=Timestamp.valueOf(result.get("end").toString());
            return flyerDao.findByDay(start, end, storeId,start, end, storeId,utillService.getStart(page, pageSize)-1,pageSize);

        }
        return flyerDao.findByStoreId(storeId,storeId,utillService.getStart(page, pageSize)-1,pageSize);
    }
    public flyerVo getFlyer(int flyerId) {
        return flyerDao.findById(flyerId).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 전단입니다", "getFlyer"));
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insert(tryInsertFlyerDto tryInsertFlyerDto ,int storeId) {
        //이미지 꺼내기
        List<String>flyerImgs=tryInsertFlyerDto.getFlyerImgs();
        //기본전단이라면 이전 기본전단 찾아서 꺼주기
        if(tryInsertFlyerDto.getDefaultFlag()==1){
            flyerDao.updateDefaultFlyerById(0, storeId);
        }
        //전단 만들기
        flyerVo vo=flyerVo.builder().defaultSelect(tryInsertFlyerDto.getDefaultFlag()).storeId(storeId).companyId(utillService.getLoginId()).thumbNail(tryInsertFlyerDto.getThumbNail()).build();
        flyerDao.save(vo);
        //전단 디테일 만들기
        for(String img:flyerImgs){
            if(utillService.checkBlank(img)){
                continue;
            }
            int defaultNum=0;
            if(tryInsertFlyerDto.getThumbNail().equals(img)){
                defaultNum=1;
            }
            flyerDetailVo vo2=flyerDetailVo.builder().defaultFlag(defaultNum).flyerId(vo.getId()).imgPath(img).build();
            flyerDetialDao.save(vo2);
        }
        return utillService.getJson(true, vo.getId());
    }
    public JSONObject ocrAndUpload(MultipartHttpServletRequest request,int storeId) {
        List<JSONObject>responses=new ArrayList<>();
        List<MultipartFile>imgs=request.getFiles("upload");
        for(MultipartFile img:imgs){
            JSONObject response=new JSONObject();
            File file=fileService.convert(img);
            //aws upload
            response=fileService.upload(file);
            //글자추출
            try {
                response.put("ocr",ocrService.detectText(file.toPath().toString()));
            } catch (Exception e) {
                utillService.writeLog("ocr 글자 추출 실패",flyerService.class);
                e.printStackTrace();
            }
            //로컬 파일 삭제
            file.delete();
            responses.add(response);
        }
        return utillService.getJson(true, responses);
    }
}
