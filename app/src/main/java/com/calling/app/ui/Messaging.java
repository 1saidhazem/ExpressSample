package com.calling.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.Utils;
import com.calling.example.express.ExpressManager;
import com.calling.example.express.ZegoMediaOptions;
import com.calling.example.ringtone.RingtoneManager;
import com.calling.example.ui.ReceiveCallDialog;
import com.calling.example.ui.ReceiveCallView;
import com.calling.app.HttpClient;
import com.calling.app.Messages.Chat;
import com.calling.app.Messages.MessagesAdaper;
import com.calling.app.Notification.AllConstants;
import com.calling.app.R;
import com.calling.app.VoiceCall.Users;
import com.calling.app.cloudmessage.CloudMessage;
import com.calling.app.cloudmessage.CloudMessageManager;
import com.calling.app.cloudmessage.NotificationHelper;
import com.calling.app.databinding.ActivityLoginBinding;
import com.calling.app.express.AppCenter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.permissionx.guolindev.PermissionX;
import com.squareup.picasso.Picasso;
import com.calling.app.HttpClient.HttpResult;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.entity.ZegoUser;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class Messaging extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    TextView userName;
    CircleImageView imageViewProfile;
    EditText textSendEditText;
    ImageButton sendMessage;
    Chat chat;

    String token;


    private ActivityLoginBinding binding;
    String selfID = FirebaseAuth.getInstance().getCurrentUser().getUid();// String.valueOf(new Random().nextInt(10000));            //  my user ID
    String selfName = "User" + selfID;
    String selfIcon = "https://img.icons8.com/color/48/000000/avatar.png";


    MessagesAdaper messagesAdaper;
    List<Chat> chatList;
    RecyclerView recyclerView;
    public static String userId;
    private static final String TAG = "Messaging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


        Intent intent = getIntent();
        userId = intent.getStringExtra("IDUser");
        Log.e("Fadykdsl", "Hellos" + userId);


        // Beginning
        ImageView callImageButton = findViewById(R.id.callImageButton);
//        callImageBtton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Initial Dail
//
//
////                Intent intent = new Intent(Messaging.this,Sinchservice.class);
////                bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
////
////                Intent i = new Intent(Messaging.this, Sinchuser.class);
////                i.putExtra("IDUsers",userId);
////                startActivity(i);
////
//
//
//            }
//        });

        Utils.init(getApplication());

        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(Messaging.this)
                        .permissions(Manifest.permission.RECORD_AUDIO)
                        .request((allGranted, grantedList, deniedList) -> {
                            if (allGranted) {
                                CloudMessage cloudMessage = new CloudMessage();
                                cloudMessage.targetUserID = userId;   // binding.targetUserId.getText().toString();    // ID User
                                cloudMessage.roomID = selfID;
                                cloudMessage.callType = "Audio";
                                cloudMessage.callerUserID = selfID;
                                cloudMessage.callerUserName = selfName;
                                cloudMessage.callerIconUrl = selfIcon;

                                HttpClient.getInstance().callUserByCloudMessage(cloudMessage, new HttpResult() {
                                    @Override
                                    public void onResult(int errorCode, String result) {
                                        if (errorCode == 0) {
                                            joinRoom(cloudMessage.roomID, cloudMessage.callerUserID,
                                                    cloudMessage.callerUserName);
                                        }
                                    }
                                });
                            }
                        });
            }
        });

        ExpressManager.getInstance().createEngine(getApplication(), AppCenter.appID);
        PermissionX.init(this)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request((allGranted, grantedList, deniedList) -> {
                });

        CloudMessageManager.getInstance().setListener(new CloudMessageManager.CloudMessageListener() {
            @Override
            public void onMessageReceived(CloudMessage cloudMessage) {
                onCloudMessageReceived(cloudMessage,Messaging.this);
            }
        });
        CloudMessage cloudMessage = CloudMessageManager.getInstance().getCloudMessage();
        if (cloudMessage != null) {
            onCloudMessageReceived(cloudMessage,Messaging.this);
            CloudMessageManager.getInstance().clearCloudMessage();
        }
        getAndRegisterFCMToken();


        // Initializing Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });


        // Initializing Views
        userName = findViewById(R.id.nameUserMessagingToolbar);
        imageViewProfile = findViewById(R.id.imageViewMessaginToolbar);


        setUserNameForToolbar();

        // Intializing RecyclerView
        recyclerView = findViewById(R.id.showMessageRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //recyclerView.setAdapter(messagesAdaper);
        userName = findViewById(R.id.nameUserMessagingToolbar);
        imageViewProfile = findViewById(R.id.imageViewMessaginToolbar);


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                Log.d(TAG, "Hello SADAsd " + user.getUrl_profile());

                if (user.getUrl_profile().equals("")) {
                    imageViewProfile.setImageResource(R.drawable.user);
                } else {
                    Picasso.get().load(user.getUrl_profile()).into(imageViewProfile);


                }
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                readMesage(firebaseUser.getUid(), userId, user.getUrl_profile());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        sendMessage = findViewById(R.id.sendMessageIcon);
        textSendEditText = findViewById(R.id.textMessageEditText);

        // For send message to another user
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgText = textSendEditText.getText().toString().trim();
                if (!msgText.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, msgText);
                    textSendEditText.getText().clear();


                    // Get on name and image for notification to my id and

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users user = snapshot.getValue(Users.class);
                            Log.d(TAG, "Hello SADAsd " + user.getUrl_profile());

                            if (user.getUrl_profile().equals("")) {
                                //   imageViewProfile.setImageResource(R.drawable.user);
                            } else {

                                getToken(msgText, FirebaseAuth.getInstance().getUid(), user.getUrl_profile(), user.name);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });


                } else {
                    textSendEditText.setError("You can't send empty message");

                }

            }
        });


        ImageView removeChatImageButton = findViewById(R.id.removeChatImageButton);

        removeChatImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeChat();
            }
        });


    }


    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        reference.child("Chats").push().setValue(hashMap);

    }

    private void readMesage(String myID, String userid, String imageUrl) {

        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat1 = dataSnapshot.getValue(Chat.class);
                    if (chat1.getReceiver().equals(myID) && chat1.getSender().equals(userid) || chat1.getReceiver().equals(userid) && chat1.getSender().equals(myID)) {

                        chatList.add(chat1);
                        //chatList.add(new Chat(myID, userid, chat1.getMessage()));
                        //
                    }
                    messagesAdaper = new MessagesAdaper(Messaging.this, chatList, imageUrl);
                    recyclerView.setAdapter(messagesAdaper);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void setUserNameForToolbar() {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if (user != null) {
                    String name = user.name;
                    userName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    // Beginning

    private void getAndRegisterFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();
                // Log and toast
                Log.w(TAG, "Fetching FCM registration token " + token);


                HttpClient.getInstance().registerFCMToken(selfID, token, new HttpResult() {
                    @Override
                    public void onResult(int errorCode, String result) {

                    }
                });
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        NotificationHelper.cancelNotification(this);
//        recyclerView.setAdapter(messagesAdaper);
//    }

    private void joinRoom(String roomID, String userID, String username) {
        if (!checkAppID()) {
            return;
        }
        //  binding.loading.setVisibility(View.VISIBLE);
        ZegoUser user = new ZegoUser(userID, username);

        HttpClient.getInstance().getRTCToken(selfID, new HttpResult() {
            @Override
            public void onResult(int errorCode, String token) {
                if (errorCode == 0) {
                    int mediaOptions = ZegoMediaOptions.autoPlayAudio |
                            ZegoMediaOptions.publishLocalAudio;
                    ExpressManager.getInstance().joinRoom(roomID, user, token, mediaOptions,
                            new IZegoRoomLoginCallback() {
                                @Override
                                public void onRoomLoginResult(int errorCode, JSONObject jsonObject) {
                                    //              binding.loading.setVisibility(View.GONE);
                                    if (errorCode == 0) {
                                        startActivity(new Intent(Messaging.this, CallActivity.class));
                                    }
                                }
                            });
                } else {
                    //    binding.loading.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean checkAppID() {
        return AppCenter.appID != 0L;
    }

    public void onCloudMessageReceived(CloudMessage cloudMessage, Context context) {
        if (context != null){
            RingtoneManager.playRingTone(context);
        }

        ReceiveCallView.ReceiveCallData callData = new ReceiveCallView.ReceiveCallData();
        callData.callUserName = cloudMessage.callerUserName;
        callData.callUserID = cloudMessage.callerUserID;
        callData.callUserIcon = cloudMessage.callerIconUrl;
        callData.callType = "Audio".equals(cloudMessage.callType)
                ? ReceiveCallView.ReceiveCallData.Video : ReceiveCallView.ReceiveCallData.Voice;
        ReceiveCallView view = new ReceiveCallView(this);
        view.setReceiveCallData(callData);
        ReceiveCallDialog dialog = new ReceiveCallDialog(this, view);
        view.setListener(new ReceiveCallView.OnReceiveCallViewClickedListener() {
            @Override
            public void onAcceptAudioClicked() {
                dialog.dismiss();
                joinRoom(cloudMessage.roomID, selfID, selfName);
            }

            @Override
            public void onDeclineClicked() {
                dialog.dismiss();
            }

            @Override
            public void onWindowClicked() {
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                RingtoneManager.stopRingTone();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RingtoneManager.stopRingTone();
    }


    private void getToken(String message, String hisID, String myImage, String myName) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if (user != null) {
                    token = user.getToken();

                    JSONObject to = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        data.put("title", myName);
                        data.put("message", message);
                        data.put("hisID", FirebaseAuth.getInstance().getUid());
                        data.put("hisImage", myImage);


                        to.put("to", token);
                        to.put("data", data);

                        sendNotification(to);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(JSONObject to) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AllConstants.NOTIFICATION_URL, to, response -> {
            Log.d("notification", "sendNotification: " + response);
        }, error -> {
            Log.d("notification", "sendNotification: " + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + AllConstants.SERVER_KEY);
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    private void removeChat() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String pushKey = snapshot.getKey();
                    ref.child("Chats").child(pushKey).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.getKey().equals("sender") && snapshot.getValue().equals(firebaseUser.getUid())) {
                                ref.child("Chats").child(pushKey).removeValue();
                                finish();
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}







