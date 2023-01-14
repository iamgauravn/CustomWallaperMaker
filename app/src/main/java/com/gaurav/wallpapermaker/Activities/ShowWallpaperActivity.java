package com.gaurav.wallpapermaker.Activities;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gaurav.wallpapermaker.LiveWallpaper.LiveWallpaperService;
import com.gaurav.wallpapermaker.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ShowWallpaperActivity extends AppCompatActivity {

    RelativeLayout iv_preview, iv_setWallpaper, iv_addToFav;
    ImageView iv_exit;
    String wallpaper;
    int choice;
    TextView tv_fav;
    ImageView iv_wallpaper;
    ArrayList<String> fav_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_wallpaper);

        init();

        wallpaper = getIntent().getStringExtra("path");

        check();

        Glide.with(ShowWallpaperActivity.this).load(wallpaper).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(iv_wallpaper);

        iv_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent ie = new Intent(ShowWallpaperActivity.this, WallpaperPreviewActivity.class);
                ie.putExtra("path", wallpaper);
                startActivity(ie);

            }
        });

        iv_setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDialogBoxLiveOrNormal(wallpaper, ShowWallpaperActivity.this);

            }
        });

        iv_addToFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addToFAV(wallpaper);
                check();

            }
        });

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

    }

    private void check() {

        loadData();

        if (fav_list.contains(wallpaper)) {
            tv_fav.setText("Dislike");
        } else {
            tv_fav.setText("Favourite");
        }

    }


    private void addToFAV(String path) {

        loadData();
        if (!fav_list.contains(path) && new File(path).exists()) {

            fav_list.add(path);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

        } else {

            fav_list.remove(path);
            Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();

        }

        saveData();

    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("fav_list", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        fav_list = gson.fromJson(json, type);
        if (fav_list == null) {
            fav_list = new ArrayList<>();
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(fav_list);
        editor.putString("fav_list", json);
        editor.apply();
    }

    private void openDialogBox() {

        final Dialog dialog = new Dialog(ShowWallpaperActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.wallpaper_option_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_both = dialog.findViewById(R.id.tv_both);
        TextView tv_home = dialog.findViewById(R.id.tv_home);
        TextView tv_lock = dialog.findViewById(R.id.tv_lock);


        tv_both.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choice = 0;
                dialog.dismiss();
                setWallpaper(wallpaper);

            }
        });

        tv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choice = 1;
                dialog.dismiss();
                setWallpaper(wallpaper);

            }
        });

        tv_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choice = 2;
                dialog.dismiss();
                setWallpaper(wallpaper);

            }
        });

        dialog.show();

    }


    private void setWallpaper(String path) {

        Bitmap bmap2 = BitmapFactory.decodeFile(path);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        Bitmap bitmap = Bitmap.createScaledBitmap(bmap2, width, height, true);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(ShowWallpaperActivity.this);
        try {
            switch (choice) {

                case 0:
                    wallpaperManager.setBitmap(bitmap);
                    makeToast();
                    break;

                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                        makeToast();
                    }
                    break;

                case 2:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                        makeToast();
                    }
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() {

        iv_preview = findViewById(R.id.iv_preview);
        iv_setWallpaper = findViewById(R.id.iv_setWallpaper);
        iv_addToFav = findViewById(R.id.iv_addToFav);
        iv_exit = findViewById(R.id.iv_exit);
        tv_fav = findViewById(R.id.tv_fav);
        iv_wallpaper = findViewById(R.id.iv_wallpaper);

    }

    private void openDialogBoxLiveOrNormal(String path, Context context) {

        final Dialog dialog = new Dialog(ShowWallpaperActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialogbox);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_live = dialog.findViewById(R.id.tv_live);
        TextView tv_normal = dialog.findViewById(R.id.tv_both);


        tv_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDialogBox();
                dialog.dismiss();

            }
        });

        tv_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setImage(path);
                setAsLiveWallpaper();
                dialog.dismiss();

            }
        });

        dialog.show();

    }

    private void setAsLiveWallpaper() {

        try {
            startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(ShowWallpaperActivity.this, LiveWallpaperService.class)));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(ShowWallpaperActivity.this, R.string.toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
            }
        }

    }

    private void makeToast() {

        Toast.makeText(this, "Wallpaper set Successfully", Toast.LENGTH_SHORT).show();

    }

    private void setImage(String path) {

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("image", path);
        myEdit.apply();
    }


}