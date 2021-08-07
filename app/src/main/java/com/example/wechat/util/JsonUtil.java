package com.example.wechat.util;

import com.example.wechat.bean.User;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    public static <T extends Object> T getBeanFromJson(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.optJSONObject("data");
            if(data!=null){
                User user = new User();
                user.setId(data.optString("id"));
                user.setUsername(data.optString("username"));
                user.setPassword("");
                user.setNickname(data.optString("nickname"));
                user.setFaceImage(data.optString("faceImage"));
                user.setFaceImageBig(data.optString("faceImageBig"));
                user.setQrcode(data.optString("qrcode"));
                user.setCid("");

                return (T) user;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
