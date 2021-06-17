package com.dev.salim.ChatApp.Ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.dev.salim.ChatApp.Ui.activities.ImageViewerActivity;
import com.dev.salim.ChatApp.Ui.modules.ChatMessages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ChatHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    Context mContext;
    List<ChatMessages> mMessageList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public MessagesAdapter(Context mContext, List<ChatMessages> mMessageList, String image) {
        this.mContext = mContext;
        this.mMessageList = mMessageList;
        this.imageUrl = image;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_message_right, parent, false);
            return new ChatHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_message_left, parent, false);
            return new ChatHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {

        ChatMessages messages = mMessageList.get(position);

        if (messages.getType().equals("text")) {
            holder.mMessage.setText(messages.getMessage());
            holder.msgImage.setVisibility(View.GONE);
        }else {
            Glide.with(mContext).load(messages.getMessage()).into(holder.msgImage);
            holder.mMessage.setVisibility(View.GONE);
        }

        if (imageUrl.equals("default")) {
            holder.mProfile.setImageResource(R.drawable.ic_image);
        } else {
            Glide.with(mContext).load(imageUrl).into(holder.mProfile);
        }

        if (position == mMessageList.size() - 1) {
            if (messages.isIsseen()) {
                holder.tvSeen.setText("Seen");
            } else {
                holder.tvSeen.setText("Delivered");
            }
        } else {
            holder.tvSeen.setVisibility(View.GONE);
        }

        holder.msgImage.setOnClickListener(v ->
                mContext.startActivity(new Intent(mContext, ImageViewerActivity.class)
                        .putExtra("image", messages.getMessage())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        );

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessageList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else return MSG_TYPE_LEFT;
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        ConstraintLayout msgCard;
        CircleImageView mProfile;
        TextView mMessage, tvSeen;
        ImageView msgImage;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            msgCard = itemView.findViewById(R.id.msgCard);
            mProfile = itemView.findViewById(R.id.ivChatProfile);
            mMessage = itemView.findViewById(R.id.tvMessage);
            tvSeen = itemView.findViewById(R.id.tvSeen);
            msgImage = itemView.findViewById(R.id.msgImage);
        }
    }
}
