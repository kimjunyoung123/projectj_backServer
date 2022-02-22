package com.kimcompay.projectjb.apis.aws.services;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class fileService {
    @Value("${aws.s3.url}")
    private String awsUrl;
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private s3Service s3Service;
    public JSONObject upload(MultipartHttpServletRequest request) {
        List<MultipartFile>files=request.getFiles("upload");
        if(files.size()>1){
            //아직 미사용
            for(MultipartFile multipartFile:files){
                File file=convert(multipartFile);
                uploadCore(file);
                file.delete();
            }
            return null;
        }else{
            //file로변환
            File file=convert(files.get(0));
            //aws업로드
            Map<String,String>result=uploadCore(file);
            //세션저장
            saveSession(result.get("uploadName"));
            //파일삭제
            file.delete();
            return utillService.getJson(true, result.get("url"));
        }
    }
    public JSONObject upload(File file) {
        Map<String,String>result=uploadCore(file);
        saveSession(result.get("uploadName"));
        return utillService.getJson(true, result.get("url"));
    }
    private void saveSession(String uploadName) {
        //세션에담기
        List<String>imgNames=new ArrayList<>();
        HttpSession httpSession=utillService.getHttpServletRequest().getSession();
        String imgSession=senums.imgSessionName.get();
        try {
            //이미지 배열 꺼내서 넣어주기 첫 요청이라면 예외 발생
            imgNames=(List<String>)httpSession.getAttribute(imgSession);
            imgNames.add(uploadName);
        } catch (NullPointerException e) {
            utillService.writeLog("첫 사진업로드 요청이므로 예외발생 만들어서 넣어주기",fileService.class);
            imgNames=new ArrayList<>();
            imgNames.add(uploadName);
            httpSession.setAttribute(imgSession, imgNames);
        }
    }
    private Map<String,String> uploadCore(File file) {
        //업로드
        String uploadName=s3Service.uploadImage(file, bucketName);
        //결과응답
        Map<String,String>response=new HashMap<>();
        response.put("url", awsUrl+"/"+bucketName+"/"+uploadName);
        response.put("uploadName", uploadName);
        return response;
    }
    public void deleteFile(HttpSession httpSession,List<String>usingImgs) {
        try {
            List<String>imgNames=(List<String>)httpSession.getAttribute(senums.imgSessionName.get());
            for(String img:imgNames){
                if(!usingImgs.contains(img)){
                    utillService.writeLog("삭제할 이미지: "+img,fileService.class);
                    deleteFile(img);
                }
            }
        } catch(NullPointerException e){
            utillService.writeLog("삭제할 이미지가 없습니다",fileService.class);
        }catch (Exception e) {
           utillService.writeLog("이미지 삭제 실패",fileService.class);
        }
    }
    @Async
    public void deleteFile(String fileName) {
        s3Service.deleteFile(bucketName, fileName);
    }
    public File convert(MultipartFile multipartFile) {
        File file=new File(LocalDate.now().toString()+UUID.randomUUID()+multipartFile.getOriginalFilename());
        try(FileOutputStream fileOutputStream=new FileOutputStream(file)){
            fileOutputStream.write(multipartFile.getBytes()); 
        } catch (Exception e) {
            e.printStackTrace();
            utillService.writeLog(e.getMessage(),fileService.class);
            throw utillService.makeRuntimeEX("파일형식변환에 실패했습니다","convert");
        }
        return file;
    }
}
