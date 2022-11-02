package com.calling.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.calling.app.VoiceCall.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class Search_Fragment extends Fragment {


    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    ImageView btnSearchImageView;
    private EditText searchEditTextUser;
    private FirebaseRecyclerOptions<Users> options;
    private FirebaseRecyclerAdapter adapters;
    private RecyclerView messagesRecyclerView;


    public Search_Fragment() {}

    public static Search_Fragment newInstance() {
        return new Search_Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home_, container, false);
    }

}





