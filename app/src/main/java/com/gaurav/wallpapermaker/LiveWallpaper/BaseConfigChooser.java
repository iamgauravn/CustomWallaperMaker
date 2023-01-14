
package com.gaurav.wallpapermaker.LiveWallpaper;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;


abstract class BaseConfigChooser implements GLSurfaceView.EGLConfigChooser {
    protected int[] mConfigSpec;
    private int eglContextClientVersion;

    public BaseConfigChooser(int[] configSpec, int eglContextClientVersion) {
        this.eglContextClientVersion = eglContextClientVersion;
        mConfigSpec = filterConfigSpec(configSpec);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] num_config = new int[1];
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException(
                    "No configs match configSpec");
        }

        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        EGLConfig config = chooseConfig(egl, display, configs);
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                    EGLConfig[] configs);

    private int[] filterConfigSpec(int[] configSpec) {
        if (eglContextClientVersion != 2) {
            return configSpec;
        }

        int len = configSpec.length;
        int[] newConfigSpec = new int[len + 2];
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
        newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
        newConfigSpec[len] = 4;
        newConfigSpec[len + 1] = EGL10.EGL_NONE;
        return newConfigSpec;
    }

    public static class ComponentSizeChooser extends BaseConfigChooser {

        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue;
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize,
                                    int stencilSize, int eglContextClientVersion) {
            super(new int[]{EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
                    blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE,
                    stencilSize, EGL10.EGL_NONE}, eglContextClientVersion);
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            EGLConfig closestConfig = null;
            int closestDistance = 1000;
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE);
                if (d >= mDepthSize && s >= mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE);
                    int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE);
                    int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE);
                    int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE);
                    int distance = Math.abs(r - mRedSize) + Math.abs(g - mGreenSize) + Math.abs(b - mBlueSize)
                            + Math.abs(a - mAlphaSize);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestConfig = config;
                    }
                }
            }
            return closestConfig;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return 0;
        }
    }


    public static class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer, int eglContextClientVersion) {
            super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0, eglContextClientVersion);

            mRedSize = 5;
            mGreenSize = 6;
            mBlueSize = 5;
        }
    }
}
