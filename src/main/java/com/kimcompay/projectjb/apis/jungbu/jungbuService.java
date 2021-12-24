package com.kimcompay.projectjb.apis.jungbu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    public JSONObject getCompanyNum(String compay_num,String start_dt,String name) {
        logger.info("getCompanyNum");
        //resttemplate통신이 안되서 okhttp3으로 통신
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        //필수값 넣기
        Map<String,Object> map=new LinkedHashMap<>();
        map.put("b_no", compay_num);
        map.put("start_dt", start_dt);
        map.put("p_nm", name);
       //필수값이 아닌것들
        map.put("p_nm2", ""); 
        map.put("b_nm", "");
        map.put("corp_no", "");
        map.put("b_sector", "");
        map.put("b_type", "");
        //요청형식이 json 배열임 
        List< Map<String,Object>>jsonObjects=new ArrayList<>();
        jsonObjects.add(map);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("businesses", jsonObjects);
        RequestBody body = RequestBody.create(jsonObject.toString(),mediaType); /*RequestBody.create(mediaType,
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
                        "}"); 옜날 방식*/
        logger.info("전송 정보: "+jsonObject);
        //요청
        Request request = new Request.Builder()
                .url("http://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey="+apikey)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        //결과 탐색
        try {
                Response response = client.newCall(request).execute();
                return utillService.stringToJson(response.body().string());
               
        } catch (Exception e) {
                logger.info("사업자 등록 조회실패");
                throw utillService.makeRuntimeEX("사업자 조회에 실패했습니다", "getCompanyNum");
        } 
    }
}
