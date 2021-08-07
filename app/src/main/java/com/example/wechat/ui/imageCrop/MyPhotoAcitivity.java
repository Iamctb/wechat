package com.example.wechat.ui.imageCrop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.wechat.R;

public class MyPhotoAcitivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photo_acitivity);

        initMainFragment();

    }


    /**
     * 初始化内容Fragment
     *
     * @return void
     */
    public void initMainFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MyPhotoFragment mFragment = MyPhotoFragment.newInstance();
        transaction.replace(R.id.main_act_container, mFragment, mFragment.getClass().getSimpleName());
        transaction.commit();
    }



}
