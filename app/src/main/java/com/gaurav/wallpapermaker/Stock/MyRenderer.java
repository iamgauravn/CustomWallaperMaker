package com.gaurav.wallpapermaker.Stock;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    public void onDrawFrame(GL10 gl) {


        gl.glClearColor(0.2f, 0.4f, 0.2f, 1f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }


    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }


    public void release() {
    }
}