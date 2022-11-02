package com.calling.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import com.calling.app.HttpClient.HttpResult;
import com.calling.example.express.ExpressManager;
import com.calling.example.express.ZegoMediaOptions;
import com.calling.example.ringtone.RingtoneManager;
import com.calling.example.ui.ReceiveCallDialog;
import com.calling.example.ui.ReceiveCallView;
import com.calling.app.cloudmessage.CloudMessage;
import com.calling.app.cloudmessage.CloudMessageManager;
import com.calling.app.databinding.ActivityProfileBinding;
import com.calling.app.express.AppCenter;
import com.calling.app.ui.CallActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.permissionx.guolindev.PermissionX;
import org.json.JSONObject;
import de.hdodenhof.circleimageview.CircleImageView;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.entity.ZegoUser;

public class Profile extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    TextView userName;
    CircleImageView imageViewProfile;
    EditText textSendEditText;

    private ActivityProfileBinding binding;
    String selfID = FirebaseAuth.getInstance().getCurrentUser().getUid()   ;// String.valueOf(new Random().nextInt(10000));            //  my user ID
    String selfName = "User" + selfID;
    String selfIcon = "https://img.icons8.com/color/48/000000/avatar.png";

    public static String userId;
    private static final String TAG = "Profile";


    ChipNavigationBar chipNavigationBar;
    EditText editTextSearchbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);






        chipNavigationBar = findViewById(R.id.bottom_nav_bar);

        chipNavigationBar.setItemSelected(R.id.Home_profile,
                true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_Container_FrameLayout,
                        new Home_Fragment()).commit();
        bottomMenu();


        editTextSearchbar = findViewById(R.id.search_barEditTextHome);




    }

    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {

                    Fragment fragment = null;
                    switch (i) {
                        case R.id.Home_profile:
                            fragment = new Home_Fragment();
                            break;
                        //       case R.id.bottom_nav_book:
                        //          fragment = new BookFragment();
                        //         break;
                        case R.id.nav_account:
                            fragment = new account_fragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_Container_FrameLayout,
                                    fragment).commit();
                }

        });


        // Initializing call Voice

        ExpressManager.getInstance().createEngine(getApplication(), AppCenter.appID);
        PermissionX.init(this)
                .permissions( Manifest.permission.RECORD_AUDIO)
                .request((allGranted, grantedList, deniedList) -> {
                });

        CloudMessageManager.getInstance().setListener(new CloudMessageManager.CloudMessageListener() {
            @Override
            public void onMessageReceived(CloudMessage cloudMessage) {
                onCloudMessageReceived(cloudMessage);
            }
        });
        CloudMessage cloudMessage = CloudMessageManager.getInstance().getCloudMessage();
        if (cloudMessage != null) {
            onCloudMessageReceived(cloudMessage);
            CloudMessageManager.getInstance().clearCloudMessage();
        }


        getAndRegisterFCMToken();
    }


// berginning Call


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
//
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
                            ZegoMediaOptions.publishLocalAudio ;
                    ExpressManager.getInstance().joinRoom(roomID, user, token, mediaOptions,
                            new IZegoRoomLoginCallback() {
                                @Override
                                public void onRoomLoginResult(int errorCode, JSONObject jsonObject) {
                                    //              binding.loading.setVisibility(View.GONE);
                                    if (errorCode == 0) {
                                        startActivity(new Intent(Profile.this, CallActivity.class));
                                    } else {
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

    public void onCloudMessageReceived(CloudMessage cloudMessage) {
        RingtoneManager.playRingTone(this);

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


}


