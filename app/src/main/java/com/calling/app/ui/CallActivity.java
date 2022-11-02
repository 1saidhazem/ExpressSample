package com.calling.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.calling.app.VoiceCall.Users;
import com.calling.example.express.ExpressManager;
import com.calling.example.express.ZegoDeviceUpdateType;
import com.calling.app.databinding.ActivityCallBinding;
import com.calling.app.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;

public class CallActivity extends AppCompatActivity {

    private ActivityCallBinding binding;
    private static final String TAG = "CallActivity";
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.logoutRoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpressManager.getInstance().leaveRoom();
                finish();
            }
        });

        binding.switchBtn.setSelected(true);
        binding.switchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().switchFrontCamera(!selected);
            }
        });

        binding.cameraBtn.setSelected(true);
        binding.cameraBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableCamera(!selected);
            }
        });
        binding.micBtn.setSelected(true);
        binding.micBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableMic(!selected);
            }
        });


        ExpressManager.getInstance().setExpressHandler(new ExpressManager.ExpressManagerHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        TextureView remoteTexture = binding.remoteTexture;
                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.userID);

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Users user = snapshot.getValue(Users.class);
                                binding.remoteName.setText(user.getName()); //receiver
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        setRemoteViewVisible(true);
                        ExpressManager.getInstance().setRemoteVideoView(user.userID, remoteTexture);
                    }
                } else {
                    setRemoteViewVisible(false);
                }
            }

            @Override
            public void onRoomUserDeviceUpdate(ZegoDeviceUpdateType updateType, String userID, String roomID) {
                Log.d(TAG,
                    "onRoomUserDeviceUpdate() called with: updateType = [" + updateType + "], userID = [" + userID
                        + "], roomID = [" + roomID + "]");
                if (updateType == ZegoDeviceUpdateType.cameraOpen) {
                    setRemoteViewVisible(true);
                } else if (updateType == ZegoDeviceUpdateType.cameraClose) {
                    setRemoteViewVisible(false);
                }
            }

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {

            }
        });


    }


    private void setRemoteViewVisible(boolean visible) {
        binding.remoteTexture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExpressManager.getInstance().leaveRoom();
        finish();
    }
}