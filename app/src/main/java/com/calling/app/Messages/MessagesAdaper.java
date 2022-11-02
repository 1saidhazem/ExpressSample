package com.calling.app.Messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calling.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;
public class MessagesAdaper extends RecyclerView.Adapter<MessagesAdaper.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> mChatList;
    private String imageUrl;

    FirebaseUser firebaseUser;

    public MessagesAdaper(Context context, List<Chat> mChatList, String imageUrl) {
        this.context = context;
        this.mChatList = mChatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessagesAdaper.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new MessagesAdaper.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new MessagesAdaper.ViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChatList.get(position);
        holder.txtShowMessage.setText(chat.getMessage());
        if(imageUrl.equals("")){
            holder.profile_URLMessage.setImageResource(R.drawable.user);
        }else{
            Picasso.get().load(imageUrl).into(holder.profile_URLMessage);
        }
    }



    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtShowMessage;
        public ImageView profile_URLMessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShowMessage = itemView.findViewById(R.id.showMessage);
            profile_URLMessage = itemView.findViewById(R.id.profile_image_messageText);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }

    }
}
