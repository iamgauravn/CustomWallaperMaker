package com.gaurav.wallpapermaker.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gaurav.wallpapermaker.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class FavouriteWallpaperActivity extends AppCompatActivity {

    ImageView iv_exit;
    TextView iv_empty;
    RecyclerView recyclerview;
    FavouriteAdapter favouriteAdapter;
    ArrayList<String> stringArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_wallpaper);

        init();
        setAdapter();

        iv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        updateAdapter();

    }

    private void set() {

        if (stringArrayList.size() > 0) {

            iv_empty.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);

        } else {

            iv_empty.setVisibility(View.VISIBLE);
            recyclerview.setVisibility(View.GONE);

        }

    }

    private void updateAdapter() {

        stringArrayList = new ArrayList<>();
        loadData();
        favouriteAdapter.update(stringArrayList);
        set();

    }

    private void setAdapter() {

        stringArrayList = new ArrayList<>();
        loadData();
        favouriteAdapter = new FavouriteAdapter(stringArrayList, FavouriteWallpaperActivity.this);
        recyclerview.setAdapter(favouriteAdapter);
        set();

    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("fav_list", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        stringArrayList = gson.fromJson(json, type);
        if (stringArrayList == null) {
            stringArrayList = new ArrayList<>();
        }
    }

    private void init() {

        iv_exit = findViewById(R.id.iv_exit);
        recyclerview = findViewById(R.id.recyclerview);
        iv_empty = findViewById(R.id.iv_empty);
        recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

    }

    private class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

        ArrayList<String> list;
        Context context;

        public FavouriteAdapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        public void update(ArrayList<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FavouriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FavouriteAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

            Glide.with(context).load(list.get(position)).into(holder.iv_image);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent ie = new Intent(context, ShowWallpaperActivity.class);
                    ie.putExtra("path", list.get(position));
                    startActivity(ie);

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

}