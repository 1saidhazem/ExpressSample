package com.calling.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.calling.app.VoiceCall.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText EmailEditText, passwordEditText;
    TextView signUPTextView;
    Button loginButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    FirebaseUser firebaseUser;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Variable Firebase and connect it with Database
        mAuth = FirebaseAuth.getInstance();


        // check if user sign in before or first time
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
            return;
        }



        // Add underline for Sign UP textView Programtically
        signUPTextView = (TextView) findViewById(R.id.signUP_TextView);
        signUPTextView.setPaintFlags(signUPTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // EditText Intialaizing
        EmailEditText = findViewById(R.id.EmailEditText);
        passwordEditText = findViewById(R.id.PasswordEditText);


        // Login Button Intialiazing
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginUsers();


            }
        });


        // SignUP Button Intialiazing
        signUPTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);

            }

        });


        String deviceId = getDeviceId(this);

        reference = FirebaseDatabase.getInstance().getReference("devices Id").child(deviceId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.getValue().equals(deviceId)){
                        startActivity(new Intent(MainActivity.this, cantLoginMainActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // Initialize Progress Bar
        progressBar = findViewById(R.id.progressBarLogin);

    }

    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void loginUsers() {

        // Get Data From EditText
        String email = EmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty()) {
            EmailEditText.setError("Required");
            EmailEditText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Required");
            passwordEditText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            EmailEditText.setError(" Email not valid");
            EmailEditText.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //redirect User to profile
                if (task.isSuccessful()) {
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    startActivity(intent);
                    finish();
                    progressBar.setVisibility(View.GONE);

                } else {
                    progressBar.setVisibility(View.GONE);

                }
            }
        });

    }


}