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
    private final static Logger logger=LoggerFactory.getLogger(s3Service.class);
    @Autowired
    private AmazonS3 amazonS3;

    public JSONObject uploadImage(File file,String bucketName) {
        logger.info("uploadImage");
        try {
            String saveName=file.getName();
            amazonS3.putObject(bucketName,saveName, file);
            logger.info("파일업로드 완료");
            return utillService.getJson(true, saveName);
        } catch (Exception e) {
            logger.info("파일 업로드에 실패 했습니다");
            return utillService.getJson(false, "파일 업로드에 실패했습니다");
        }

    }
    public void deleteFile(String bucktetName,String fileName) {
        logger.info("deleteFile");
        amazonS3.deleteObject(bucktetName, fileName);
    }
}
