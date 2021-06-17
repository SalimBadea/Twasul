package com.dev.salim.ChatApp.Ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dev.salim.ChatApp.R;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewerActivity extends AppCompatActivity {

    ImageView ivBack;
    PhotoView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ivBack = findViewById(R.id.ivBack);
        ivImage = findViewById(R.id.ivImage);

        String imageUrl = getIntent().getStringExtra("image");
        Glide.with(this).load(imageUrl).error(R.drawable.ic_image).into(ivImage);

        ivBack.setOnClickListener(v -> finish());
    }
}