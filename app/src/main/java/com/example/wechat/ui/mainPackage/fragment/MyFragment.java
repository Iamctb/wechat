package com.example.wechat.ui.mainPackage.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.AppConstant.Constant;
import com.example.wechat.R;
import com.example.wechat.bean.User;
import com.example.wechat.ui.imageCrop.MyPhotoAcitivity;
import com.example.wechat.ui.imageCrop.PictureSelectorFragment;
import com.example.wechat.util.SpUtils;
import com.orhanobut.hawk.Hawk;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MyFragment extends PictureSelectorFragment {

    private TextView tv_username;
    private TextView tv_id;
    private ImageView photo;
    private ImageView qrcode;

    private static Boolean goToCropPhoto =false;


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj !=null && msg.what == 1){
                photo.setImageBitmap((Bitmap) msg.obj);
            }

            if (msg.obj !=null && msg.what == 2){
                qrcode.setImageBitmap((Bitmap) msg.obj);
            }
        };
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_my,container,false);

        tv_username = view.findViewById(R.id.username);
        tv_id = view.findViewById(R.id.id);
        photo = view.findViewById(R.id.photo);
        qrcode = view.findViewById(R.id.qrcode);


        //final User user = Hawk.get("loginUser");

        final MMKV mmkv =  MMKV.defaultMMKV();
        final User user = (User)mmkv.decodeParcelable("UserInfo",User.class);



        tv_username.setText(user.getUsername());
        tv_id.setText(user.getId());

        //获取头像
        File file = new File(Environment.getExternalStorageDirectory(),"cropImage.png") ;
        if(file.exists()){      //优先从本地取
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            photo.setImageBitmap(bm);
        }else{                  //如果本地没有，则从网咯取
            new Thread(new Runnable() {
                @Override
                public void run() {

                    String internetImageUrl = Constant.getImageUrl()+"/"+user.getFaceImage();
                    Bitmap bitmap = getInternetPicture(internetImageUrl);
                    if(bitmap !=null ){
                        Message message = new Message();
                        message.obj = bitmap;
                        message.what = 1 ;
                        handler.sendMessage(message);

                        File file = new File(Environment.getExternalStorageDirectory(), "cropImage.png");
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
        }

        //获取二维码
        File qrcodeFile = new File(Environment.getExternalStorageDirectory(), "qrcode.png");
        if(qrcodeFile.exists()){
            Bitmap bm = BitmapFactory.decodeFile(qrcodeFile.getAbsolutePath());
            qrcode.setImageBitmap(bm);
        }else{
            //获取二维码
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String internetImageUrl = Constant.getImageUrl()+"/"+user.getQrcode();
                    Bitmap bitmap = getInternetPicture(internetImageUrl);
                    if (bitmap!=null){
                        Message message = new Message();
                        message.obj = bitmap;
                        message.what = 2;
                        handler.sendMessage(message);
                        Log.d("qrcode","从网上");

                        File file = new File(Environment.getExternalStorageDirectory(), "qrcode.png");
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
            }).start();

        }

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCropPhoto = true;
                Intent intent = new Intent(getContext(), MyPhotoAcitivity.class);
                startActivity(intent);
            }
        });

        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        if(goToCropPhoto){
            File file = new File(Environment.getExternalStorageDirectory(),"cropImage.png") ;
            if(file.exists()){
                Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                photo.setImageBitmap(bm);
            }
        }


    }

    public Bitmap getInternetPicture(String UrlPath) {
        Bitmap bm = null;
        // 1、确定网址
        // http://pic39.nipic.com/20140226/18071023_164300608000_2.jpg
        String urlpath = UrlPath;
        // 2、获取Uri
        try {
            URL uri = new URL(urlpath);

            // 3、获取连接对象、此时还没有建立连接
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            // 4、初始化连接对象
            // 设置请求的方法，注意大写
            connection.setRequestMethod("GET");
            // 读取超时
            connection.setReadTimeout(5000);
            // 设置连接超时
            connection.setConnectTimeout(5000);
            // 5、建立连接
            connection.connect();

            // 6、获取成功判断,获取响应码
            if (connection.getResponseCode() == 200) {
                // 7、拿到服务器返回的流，客户端请求的数据，就保存在流当中
                InputStream is = connection.getInputStream();
                // 8、从流中读取数据，构造一个图片对象GoogleAPI
                bm = BitmapFactory.decodeStream(is);
                // 9、把图片设置到UI主线程
                // ImageView中,获取网络资源是耗时操作需放在子线程中进行,通过创建消息发送消息给主线程刷新控件；

                Log.i("", "网络请求成功");

            } else {
                Log.v("tag", "网络请求失败");
                bm = null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;

    }

}
