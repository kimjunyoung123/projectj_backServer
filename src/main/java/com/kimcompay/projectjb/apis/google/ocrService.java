package com.kimcompay.projectjb.apis.google;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.kimcompay.projectjb.utillService;

import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ocrService {
  public static JSONObject detectText() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String filePath = "/Users/sesisoft/Desktop/test2.jpeg";
    return detectText(filePath);
  }

  // Detects text in the specified image.
  public static JSONObject detectText(String filePath) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      List<String>strings=new ArrayList<>();
      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          return utillService.getJson(false, "글자 추출에 실패했습니다");
        }
        
        // For full list of available annotations, see http://g.co/cloud/vision/docs
        for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
          //System.out.format("Text: %s%n", annotation.getDescription());
          //System.out.format("Position : %s%n", annotation.getBoundingPoly());
          strings.add(annotation.getDescription());
          strings.add(annotation.getBoundingPoly().toString());
        }
        
      }return utillService.getJson(true, strings);
    }
  }
}