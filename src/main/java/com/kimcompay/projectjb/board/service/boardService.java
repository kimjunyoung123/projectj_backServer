package com.kimcompay.projectjb.board.service;

import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class boardService {

    @Autowired
    private fileService fileService;

    @Async
    public void deleteOriginImgInText(String text,String originText) {
        List<String>notUsedImgs=getNotUsedImg(text, originText);
        if(notUsedImgs.size()==0){
            utillService.writeLog("새글수정후 삭제할 이미지가 없음", boardService.class);
            return;
        }
        for(String notUseImg:notUsedImgs){
            utillService.writeLog("기존에서 삭제시도 이미지: "+notUseImg, boardService.class);
            fileService.deleteFile(utillService.getImgNameInPath(notUseImg, 4));
        }
    }
    public List<String> getNotUsedImg(String text,String originText) {
        List<String>imgs=utillService.getImgSrc(text);
        List<String>originImgs=utillService.getImgSrc(originText);
        //기존글에 사진이 있는지 검사 기존글에 사진이 없다면 처리할 사진이 없음
        if(originImgs.size()==0){
            throw utillService.makeRuntimeEX("기존 글에 사진이 없음", "getNotUsedImg");
        }else if(imgs.size()==0){ //새 글의 사진이 없다면 기존 글 사진 전부 던지기
            utillService.writeLog("새글에서 이미지가 한개도 없음 기존이미지 전부 삭제시도", boardService.class);
            return originImgs;
        }
        //새 글에 기존 사진이 있는지 검사
        List<String>notUsedImgs=new ArrayList<>();
        for(String originImg:originImgs){
            if(!imgs.contains(originImg)){
                utillService.writeLog("기존에서 삭제될 이미지: "+originImg, boardService.class);
                notUsedImgs.add(originImg);
            }
        }
        return notUsedImgs;
    }
}
