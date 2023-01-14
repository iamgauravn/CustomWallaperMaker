package com.gaurav.wallpapermaker.Stock;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AdvanceRenderer implements GLSurfaceView.Renderer {
    private static final float TOUCH_SCALE = 0.2f;

    private static final float xspeed = 0.5f;
    private static final float yspeed = 0.5f;

    private Cube cube;
    private Context context;

    private float xrot;
    private float yrot;

    private float oldX;
    private float oldY;
    private float z = -5.0f;

    public AdvanceRenderer(Context context) {
        this.cube = new Cube();
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL10.GL_LIGHT0);

        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

        gl.glDisable(GL10.GL_DITHER);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);


        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        cube.loadGLTexture(gl, context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) {
            height = 1;
        }

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();


        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();


        gl.glEnable(GL10.GL_LIGHTING);

        gl.glEnable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_DEPTH_TEST);

        gl.glTranslatef(0.0f, 0.0f, z);

        gl.glScalef(0.8f, 0.8f, 0.8f);


        gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);

        cube.draw(gl, 0);


        xrot += xspeed;
        yrot += yspeed;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            float dx = x - oldX;
            float dy = y - oldY;

            int upperArea = 0;

            if (y < upperArea) {
                z -= dx * TOUCH_SCALE / 2;

            } else {
                xrot += dy * TOUCH_SCALE;
                yrot += dx * TOUCH_SCALE;
            }
        }


        oldX = x;
        oldY = y;


        return true;
    }


    public void release() {
    }
}
