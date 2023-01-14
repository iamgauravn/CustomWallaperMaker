package com.gaurav.wallpapermaker.VideoWallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class VideoWallpaperService extends WallpaperService {

    protected static int playheadTime = 0;
    Uri path;

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends Engine {

        private final MediaPlayer mediaPlayer;

        public VideoEngine() {
            super();

            getVideoPath();
            mediaPlayer = MediaPlayer.create(getBaseContext(), path);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0, 0);
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {

            if (mediaPlayer != null) {
                mediaPlayer.setSurface(holder.getSurface());
                mediaPlayer.start();
                mediaPlayer.setVolume(0, 0);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {

            if (mediaPlayer != null) {
                playheadTime = mediaPlayer.getCurrentPosition();
                mediaPlayer.reset();
                mediaPlayer.release();
            }

        }
    }

    private void getVideoPath() {

        SharedPreferences sh = getSharedPreferences("VideoPath", Context.MODE_MULTI_PROCESS);
        path = Uri.parse(sh.getString("videoPath", ""));

    }

}