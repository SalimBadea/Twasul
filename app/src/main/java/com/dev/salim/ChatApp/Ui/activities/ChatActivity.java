package com.dev.salim.ChatApp.Ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.dev.salim.ChatApp.Ui.adapters.MessagesAdapter;
import com.dev.salim.ChatApp.Ui.modules.ChatMessages;
import com.dev.salim.ChatApp.Ui.modules.User;
import com.dev.salim.ChatApp.Utilities.Notifications.Data;
import com.dev.salim.ChatApp.Utilities.Notifications.MyResponse;
import com.dev.salim.ChatApp.Utilities.Notifications.Sender;
import com.dev.salim.ChatApp.Utilities.Notifications.Token;
import com.dev.salim.ChatApp.network.APIService;
import com.dev.salim.ChatApp.network.Client;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    CircleImageView mProfile, ivChatOnline, ivChatOffline;
    TextView mUserName;
    EditText etMessage;
    ImageView mBack, ibnSend, ibnAttach;

    String userId, status;

    MessagesAdapter messagesAdapter;
    RecyclerView rvMessages;
    List<ChatMessages> messageList;

    ValueEventListener seenListener;

    Uri imageUri;
    String checker = "";
    private static final int PICK_IMAGE = 1;
    StorageReference storageReference;
    StorageTask uploadTask;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mBack = findViewById(R.id.ivBack);
        mUserName = findViewById(R.id.tvName);
        mProfile = findViewById(R.id.ivProfile);
        ivChatOnline = findViewById(R.id.ivChatOnline);
        ivChatOffline = findViewById(R.id.ivChatOffline);
        ibnSend = findViewById(R.id.ibnSend);
        ibnAttach = findViewById(R.id.ibnAttach);
        etMessage = findViewById(R.id.etMessage);
        rvMessages = findViewById(R.id.rvMessages);

        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = getIntent().getStringExtra("userId");
        Log.e("TAG", "onCreate: userId >> " + userId);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("Images Files");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                mUserName.setText(user.getName());
                status = user.getStatus();
                Log.e("ImageUrl", "onDataChange: " + user.getImageUrl());
                if (user.getImageUrl().equals("default")) {
                    mProfile.setImageResource(R.drawable.ic_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(mProfile);
                }
                if (user.getStatus().equals("online")) {
                    ivChatOnline.setVisibility(View.VISIBLE);
                    ivChatOffline.setVisibility(View.GONE);
                } else {
                    ivChatOnline.setVisibility(View.GONE);
                    ivChatOffline.setVisibility(View.VISIBLE);
                }
                readMessages(firebaseUser.getUid(), userId, user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        mBack.setOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, MainActivity.class)
                    .putExtra("fragmentPosition", 0)
                    .putExtra("status", status));
            finish();
        });

        ibnSend.setOnClickListener(v -> {
            notify = true;
            String msg = etMessage.getText().toString();
            if (!msg.isEmpty()) {
                sendMessage(firebaseUser.getUid(), userId, msg);
            } else {
                Toast.makeText(this, "Can't send Empty Message !", Toast.LENGTH_SHORT).show();
            }
            etMessage.setText("");
        });

        ibnAttach.setOnClickListener(v -> {

            CharSequence options[] = new CharSequence[]{"Image", "PDF File", "Word File"};

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Select File Type");
            builder.setItems(options, (dialog, i) -> {
                if (i == 0) {
                    checker = "image";
                    openGalleryIntent();
                }
                if (i == 1) {
                    checker = "pdf";
                }
                if (i == 2) {
                    checker = "docx";
                }
            });
            builder.show();
        });

        seenMessage(userId);
    }

    private void currentUser(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userId);
        editor.apply();
    }

    @SuppressLint("IntentReset")
    private void openGalleryIntent() {
        Intent openGallery = new Intent(Intent.ACTION_GET_CONTENT);
        openGallery.setType("image/*");
        startActivityForResult(Intent.createChooser(openGallery, " اختر صورة "), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
            progressDialog.setMessage("Please wait, photo is sending...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            if (imageUri != null) {
                Log.e("imageUri", "uploadImage: " + imageUri.getPath());
                if (checker.equals("image")) {
                    StorageReference filePath = storageReference.child(firebaseUser.getUid() + ".jpg");
                    uploadTask = filePath.putFile(imageUri);
                    uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Image sent successfully !", Toast.LENGTH_SHORT).show();
                            Uri downloadUri = task.getResult();
                            String mUri = downloadUri.toString();
                            Log.e("mUri", "uploadImage: " + mUri);
                            reference = FirebaseDatabase.getInstance().getReference("Chats");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("sender", firebaseUser.getUid());
                            map.put("receiver", userId);
                            map.put("message", mUri);
                            map.put("isseen", false);
                            map.put("type", checker);
                            reference.push().setValue(map);

                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(ChatActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Image Selected !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessages chatMessages = dataSnapshot.getValue(ChatMessages.class);
//                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                    if (chatMessages.getReceiver().equals(firebaseUser.getUid()) && chatMessages.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        hashMap.put("time", System.currentTimeMillis());
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        checker = "text";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("type", checker);

        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify)
                    sendNotification(receiver, user.getName(), msg);
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, String name, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.drawable.splash, name + ": " + msg, "New Message", userId);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages(String mId, String userId, String imageUrl) {
        messageList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessages message = dataSnapshot.getValue(ChatMessages.class);
                    ChatMessages chatMessages = new ChatMessages();
                    String reciever = message.getReceiver();
                    String sender = message.getSender();
                    String msg = message.getMessage();
                    String type = message.getType();
                    boolean isSeen = message.isIsseen();

                    assert message != null;
                    if (message.getReceiver().equals(mId) && message.getSender().equals(userId) ||
                            message.getSender().equals(mId) && message.getReceiver().equals(userId)) {
                        chatMessages.setReceiver(reciever);
                        chatMessages.setSender(sender);
                        chatMessages.setMessage(msg);
                        chatMessages.setType(type);
                        chatMessages.setIsseen(isSeen);
                        messageList.add(chatMessages);
                    }
                }
                messagesAdapter = new MessagesAdapter(getApplicationContext(), messageList, imageUrl);
                rvMessages.setAdapter(messagesAdapter);
                if (messageList.size() > 0)
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}