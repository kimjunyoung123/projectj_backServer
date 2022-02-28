package com.kimcompay.projectjb.users.user.service;

import java.util.List;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.board.service.boardService;
import com.kimcompay.projectjb.users.company.model.stores.storeReviewVo;
import com.kimcompay.projectjb.users.company.model.stores.storeReviewsDao;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class reviewService {
    @Autowired
    private storeReviewsDao storeReviewsDao;
    @Autowired
    private boardService boardService;
    @Autowired
    private fileService fileService;

    public JSONObject tryInsert(JSONObject jsonObject,int storeId) {
        //결제매장 확인 로직 추후에 추가해야함

        //유효성검사
        String text=jsonObject.get("text").toString();
        confrim(text);
        //insert
        storeReviewVo vo=storeReviewVo.builder().storeId(storeId).text(text).userId(utillService.getLoginId()).build();
        storeReviewsDao.save(vo);
        return utillService.getJson(true, "리뷰등록이 완료 되었습니다");
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject tryUpadte(JSONObject jsonObject,int reviewId) {
        //유효성검사
        String text=jsonObject.get("text").toString();
        confrim(text);
        storeReviewVo storeReviewVo=getVoByIdAndUserId(reviewId);
        String originText=storeReviewVo.getText();
        if(!originText.equals(text)){
            utillService.writeLog("리뷰 수정 요청", reviewService.class);
            storeReviewVo.setText(text);
            boardService.getNotUsedImg(text, originText);
        }
        return utillService.getJson(true, "리뷰가 수정되었습니다");
    }
    public JSONObject tryDelete(int reviewId) {
        storeReviewVo storeReviewVo=getVoByIdAndUserId(reviewId);
        storeReviewsDao.deleteById(reviewId);
        List<String>imgs=utillService.getImgSrc(storeReviewVo.getText()); 
        afterDelete(imgs);
        return utillService.getJson(true, "리뷰기 삭제되었습니다");
    }
    @Async
    public void afterDelete(List<String>imgs) {
        try {
            for(String img:imgs){
                fileService.deleteFile(img);
            }
        } catch (Exception e) {
            utillService.writeLog("리뷰삭제중 사진삭제 실패",reviewService.class );
        }
    }
    private void confrim(String text) {
        if(utillService.checkBlank(text)){
            throw utillService.makeRuntimeEX("공백입니다", "confrim");
        }
    }
    private storeReviewVo getVoByIdAndUserId(int reviewId) {
        return  storeReviewsDao.findByIdAndUserId(reviewId, utillService.getLoginId()).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 댓글입니다", "tryUpadte"));

    }
}
