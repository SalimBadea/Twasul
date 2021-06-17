package com.dev.salim.ChatApp.Ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dev.salim.ChatApp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText email;
    Button send;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.forget_email);
        send = findViewById(R.id.btnReset);

        send.setOnClickListener(v -> {
            String Email = email.getText().toString();

            if (Email.equals("")){
                Toast.makeText(this, "Email Field is required !", Toast.LENGTH_SHORT).show();
            }else{

                mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(this, "Please check your email !", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }else {
                        String error = task.getException().getMessage();
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}