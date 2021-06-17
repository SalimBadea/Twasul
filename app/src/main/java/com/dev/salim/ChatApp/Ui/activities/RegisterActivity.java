package com.dev.salim.ChatApp.Ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.salim.ChatApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tuyenmonkey.mkloader.MKLoader;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    EditText etUserName, etEmail, etPhone, etPassword, etCPassword;
    TextView tvLogin;
    Button btnRegister;
    MKLoader mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etUserName = findViewById(R.id.user_name);
        etEmail = findViewById(R.id.email);
        etPhone = findViewById(R.id.phone);
        etPassword = findViewById(R.id.password);
        etCPassword = findViewById(R.id.confirm_password);
        tvLogin = findViewById(R.id.login);
        btnRegister = findViewById(R.id.btn_sign_up);
        mLoader = findViewById(R.id.loading);

        tvLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;

            case R.id.btn_sign_up:
                createUser();
                break;
        }
    }

    private void createUser() {
        mLoader.setVisibility(View.VISIBLE);

        final String name = etUserName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String mPassword = etCPassword.getText().toString().trim();

//        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                User user = new User(name, email, phone, "default");
//
//                FirebaseDatabase.getInstance().getReference("Users")
//                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .setValue(user).addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful()) {
//                        Toast.makeText(RegisterActivity.this, "User has been register Successfully !", Toast.LENGTH_LONG).show();
//                        mLoader.setVisibility(View.GONE);
//                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(RegisterActivity.this, "Failed to register, Try Again !", Toast.LENGTH_LONG).show();
//                        mLoader.setVisibility(View.GONE);
//                    }
//                });
//            } else {
//                Toast.makeText(RegisterActivity.this, "Failed to register !", Toast.LENGTH_LONG).show();
//                mLoader.setVisibility(View.GONE);
//            }
//        });

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        mLoader.setVisibility(View.GONE);

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        assert firebaseUser != null;
                        String userId = firebaseUser.getUid();

                        Log.e("TAG", "onComplete: " + userId);

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", userId);
                        hashMap.put("name", name);
                        hashMap.put("email", email);
                        hashMap.put("phone", phone);
                        hashMap.put("imageUrl", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("search", name.toLowerCase());

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                mLoader.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Create User SuccessFully !", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
                    } else {
                        mLoader.setVisibility(View.GONE);
                        String error = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}