package com.dev.salim.ChatApp.Ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.salim.ChatApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.tuyenmonkey.mkloader.MKLoader;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    EditText etEmail, etPassword;
    TextView tvRegister, tvForget;
    Button mLogin;
    MKLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        mLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.create);
        tvForget = findViewById(R.id.tvForget);
        loader = findViewById(R.id.loadingLogin);

        mLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForget.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                loginUser();
                break;

            case R.id.create:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;

            case R.id.tvForget:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                finish();
                break;

        }
    }

    private void loginUser() {
        loader.setVisibility(View.VISIBLE);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        loader.setVisibility(View.GONE);
                        String error = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    } else {
                        loader.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Login Successfully !", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();

                        //                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        //                    assert user != null;
                        //                    String userId = user.getUid();
                        //                    if (user.isEmailVerified()) {
                        //                        loader.setVisibility(View.GONE);
                        //                        Toast.makeText(LoginActivity.this, "Login Successfully !", Toast.LENGTH_LONG).show();
                        //                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        //                        startActivity(i);
                        //                        finish();
                        //                    } else {
                        //                        loader.setVisibility(View.GONE);
                        //                        user.sendEmailVerification();
                        //                        Toast.makeText(LoginActivity.this, "Check Your email to verify your account !", Toast.LENGTH_LONG).show();
                        //                    }
                    }
                });
    }
}