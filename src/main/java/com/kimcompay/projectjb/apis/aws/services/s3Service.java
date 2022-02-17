package com.kimcompay.projectjb.apis.aws.services;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.kimcompay.projectjb.utillService;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class s3Service {
    @Autowired
    private AmazonS3 amazonS3;

    public String uploadImage(File file,String bucketName) {
        try {
            String imgName=file.getName();
            amazonS3.putObject(bucketName,imgName, file);
            utillService.writeLog("파일업로드 완료",s3Service.class);
            return imgName;
        } catch (Exception e) {
            utillService.writeLog("파일 업로드에 실패 했습니다",s3Service.class);
            throw utillService.makeRuntimeEX("파일 업로드에 실패했습니다","uploadImage");
        }

    }
    public void deleteFile(String bucktetName,String fileName) {
        amazonS3.deleteObject(bucktetName, fileName);
    }
}
