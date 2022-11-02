package com.calling.app;

import static android.app.Activity.RESULT_OK;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.calling.app.VoiceCall.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class account_fragment extends Fragment {

    String userName,password;
    String name, email,userId;
    String token ;
    String URL_profile;

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference reference,reference2;
    String userID;
    CircleImageView circleImageView;

    SharedPreferences preferences;

    // Uri indicates, where the image will be picked from
    private Uri filePath;


    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;

    public account_fragment() {}

    public static account_fragment newInstance() {
        return new account_fragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // to retrieve Data From Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();


        String deviceId = getDeviceId(view.getContext());
        HashMap<String,String> data = new HashMap<>();
        data.put("deviceId",deviceId);
        reference2 = FirebaseDatabase.getInstance().getReference("devices Id").child(deviceId);


        reference2.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });




        final TextView logout = view.findViewById(R.id.logOutTextView);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
            }
        });



        final TextView nameUserTextView = view.findViewById(R.id.nameUserProfileTextView);
        final TextView userIDTextView = view.findViewById(R.id.idUserProfileTextView);

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if (user != null) {
                    name = user.name;
                    email = user.email;
                    userId = user.userID;

                    nameUserTextView.setText(name);
                    String firstTenChars = userId.substring(0, 10);

                    userIDTextView.setText(firstTenChars);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // to show Dialog For edit User Name
        TextView editUserTextView = view.findViewById(R.id.editUserTextView);
        editUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showDialog();

                }
        });

        circleImageView = view.findViewById(R.id.profile_image);
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageReference = storageReference.child("Users/"+mAuth.getCurrentUser().getUid() +"images/ ");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {

                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            token = task.getResult();
                            Log.e("", "Register token 2" + task.getResult()); // com.google.firebase.auth.internal.zzr@241dbd1
                        }
                    }
                });
                Picasso.get().load(uri).into(circleImageView);
                if(uri != null){
                     URL_profile = uri.toString();
                     Users user = new Users(name,email,URL_profile,userID,token);
                     reference.child(userID).setValue(user);
                    writeNewUser(name,email);

                }
            }
        });




        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
                uploadImage();
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(circleImageView);
                        if(uri != null){
                            URL_profile = uri.toString();
                            Users user = new Users(name,email,URL_profile,userID,token);
                            reference.child(userID).setValue(user);
                            writeNewUser(name,email);

                        }
                    }
                });


            }


        });


        super.onViewCreated(view, savedInstanceState);
    }

    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }



    protected String showDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.input_edit_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Edit");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText userNameEdit = dialogView.findViewById(R.id.edit_username);
                EditText passwordEdit = dialogView.findViewById(R.id.edit_password);
                userName = userNameEdit.getText().toString().trim();
                 //password = passwordEdit.getText().toString();
                Log.e("FadyP","No Value"+userName);

                writeNewUser(userName, email);

            }
        }).setNegativeButton("cancel" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText userNameEdit = dialogView.findViewById(R.id.edit_username);
                userName = userNameEdit.getText().toString().trim();
                Log.e("FadyN",""+userName);

            }
        });
        dialog.show();
        return userName;

    }



    private void writeNewUser(String name, String email) {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {

            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                    Log.e("", "Register token 2" + task.getResult()); // com.google.firebase.auth.internal.zzr@241dbd1
                }
            }
        });


        Users user = new Users(name, email, URL_profile,userID, token);
        Map<String, Object> postValues = user.toMap();
        reference.child(userID).setValue(postValues);
    }



        // Select Image method
        private void SelectImage() {

            // Defining Implicit Intent to mobile gallery
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
        }

        // Override onActivityResult method
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {

            super.onActivityResult(requestCode,
                    resultCode,
                    data);
            // checking request code and result code
            // if request code is PICK_IMAGE_REQUEST and
            // resultCode is RESULT_OK
            // then set image in the image view
            if (requestCode == PICK_IMAGE_REQUEST
                    && resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                // Get the Uri of data
                filePath = data.getData();
                try {

                    // Setting image on image view using Bitmap
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getActivity().getContentResolver(),
                                    filePath);
                    circleImageView.setImageBitmap(bitmap);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
            }
            uploadImage();
        }


    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            // Defining the child of storageReference

            // adding listeners on upload
            // or failure of image
            storageReference .putFile(filePath).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Error, Image not uploaded
                            progressDialog.dismiss();
                        }
                    }).addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });
        }
    }


}