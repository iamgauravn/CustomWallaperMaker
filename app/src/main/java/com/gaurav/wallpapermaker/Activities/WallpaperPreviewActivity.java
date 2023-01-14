package com.gaurav.wallpapermaker.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.gaurav.wallpapermaker.R;

public class WallpaperPreviewActivity extends AppCompatActivity {

    ImageView iv_image, iv_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        init();

        Glide.with(WallpaperPreviewActivity.this).load(getIntent().getStringExtra("path")).into(iv_image);

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

    }

    private void init() {

        iv_image = findViewById(R.id.iv_image);
        iv_exit = findViewById(R.id.iv_exit);

    }

}