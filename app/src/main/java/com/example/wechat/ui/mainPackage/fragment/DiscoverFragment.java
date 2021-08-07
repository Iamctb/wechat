package com.example.wechat.ui.mainPackage.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.wechat.R;
import com.example.wechat.ui.mainPackage.activity.FriendActivity;

public class DiscoverFragment extends Fragment {

    private RelativeLayout rlFriendCircle;
    private RelativeLayout rlAddFriend;
    private RelativeLayout rlScanQrCode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_discover,container,false);

        rlFriendCircle = view.findViewById(R.id.rl_friend_circle);
        rlAddFriend = view.findViewById(R.id.rl_add_friend);
        rlScanQrCode = view.findViewById(R.id.rl_scan_qrCode);


        rlAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendActivity.class);
                startActivity(intent);
            }
        });





        return view;
    }


}
