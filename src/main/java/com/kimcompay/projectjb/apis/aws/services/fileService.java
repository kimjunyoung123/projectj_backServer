package com.kimcompay.projectjb.apis.aws.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
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
            List<String>imgNames=new ArrayList<>();
            HttpSession httpSession=request.getSession();
            String imgSession=senums.imgSessionName.get();
            try {
                //이미지 배열 꺼내서 넣어주기 첫 요청이라면 예외 발생
                httpSession=request.getSession();
                imgNames=(List<String>)httpSession.getAttribute(imgSession);
                imgNames.add(uploadName);
            } catch (NullPointerException e) {
                logger.info("첫 사진업로드 요청이므로 예외발생 만들어서 넣어주기");
                imgNames=new ArrayList<>();
                imgNames.add(uploadName);
                httpSession.setAttribute(imgSession, imgNames);
            }
        }
        reseponse.put("uploaded", result);
        reseponse.put("url", url);
        return reseponse;
    }
    public void deleteFile(HttpSession httpSession,List<String>usingImgs) {
        logger.info("deleteFile sesion");
        try {
            List<String>imgNames=(List<String>)httpSession.getAttribute(senums.imgSessionName.get());
            for(String img:imgNames){
                if(!usingImgs.contains(img)){
                    logger.info("삭제할 이미지: "+img);
                    deleteFile(img);
                }
            }
        } catch(NullPointerException e){
            logger.info("삭제할 이미지가 없습니다");
        }catch (Exception e) {
           logger.info("이미지 삭제 실패");
        }
    }
    public void deleteFile(String fileName) {
        logger.info("deleteFile");
        s3Service.deleteFile(bucketName, fileName);
    }
    public String uploadLocal(MultipartFile multipartFile) {
        logger.info("uploadLocal");
        File dest = new File("/Users/sesisoft/Desktop/persnal/projectj_backServer/src/main/resources/tempfilyers/" +multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(dest);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "/Users/sesisoft/Desktop/persnal/projectj_backServer/src/main/resources/tempfilyers/" +multipartFile.getOriginalFilename();
    }
}
