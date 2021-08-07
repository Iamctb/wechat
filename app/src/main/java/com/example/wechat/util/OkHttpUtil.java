package com.example.wechat.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.wechat.AppConstant.Constant;
import com.example.wechat.bean.User;
import com.orhanobut.hawk.Hawk;
import com.tencent.mmkv.MMKV;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    public static ProgressDialog dialog;

    private static String callbackResult=" ";
    private static int status = 0;

    public static String postHttpRequest(final Context context, final String url, User userVo)  {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id",userVo.getId());
            jsonObject.put("username",userVo.getUsername());
            jsonObject.put("password",userVo.getPassword());
            jsonObject.put("faceImage",userVo.getFaceImage());
            jsonObject.put("nickname",userVo.getNickname());
            jsonObject.put("faceImageBig",userVo.getFaceImageBig());
            jsonObject.put("qrcode",userVo.getQrcode());
            jsonObject.put("cid",userVo.getCid());
        } catch (JSONException e) {
            Log.d("CTB_OkHttp",e.getMessage());
            e.printStackTrace();
        }
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json;charset=utf-8"),jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();


        //回调
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("CTB_callback_fail",e.getMessage());
                e.printStackTrace();
                callbackResult = "fail";
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callbackResult = response.body().string();
                Log.d("CTB_callbackResult",callbackResult);
                User user = (User) JsonUtil.getBeanFromJson(callbackResult);
                if(user != null){

                    //Hawk.put("loginUser",user);

                    MMKV mmkv =  MMKV.defaultMMKV();
                    mmkv.encode("UserInfo",user);

                    //SpUtils.saveBean2Sp(context,user,"userInfo","user");
                    Log.d("CTB_callback_success",user.getUsername());
                }
            }
        });

        return callbackResult;
    }

    /**
     * 上传图片
     * @param imagePath 本地图片路径
     * @return
     */
    public static String uploadImage(final Context context, String userId, String imagePath, String url) throws IOException,JSONException{
        String base64Url = imgToBase64(imagePath);

        Log.d("imgToBase64",base64Url);


        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("userId",userId);
            jsonObject.put("faceData",base64Url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = FormBody.create(MediaType.parse("application/json;charset=utf-8"),jsonObject.toString());
        Request request =new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callbackResult = "fail";
                Log.d("uploadFace","fail"+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callbackResult = response.body().string();
                Log.d("CTB_callbackResult",callbackResult);
                User user = (User) JsonUtil.getBeanFromJson(callbackResult);
                if(user != null){

                    MMKV mmkv =  MMKV.defaultMMKV();
                    mmkv.encode("UserInfo",user);

                    Log.d("CTB_uploadImg_success",user.getFaceImage());
                }
            }
        });

        return callbackResult;
    }


    /**
     * 搜索好友
     * @param myUserId
     * @param friendUserName
     * @return
     */
    public static int searchFriend( String myUserId, String friendUserName){
        RequestBody body = new FormBody.Builder()
                .add("myUserId",myUserId)
                .add("friendUserName",friendUserName)
                .build();

        String url= Constant.getServerUrl()+"/user/searchFriend";
        ;
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        if(body != null){
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("searchFriend","卧槽，居然搜索好友失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callbackResult = response.body().string();
                Log.d("CTB_callbackResult",callbackResult);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(callbackResult);

                    status = jsonObject.optInt("status");
                    String msg = jsonObject.optString("msg");
                    MMKV mmkv =  MMKV.defaultMMKV();
                    if( status == 200){
                        User friend = (User) JsonUtil.getBeanFromJson(callbackResult);
                        if(friend != null){
                            mmkv.encode("friend",friend);
                        }
                    }else{
                        mmkv.encode("searchFriend",msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        return status;
    }




    /**
     * 下载图片
     * @param url
     * @param imagePath 图片路径
     * @return  byte[] 图片的字符数组
     */
    public static byte[] downloadImage(String url,String imagePath) throws IOException{
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        byte[] bytes = response.body().bytes();
        return bytes;
    }

    /**
     *图片转成 base64格式
     * @param imgPath
     * @return
     * @throws IOException
     */
    public static String imgToBase64(String imgPath) throws IOException {
        Bitmap bitmap = null;

        if(imgPath !=null && imgPath.length()>0){
            bitmap = readBitmap(imgPath);
        }
        if(bitmap == null){
            return null;
        }
        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,out);

            out.flush();
            out.close();

            byte[] imgBytes = out.toByteArray();
            return Base64.encodeToString(imgBytes,Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            out.flush();
            out.close();
        }
    }

    //将图片转换程base64编码
    public static void base64ToBitmap(String base64Data,String imgName) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        File myCaptureFile = new File(Environment.getExternalStorageDirectory(), imgName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myCaptureFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        boolean isTu = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        if (isTu) {
            // fos.notifyAll();
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //将base64编码转化成图片
    public Bitmap base64ToBitmap(String bitmapString){
        byte[] decode = Base64.decode(bitmapString,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode,0,decode.length);
        return bitmap;
    }



    private static Bitmap readBitmap(String imgPath){
        try{
            return BitmapFactory.decodeFile(imgPath);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void startDialog(Context context) {
        dialog = new ProgressDialog(context);
        dialog.setMessage("数据加载。。。");
        dialog.setTitle("请稍后");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 关闭等待框
     */
    public static void stopDialog() {
        if (dialog != null) {

            dialog.dismiss();
        }
    }



}
