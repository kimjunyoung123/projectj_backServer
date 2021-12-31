package com.kimcompay.projectjb.apis.aws.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class fileService {
    private Logger logger=LoggerFactory.getLogger(fileService.class);

    @Value("${aws.s3.url}")
    private String awsUrl;
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private s3Service s3Service;

    public JSONObject upload(MultipartHttpServletRequest request) {
        logger.info("upload");
        //파일꺼내기
        List<MultipartFile>files=request.getFiles("upload");
        //s3업로드시도
        JSONObject reseponse=s3Service.uploadImage(files.get(0), bucketName);
        //성공했다면 페이지 이탈시 사진 삭제를 위해 세션에 담기+경로만들기
        Boolean result=(Boolean)reseponse.get("flag");
        String url=null;
        if(result){
            logger.info("사진 세션에 담기");
            //주소만들기
            String uploadName=reseponse.get("message").toString();
            url=awsUrl+"/"+bucketName+"/"+uploadName;
            logger.info("s3사진 경로: "+url);
            //세션에담기
            List<String>imgPaths=new ArrayList<>();
            HttpSession httpSession=request.getSession();
            String imgSession=senums.imgSessionName.get();
            try {
                //이미지 배열 꺼내서 넣어주기 첫 요청이라면 예외 발생
                httpSession=request.getSession();
                imgPaths=(List<String>)httpSession.getAttribute(imgSession);
                imgPaths.add(uploadName);
            } catch (NullPointerException e) {
                logger.info("첫 사진업로드 요청이므로 예외발생 만들어서 넣어주기");
                imgPaths=new ArrayList<>();
                imgPaths.add(uploadName);
                httpSession.setAttribute(imgSession, imgPaths);
            }
        }
        reseponse.put("uploaded", result);
        reseponse.put("url", url);
        return reseponse;
    }
}
