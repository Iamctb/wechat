package com.example.wechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.AppConstant.Constant;
import com.example.wechat.bean.User;
import com.example.wechat.ui.MainActivity;
import com.example.wechat.util.OkHttpUtil;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.tencent.mmkv.MMKV;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnGo;
    private TextView tvCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Hawk.init(this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();

        //MMKV 初始化
        MMKV.initialize(this);

        init();
    }

    private void init(){
        etName=findViewById(R.id.et_name);
        etPassword=findViewById(R.id.et_password);
        btnLogin=findViewById(R.id.btn_login);
        tvCallback=findViewById(R.id.tv_callback_result);
        btnGo=findViewById(R.id.btn_go);

        btnLogin.setOnClickListener(this);
        btnGo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:

                final Context context = this;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url= Constant.getServerUrl()+"/user/registerOrLogin";
                        User user = new User();
                        //User user1 = Hawk.get("loginUser");


                        MMKV mmkv =  MMKV.defaultMMKV();
                        User user1 = (User)mmkv.decodeParcelable("UserInfo",User.class);

                        if(user1!=null){
                            user = user1;
                        }else{
                            user.setId("");
                            user.setUsername(etName.getText().toString());
                            user.setPassword(etPassword.getText().toString());

                            user.setFaceImage("");
                            user.setFaceImageBig("");
                            user.setNickname("");
                            user.setQrcode("");
                            user.setCid("");
                        }



                        final String result = OkHttpUtil.postHttpRequest(context,url,user);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvCallback.setText(result);
                                if(!result.equals("fail")){
                                    Intent intent =  new Intent(context, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }).start();

                break;

            case R.id.btn_go:
                Intent intent =  new Intent(this, MainActivity.class);
                startActivity(intent);

                default:
                    break;
        }
     }
}
