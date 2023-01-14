package com.gaurav.wallpapermaker.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gaurav.wallpapermaker.Stock.AdvanceGLWallpaperService;
import com.gaurav.wallpapermaker.Stock.NormalWallpaperService;
import com.gaurav.wallpapermaker.R;
import com.gaurav.wallpapermaker.VideoWallpaper.VideoWallpaperService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    RelativeLayout iv_selectImage, iv_selectVideo, iv_favlist, iv_dots, iv_cube;
    ImageView iv_exit;
    BottomSheetAdapter bottomSheetAdapter;
    ArrayList<String> imageList = new ArrayList<>();
    ArrayList<String> videoList = new ArrayList<>();
    ProgressDialog p;
    int choice = 0;
    BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        init();
        getImagesFromStorage();
        getVideosFromStorage();

        iv_selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choice = 0;
                selectImage();
            }
        });

        iv_selectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = 1;
                selectVideo();
            }
        });

        iv_favlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, FavouriteWallpaperActivity.class));
            }
        });

        iv_dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(MenuActivity.this, NormalWallpaperService.class));
                startActivity(intent);

            }
        });

        iv_cube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choice = 2;
                selectImage();

            }
        });

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void getVideosFromStorage() {

        FetchVideos videos = new FetchVideos();
        videos.execute();

    }

    private void getImagesFromStorage() {

        FetchImage fetchImage = new FetchImage();
        fetchImage.execute();

    }

    private void selectImage() {

        bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);

        ImageView iv_exit = bottomSheetDialog.findViewById(R.id.iv_exit);

        TextView tv_title = bottomSheetDialog.findViewById(R.id.tv_title);
        tv_title.setText("Select Image");

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        RecyclerView recyclerview = bottomSheetDialog.findViewById(R.id.recyclerview);
        assert recyclerview != null;
        recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

        bottomSheetAdapter = new BottomSheetAdapter(imageList, MenuActivity.this);
        recyclerview.setAdapter(bottomSheetAdapter);

        bottomSheetDialog.show();

    }

    private void selectVideo() {

        bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);

        ImageView iv_exit = bottomSheetDialog.findViewById(R.id.iv_exit);

        TextView tv_title = bottomSheetDialog.findViewById(R.id.tv_title);
        tv_title.setText("Select Video");

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        RecyclerView recyclerview = bottomSheetDialog.findViewById(R.id.recyclerview);
        assert recyclerview != null;
        recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

        bottomSheetAdapter = new BottomSheetAdapter(videoList, MenuActivity.this);
        recyclerview.setAdapter(bottomSheetAdapter);

        bottomSheetDialog.show();

    }

    private void setVideoWallpaper(String path) {

        saveVideoPath(path);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            wallpaperManager.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(MenuActivity.this, VideoWallpaperService.class)));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(MenuActivity.this, R.string.toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
            }
        }

    }

    private void saveVideoPath(String path) {

        SharedPreferences sharedPreferences = getSharedPreferences("VideoPath", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("videoPath", path);
        myEdit.apply();

    }

    private void saveCubeImage(String path) {

        SharedPreferences sharedPreferences = getSharedPreferences("saveCubeImage", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("cube", path);
        myEdit.apply();

    }

    private void init() {

        iv_selectImage = findViewById(R.id.iv_selectImage);
        iv_selectVideo = findViewById(R.id.iv_selectVideo);
        iv_dots = findViewById(R.id.iv_dots);
        iv_exit = findViewById(R.id.iv_exit);
        iv_favlist = findViewById(R.id.iv_favlist);
        iv_cube = findViewById(R.id.iv_cube);

    }

    class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {

        ArrayList<String> list;
        Context context;

        public BottomSheetAdapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public BottomSheetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BottomSheetAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Glide.with(context).load(list.get(position)).into(holder.iv_image);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (choice == 0) {

                        Intent ie = new Intent(context, ShowWallpaperActivity.class);
                        ie.putExtra("path", list.get(position));
                        startActivity(ie);

                    } else if (choice == 1) {

                        setVideoWallpaper(list.get(position));

                    } else if (choice == 2) {

                        saveCubeImage(list.get(position));
                        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(MenuActivity.this, AdvanceGLWallpaperService.class));
                        startActivity(intent);

                    }

                    if (bottomSheetDialog != null) {

                        bottomSheetDialog.dismiss();

                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView iv_image;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                iv_image = itemView.findViewById(R.id.iv_image);

            }
        }
    }

    private class FetchImage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            p = new ProgressDialog(MenuActivity.this);
            p.setMessage("Getting Data");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            getVideosFromStorage();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor c = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);

            if (c != null) {
                while (c.moveToNext()) {

                    @SuppressLint("Range") String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (new File((path)).exists()) {
                        imageList.add(path);
                    }
                }
                c.close();
            }

            return null;
        }
    }

    private class FetchVideos extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            p.dismiss();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.Video.Media.DATA};

            Cursor c = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);

            if (c != null) {
                while (c.moveToNext()) {

                    @SuppressLint("Range") String path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                    if (new File((path)).exists()) {
                        videoList.add(path);
                    }
                }
                c.close();
            }

            return null;
        }
    }

}