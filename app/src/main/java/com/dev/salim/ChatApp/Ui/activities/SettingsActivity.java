package com.dev.salim.ChatApp.Ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.dev.salim.ChatApp.Ui.modules.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    CircleImageView mProfile;
    TextView tvName;
    RadioButton swStatusOn, swStatusOff;
    ImageView ivBack, ivCamera;

    Uri imageUri;
    String status;
    private static final int PICK_IMAGE = 1;
    StorageReference storageReference;
    StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        swStatusOn = findViewById(R.id.swStatusOn);
        swStatusOff = findViewById(R.id.swStatusOff);
        ivBack = findViewById(R.id.ivBack);
        ivCamera = findViewById(R.id.ivCamera);

        swStatusOn.setOnClickListener(this);
        swStatusOff.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivCamera.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Profile Images");
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                tvName.setText(user.getName());
                if (user.getImageUrl().equals("default")) {
                    mProfile.setImageResource(R.drawable.ic_image);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(mProfile);
                }

                if (user.getStatus().equals("online")) {
                    status = "online";
                    swStatusOn.setChecked(true);
                    swStatusOff.setChecked(false);
                } else {
                    status = "offline";
                    swStatusOn.setChecked(false);
                    swStatusOff.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint("IntentReset")
    private void openGalleryIntent() {
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGallery.setType("image/*");
        startActivityForResult(Intent.createChooser(openGallery, " اختر صورة "), PICK_IMAGE);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference filePath = storageReference.child(firebaseUser.getUid() + ".jpg");
            uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                    Log.e("mUri", "uploadImage: " + mUri);
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageUrl", mUri);
                    reference.updateChildren(map);

                    progressDialog.dismiss();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed !", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Image Selected !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {

            imageUri = data.getData();
            Log.e("imageUri", "onActivityResult: " + data.getData().toString());
            Log.e("imagePath", "onActivityResult: " + imageUri.getPath());

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1, 1)
//                    .start(this);
        }
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//          if (resultCode == RESULT_OK){
//              Uri resultUri = result.getUri();
//              reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//              HashMap<String, Object> map = new HashMap<>();
//              map.put("imageUrl", resultUri);
//              reference.updateChildren(map);

//              if (uploadTask != null && uploadTask.isInProgress()){
//                  Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
//              }else{
//                  uploadImage();
//              }
//          }
//        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.swStatusOn:
                if (swStatusOn.isChecked()) {
                    Log.e("TAG", "onClick: Status >> " + status);
                    status("online");
                    swStatusOn.setChecked(true);
                    swStatusOff.setChecked(false);

                } else {
                    Log.e("TAG", "onClick: Status >> " + status);
                    status("offline");
                    swStatusOn.setChecked(false);
                    swStatusOff.setChecked(true);
                }
                break;
            case R.id.swStatusOff:
                if (swStatusOff.isChecked()) {
                    Log.e("TAG", "onClick: Status >> " + status);
                    status("offline");
                    swStatusOn.setChecked(false);
                    swStatusOff.setChecked(true);

                } else {
                    Log.e("TAG", "onClick: Status >> " + status);
                    status("online");
                    swStatusOn.setChecked(true);
                    swStatusOff.setChecked(false);
                }
                break;

            case R.id.ivBack:
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("status", status)
                        .putExtra("fragmentPosition", 0));
                finish();
                break;
            case R.id.ivCamera:
                openGalleryIntent();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(status);
    }
}