package com.calling.example.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.calling.app.VoiceCall.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import im.zego.example.databinding.LayoutReceiveCallBinding;

public class ReceiveCallView extends FrameLayout {

    private LayoutReceiveCallBinding binding;
    private OnReceiveCallViewClickedListener listener;
    private ReceiveCallData receiveCallData;
    private DatabaseReference reference;

    public ReceiveCallView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                           int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = LayoutReceiveCallBinding.inflate(LayoutInflater.from(context), this, true);
        binding.dialogCallAcceptVoice.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptAudioClicked();
            }
        });
        binding.dialogCallDecline.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeclineClicked();
            }
        });
        binding.dialogReceiveCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWindowClicked();
            }
        });
        if (receiveCallData != null) {
            setReceiveCallData(receiveCallData);
        }
    }

    public void setReceiveCallData(ReceiveCallData callData) {
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(callData.callUserID);
        this.receiveCallData = callData;

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                binding.dialogCallName.setText(user.getName());
                Glide.with(getContext()).load(callData.callUserIcon).into(binding.dialogCallIcon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void setListener(OnReceiveCallViewClickedListener listener) {
        this.listener = listener;
    }

    public interface OnReceiveCallViewClickedListener {
        void onAcceptAudioClicked();

        void onDeclineClicked();

        void onWindowClicked();
    }

    public static class ReceiveCallData {

        public String callUserName;
        public String callUserIcon;
        public String callUserID;
        public int callType;
        public static final int Video = 0;
        public static final int Voice = 1;
    }
}
