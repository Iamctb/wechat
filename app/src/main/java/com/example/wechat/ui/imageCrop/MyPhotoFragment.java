package com.example.wechat.ui.imageCrop;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.wechat.AppConstant.Constant;
import com.example.wechat.R;
import com.example.wechat.bean.User;
import com.example.wechat.util.OkHttpUtil;
import com.example.wechat.util.SpUtils;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

import static com.example.wechat.R.drawable.logo;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPhotoFragment extends PictureSelectorFragment {


    Toolbar toolbar;
    ImageView mPictureIv;


    public  static MyPhotoFragment newInstance () {
        return new MyPhotoFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_my_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        mPictureIv = view.findViewById(R.id.main_frag_picture_iv);

        File file = new File(Environment.getExternalStorageDirectory(),"cropImage.png") ;
        if(file.exists()){
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            mPictureIv.setImageBitmap(bm);

        }

        initEvents();


    }

    public void initEvents() {
        // 设置图片点击监听
        mPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });

        // 设置裁剪图片结果监听
        setOnPictureSelectedListener(new OnPictureSelectedListener() {
            @Override
            public void onPictureSelected(Uri fileUri, Bitmap bitmap) {
                mPictureIv.setImageBitmap(bitmap);

                final String filePath = fileUri.getEncodedPath();
                final String imagePath = Uri.decode(filePath);

                //上传图片
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            User user = Hawk.get("loginUser");
                            String url = Constant.getServerUrl()+"/user/uploadFaceBase64";
                            OkHttpUtil.uploadImage(getContext(),user.getId(),imagePath,url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                Toast.makeText(getContext(), "图片已经保存到:" + imagePath, Toast.LENGTH_LONG).show();
            }
        });
    }

}
