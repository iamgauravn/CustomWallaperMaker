package com.gaurav.wallpapermaker.LiveWallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.greenrobot.eventbus.EventBus;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class LiveWallpaperRenderer implements GLSurfaceView.Renderer {
    private final static int REFRESH_RATE = 60;
    private final static float MAX_BIAS_RANGE = 0.003f;
    private final static String TAG = "LiveWallpaperRenderer";

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final Context mContext;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final float transitionStep = REFRESH_RATE / LiveWallpaperService.SENSOR_RATE;
    private Wallpaper wallpaper;
    private float scrollStep = 1f;
    private Queue<Float> scrollOffsetXQueue = new CircularFifoQueue<>(10);
    private float scrollOffsetX = 0.5f;// , offsetY = 0.5f;
    private float scrollOffsetXBackup = 0.5f;
    private float currentOrientationOffsetX, currentOrientationOffsetY;
    private float orientationOffsetX, orientationOffsetY;
    private Callbacks mCallbacks;
    private float screenAspectRatio;
    private int screenH;
    private float wallpaperAspectRatio;
    private float biasRange;
    private float scrollRange;
    private boolean scrollMode = true;
    private int delay = 3;
    private final Runnable transition = new Runnable() {
        @Override
        public void run() {
            transitionCal();
        }
    };
    private ScheduledFuture<?> transitionHandle;
    private boolean needsRefreshWallpaper;
    private boolean isDefaultWallpaper;
    private float preA;
    private float preB;

    String image;

    LiveWallpaperRenderer(Context context, Callbacks callbacks) {

        mContext = context;
        mCallbacks = callbacks;
    }

    void release() {

        if (wallpaper != null) wallpaper.destroy();
        stopTransition();
        scheduler.shutdown();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Wallpaper.initGl();
    }

    void startTransition() {
        stopTransition();
        transitionHandle = scheduler.scheduleAtFixedRate(transition, 0, 1000 / REFRESH_RATE, TimeUnit.MILLISECONDS);
    }

    void stopTransition() {
        if (transitionHandle != null) transitionHandle.cancel(true);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        if (needsRefreshWallpaper) {
            loadTexture();
            needsRefreshWallpaper = false;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


        float x = preA * (-2 * scrollOffsetX + 1) + currentOrientationOffsetX;
        float y = currentOrientationOffsetY;
        Matrix.setLookAtM(mViewMatrix, 0, x, y, preB, x, y, 0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        wallpaper.draw(mMVPMatrix);

    }

    private void preCalculate() {
        if (scrollStep > 0) {
            if (wallpaperAspectRatio > (1 + 1 / (3 * scrollStep)) * screenAspectRatio) {
                scrollRange = 1 + 1 / (3 * scrollStep);
            } else if (wallpaperAspectRatio >= screenAspectRatio) {
                scrollRange = wallpaperAspectRatio / screenAspectRatio;
            } else {
                scrollRange = 1;
            }
        } else {
            scrollRange = 1;
        }

        preA = screenAspectRatio * (scrollRange - 1);

        if (screenAspectRatio < 1) preB = -1.0f + (biasRange / screenAspectRatio);
        else preB = -1.0f + (biasRange * screenAspectRatio);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) {
            height = 1;
        }

        screenAspectRatio = (float) width / (float) height;
        screenH = height;

        GLES20.glViewport(0, 0, width, height);

        Matrix.frustumM(mProjectionMatrix, 0, -0.1f * screenAspectRatio, 0.1f * screenAspectRatio, -0.1f, 0.1f, 0.1f, 2);

        needsRefreshWallpaper = true;

        mCallbacks.requestRender();
    }

    void setOffset(float offsetX, float offsetY) {
        if (scrollMode) {
            scrollOffsetXBackup = offsetX;
            scrollOffsetXQueue.offer(offsetX);
        } else {
            scrollOffsetXBackup = offsetX;
        }
    }

    void setOffsetStep(float offsetStepX, float offsetStepY) {
        if (scrollStep != offsetStepX) {
            scrollStep = offsetStepX;
            preCalculate();
        }
    }

    void setScrollMode(boolean scrollMode) {
        this.scrollMode = scrollMode;
        if (scrollMode) scrollOffsetXQueue.offer(scrollOffsetXBackup);
        else {
            scrollOffsetXQueue.clear();
            scrollOffsetXQueue.offer(0.5f);
        }

    }

    void setOrientationAngle(float roll, float pitch) {
        orientationOffsetX = (float) (biasRange * Math.sin(roll));
        orientationOffsetY = (float) (biasRange * Math.sin(pitch));
    }

    void setIsDefaultWallpaper(boolean isDefault) {
        isDefaultWallpaper = isDefault;
        needsRefreshWallpaper = true;

        mCallbacks.requestRender();
    }

    void setBiasRange(int multiples) {

        biasRange = multiples * MAX_BIAS_RANGE + 0.03f;
        preCalculate();
        mCallbacks.requestRender();
    }

    void setDelay(int delay) {
        this.delay = delay;
    }

    private void transitionCal() {
        boolean needRefresh = false;
        if (Math.abs(currentOrientationOffsetX - orientationOffsetX) > .0001 || Math.abs(currentOrientationOffsetY - orientationOffsetY) > .0001) {
            float tinyOffsetX = (orientationOffsetX - currentOrientationOffsetX) / (transitionStep * delay);
            float tinyOffsetY = (orientationOffsetY - currentOrientationOffsetY) / (transitionStep * delay);
            currentOrientationOffsetX += tinyOffsetX;
            currentOrientationOffsetY += tinyOffsetY;
            EventBus.getDefault().post(new BiasChangeEvent(currentOrientationOffsetX / biasRange, currentOrientationOffsetY / biasRange));
            needRefresh = true;
        }
        if (!scrollOffsetXQueue.isEmpty()) {
            scrollOffsetX = scrollOffsetXQueue.poll();
            needRefresh = true;
        }
        if (needRefresh) mCallbacks.requestRender();
    }

    private void loadTexture() {

        getImage();

        Bitmap bmap2 = BitmapFactory.decodeFile(image);

        if (wallpaper != null) wallpaper.destroy();
        wallpaper = new Wallpaper(bmap2);
        preCalculate();

        System.gc();
    }

    private void getImage() {

        SharedPreferences sh = mContext.getSharedPreferences("MySharedPref", Context.MODE_MULTI_PROCESS);
        image = sh.getString("image", "");
    }


    interface Callbacks {
        void requestRender();
    }

    public static class BiasChangeEvent {
        float x, y;

        public BiasChangeEvent(float x, float y) {
            if (x > 1) this.x = 1;
            else if (x < -1) this.x = -1;
            else this.x = x;
            if (y > 1) this.y = 1;
            else if (y < -1) this.y = -1;
            else this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}
