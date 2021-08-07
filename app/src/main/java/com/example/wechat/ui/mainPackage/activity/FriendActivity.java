package com.example.wechat.ui.mainPackage.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;
import com.example.wechat.bean.User;
import com.example.wechat.util.OkHttpUtil;
import com.tencent.mmkv.MMKV;

import org.w3c.dom.Text;

import okhttp3.OkHttpClient;

public class FriendActivity extends AppCompatActivity {

    private EditText etFriendContent;
    private ImageView ivSearch;
    private ImageView ivFriendPhoto;
    private TextView tvFriendNickName;
    private ImageView ivAddFriend;
    private ImageView ivCancel;

    @SuppressLint("HandlerLeak")
    final
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            MMKV mmkv = MMKV.defaultMMKV();
            if (msg.what == 200) {
                User friend = mmkv.decodeParcelable("friend", User.class);
                if (friend != null) {
                    tvFriendNickName.setText(friend.getUsername());
                }
            }else {
                String message = mmkv.decodeString("searchFriend");
                if(message!=null)
                    tvFriendNickName.setText(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        changeStatusStyle();
        init();

        final MMKV mmkv =  MMKV.defaultMMKV();
        final User user = (User)mmkv.decodeParcelable("UserInfo",User.class);


        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etFriendContent.getText().toString()!=null){
                    etFriendContent.setText(null);
                }
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String myUserId = user.getId();
                final String friendUserName = etFriendContent.getText().toString();

                if(myUserId != null && friendUserName != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int status = OkHttpUtil.searchFriend(myUserId,friendUserName);

                            Message message = new Message();
                            message.what = status;
                            handler.sendMessage(message);
                        }
                    }).start();
                }

            }
        });

    }

    private void init(){
        etFriendContent = findViewById(R.id.et_friend_content);
        ivSearch = findViewById(R.id.iv_search);
        ivFriendPhoto = findViewById(R.id.iv_fiend_photo);
        tvFriendNickName = findViewById(R.id.fiend_nickName);
        ivAddFriend = findViewById(R.id.iv_add_friend);
        ivCancel = findViewById(R.id.iv_cancel);
    }

    //修改顶部状态栏Style
    public void changeStatusStyle(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            //设置修改状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏的颜色，
            window.setStatusBarColor(getResources().getColor(R.color.white));
            //设置状态栏字体颜色
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

}
