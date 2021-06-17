package com.dev.salim.ChatApp.Ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.dev.salim.ChatApp.Ui.activities.ChatActivity;
import com.dev.salim.ChatApp.Ui.modules.ChatMessages;
import com.dev.salim.ChatApp.Ui.modules.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersHolder> {

    Context mContext;
    List<User> userList;
    boolean isChat;

    String lastMessage;

    public UsersAdapter(Context mContext, List<User> mList, boolean isChat) {
        this.mContext = mContext;
        this.userList = mList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new UsersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder holder, int position) {

        final User user = userList.get(position);
        holder.userName.setText(user.getName());

        if (user.getImageUrl().equals("default")) {
            holder.userImage.setImageResource(R.drawable.ic_image);
        } else {
            Glide.with(mContext)
                    .load(user.getImageUrl())
                    .into(holder.userImage);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.online.setVisibility(View.VISIBLE);
                holder.offline.setVisibility(View.GONE);
            } else {
                holder.online.setVisibility(View.GONE);
                holder.offline.setVisibility(View.VISIBLE);
            }
            seeLastMessage(user.getId(), holder.lastMsg);
        } else {
            holder.online.setVisibility(View.GONE);
            holder.offline.setVisibility(View.GONE);
            holder.lastMsg.setVisibility(View.GONE);
        }

        holder.userCard.setOnClickListener(v ->
                mContext.startActivity(new Intent(mContext, ChatActivity.class)
                        .putExtra("userId", user.getId()))
        );
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UsersHolder extends RecyclerView.ViewHolder {

        ConstraintLayout userCard;
        TextView userName, lastMsg;
        CircleImageView userImage, online, offline;

        public UsersHolder(@NonNull View itemView) {
            super(itemView);

            userCard = itemView.findViewById(R.id.cvUser);
            userName = itemView.findViewById(R.id.tvUserName);
            lastMsg = itemView.findViewById(R.id.tvLastMessage);
            userImage = itemView.findViewById(R.id.ivUserProfile);
            online = itemView.findViewById(R.id.ivUserOnline);
            offline = itemView.findViewById(R.id.ivUserOffline);
        }
    }

    private void seeLastMessage(String userId, TextView last_msg) {
        lastMessage = "default";

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessages chatMessages = dataSnapshot.getValue(ChatMessages.class);

                    if (chatMessages.getReceiver().equals(firebaseUser.getUid()) && chatMessages.getSender().equals(userId) ||
                            chatMessages.getSender().equals(firebaseUser.getUid()) && chatMessages.getReceiver().equals(userId)) {
                        lastMessage = chatMessages.getMessage();
                    }
                }

                switch (lastMessage) {
                    case "default":
                        last_msg.setText("no Message");
                        break;
                    default:
                        if (lastMessage.startsWith("http") || lastMessage.startsWith("https")) {
                            last_msg.setText("photo message");
                        }else{
                            last_msg.setText(lastMessage);
                        }
                        break;
                }

                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
