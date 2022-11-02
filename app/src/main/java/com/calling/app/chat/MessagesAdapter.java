package com.calling.app.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calling.app.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>{

    private final List<MessagesList> messagesLists;
    private final Context context;

    public MessagesAdapter(List<MessagesList> messagesLists, Context context) {
        this.messagesLists = messagesLists;
        this.context = context;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_adapter_layout,null));
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return messagesLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profilepic;
        private TextView name;
        private TextView lastMessage;
        private TextView unseenMessage;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilepic = itemView.findViewById(R.id.profilePic);
            name = itemView.findViewById(R.id.nameHome);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            unseenMessage = itemView.findViewById(R.id.unseenMessages);
        }
    }

}
