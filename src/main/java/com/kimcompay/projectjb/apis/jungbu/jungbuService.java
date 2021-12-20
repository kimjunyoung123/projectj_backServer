package com.kimcompay.projectjb.apis.jungbu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.apis.requestTo;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class jungbuService {
    private Logger logger=LoggerFactory.getLogger(jungbuService.class);

    @Value("${tax.decoding.apikey}")
    private String apikey;
    @Autowired
    private requestTo requestTo;

    public void getCompanyNum(int compay_num) {
        logger.info("getCompanyNum");
       /* //body생성
        JSONObject body=new JSONObject();
        //사업자등록증 담기
        List<String>integers=new ArrayList<>();
        integers.add(Integer.toString(compay_num));
        body.put("b_no", integers);
        //요청 url
        String url="https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey="+apikey;
        //헤더담기
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return requestTo.requestPost(body, url, headers);*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\n  \"businesses\": " +
                        "   [\n    " +
                        "       {\n      " +
                        "           \"b_no\": \"0000000000\",\n      " +
                        "           \"start_dt\": \"20000101\",\n      " +
                        "           \"p_nm\": \"홍길동\",\n      " +
                        "           \"p_nm2\": \"홍길동\",\n      " +
                        "           \"b_nm\": \"(주)테스트\",\n      " +
                        "           \"corp_no\": \"0000000000000\",\n      " +
                        "           \"b_sector\": \"\",\n      " +
                        "           \"b_type\": \"\"\n    " +
                        "       }\n  " +
                        "   ]\n" +
                        "}");
        Request request = new Request.Builder()
                .url("http://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey="+apikey)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response;
        try {
                response = client.newCall(request).execute();
                System.out.println(response.body().string());
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }
}
