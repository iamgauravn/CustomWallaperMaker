package com.gaurav.wallpapermaker.LiveWallpaper;

import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;


import java.io.Writer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;


@Deprecated
interface EGLContextFactory extends GLSurfaceView.EGLContextFactory {
}


@Deprecated
interface EGLWindowSurfaceFactory extends GLSurfaceView.EGLWindowSurfaceFactory {
}


@Deprecated
interface GLWrapper extends GLSurfaceView.GLWrapper {
}

@Deprecated
interface EGLConfigChooser extends GLSurfaceView.EGLConfigChooser {
}


public class GLWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    @Deprecated
    public interface Renderer extends GLSurfaceView.Renderer {
    }

    public class GLEngine extends Engine {
        public final static int RENDERMODE_WHEN_DIRTY = 0;
        public final static int RENDERMODE_CONTINUOUSLY = 1;

        private GLThread mGLThread;
        private GLSurfaceView.EGLConfigChooser mEGLConfigChooser;
        private GLSurfaceView.EGLContextFactory mEGLContextFactory;
        private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
        private GLSurfaceView.GLWrapper mGLWrapper;
        private int mDebugFlags;
        private int mEGLContextClientVersion;

        public GLEngine() {
            super();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                onResume();
            } else {
                onPause();
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGLThread.requestExitAndWait();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mGLThread.onWindowResize(width, height);
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {

            mGLThread.surfaceCreated(holder);
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {

            mGLThread.surfaceDestroyed();
            super.onSurfaceDestroyed(holder);
        }

        public void setGLWrapper(GLSurfaceView.GLWrapper glWrapper) {
            mGLWrapper = glWrapper;
        }

        public int getDebugFlags() {
            return mDebugFlags;
        }

        public void setDebugFlags(int debugFlags) {
            mDebugFlags = debugFlags;
        }

        public void setRenderer(GLSurfaceView.Renderer renderer) {
            checkRenderThreadState();
            if (mEGLConfigChooser == null) {
                mEGLConfigChooser = new BaseConfigChooser.SimpleEGLConfigChooser(true, mEGLContextClientVersion);
            }
            if (mEGLContextFactory == null) {
                mEGLContextFactory = new DefaultContextFactory(mEGLContextClientVersion);
            }
            if (mEGLWindowSurfaceFactory == null) {
                mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
            }
            mGLThread = new GLThread(renderer, mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, mGLWrapper);
            mGLThread.start();
        }

        public void setEGLContextFactory(GLSurfaceView.EGLContextFactory factory) {
            checkRenderThreadState();
            mEGLContextFactory = factory;
        }

        public void setEGLWindowSurfaceFactory(GLSurfaceView.EGLWindowSurfaceFactory factory) {
            checkRenderThreadState();
            mEGLWindowSurfaceFactory = factory;
        }

        public void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser configChooser) {
            checkRenderThreadState();
            mEGLConfigChooser = configChooser;
        }

        public void setEGLConfigChooser(boolean needDepth) {
            setEGLConfigChooser(new BaseConfigChooser.SimpleEGLConfigChooser(needDepth, mEGLContextClientVersion));
        }

        public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            setEGLConfigChooser(new BaseConfigChooser.ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize, mEGLContextClientVersion));
        }

        public void setEGLContextClientVersion(int version) {
            checkRenderThreadState();
            mEGLContextClientVersion = version;
        }

        public int getRenderMode() {
            return mGLThread.getRenderMode();
        }

        public void setRenderMode(int renderMode) {
            mGLThread.setRenderMode(renderMode);
        }

        public void requestRender() {
            mGLThread.requestRender();
        }

        public void onPause() {
            mGLThread.onPause();
        }

        public void onResume() {
            mGLThread.onResume();
        }

        public void queueEvent(Runnable r) {
            mGLThread.queueEvent(r);
        }

        private void checkRenderThreadState() {
            if (mGLThread != null) {
                throw new IllegalStateException("setRenderer has already been called for this instance.");
            }
        }
    }
}

class LogWriter extends Writer {
    private StringBuilder mBuilder = new StringBuilder();

    @Override
    public void close() {
        flushBuilder();
    }

    @Override
    public void flush() {
        flushBuilder();
    }

    @Override
    public void write(char[] buf, int offset, int count) {
        for (int i = 0; i < count; i++) {
            char c = buf[offset + i];
            if (c == '\n') {
                flushBuilder();
            } else {
                mBuilder.append(c);
            }
        }
    }

    private void flushBuilder() {
        if (mBuilder.length() > 0) {

            mBuilder.delete(0, mBuilder.length());
        }
    }
}

class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private int eglContextClientVersion;

    DefaultContextFactory(int eglContextClientVersion) {
        this.eglContextClientVersion = eglContextClientVersion;
    }

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, eglContextClientVersion, EGL10.EGL_NONE};
        return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, eglContextClientVersion != 0 ? attrib_list : null);
    }

    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display, context);
    }
}

class DefaultWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {

    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {

        EGLSurface eglSurface = null;
        while (eglSurface == null) {
            try {
                eglSurface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (Throwable t) {
            } finally {
                if (eglSurface == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException t) {
                    }
                }
            }
        }
        return eglSurface;
    }

    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }
}

class EglHelper {

    EGLConfig mEglConfig;
    private EGL10 mEgl;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private GLSurfaceView.EGLConfigChooser mEGLConfigChooser;
    private GLSurfaceView.EGLContextFactory mEGLContextFactory;
    private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLSurfaceView.GLWrapper mGLWrapper;

    public EglHelper(GLSurfaceView.EGLConfigChooser chooser, GLSurfaceView.EGLContextFactory contextFactory, GLSurfaceView.EGLWindowSurfaceFactory surfaceFactory, GLSurfaceView.GLWrapper wrapper) {
        this.mEGLConfigChooser = chooser;
        this.mEGLContextFactory = contextFactory;
        this.mEGLWindowSurfaceFactory = surfaceFactory;
        this.mGLWrapper = wrapper;
    }


    public void start() {
        if (mEgl == null) {
            mEgl = (EGL10) EGLContext.getEGL();
        } else {
        }

        if (mEglDisplay == null) {
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        } else {
        }

        if (mEglConfig == null) {
            int[] version = new int[2];
            mEgl.eglInitialize(mEglDisplay, version);
            mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);
        } else {
        }

        if (mEglContext == null) {
            mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                throw new RuntimeException("createContext failed");
            }
        } else {

        }

        mEglSurface = null;
    }


    public GL createSurface(SurfaceHolder holder) {

        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {


            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
        }


        mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl, mEglDisplay, mEglConfig, holder);

        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            throw new RuntimeException("createWindowSurface failed");
        }


        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed.");
        }

        GL gl = mEglContext.getGL();
        if (mGLWrapper != null) {
            gl = mGLWrapper.wrap(gl);
        }


        return gl;
    }

    public boolean swap() {
        mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);


        return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
    }

    public void destroySurface() {
        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
            mEglSurface = null;
        }
    }

    public void finish() {
        if (mEglContext != null) {
            mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
            mEglContext = null;
        }
        if (mEglDisplay != null) {
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }
}

class GLThread extends Thread {
    public final static int DEBUG_CHECK_GL_ERROR = 1;
    public final static int DEBUG_LOG_GL_CALLS = 2;
    private final static boolean LOG_THREADS = false;
    private final GLThreadManager sGLThreadManager = new GLThreadManager();
    public SurfaceHolder mHolder;

    public boolean mDone;
    private GLThread mEglOwner;
    private GLSurfaceView.EGLConfigChooser mEGLConfigChooser;
    private GLSurfaceView.EGLContextFactory mEGLContextFactory;
    private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLSurfaceView.GLWrapper mGLWrapper;
    private boolean mSizeChanged = true;
    private boolean mPaused;
    private boolean mHasSurface;
    private boolean mWaitingForSurface;
    private boolean mHaveEgl;
    private int mWidth;
    private int mHeight;
    private int mRenderMode;
    private boolean mRequestRender;
    private boolean mEventsWaiting;

    private GLSurfaceView.Renderer mRenderer;
    private ArrayList<Runnable> mEventQueue = new ArrayList<>();
    private EglHelper mEglHelper;

    GLThread(GLSurfaceView.Renderer renderer, GLSurfaceView.EGLConfigChooser chooser, GLSurfaceView.EGLContextFactory contextFactory, GLSurfaceView.EGLWindowSurfaceFactory surfaceFactory, GLSurfaceView.GLWrapper wrapper) {
        super();
        mDone = false;
        mWidth = 0;
        mHeight = 0;
        mRequestRender = true;
        mRenderMode = GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY;
        mRenderer = renderer;
        this.mEGLConfigChooser = chooser;
        this.mEGLContextFactory = contextFactory;
        this.mEGLWindowSurfaceFactory = surfaceFactory;
        this.mGLWrapper = wrapper;
    }

    @Override
    public void run() {
        setName("GLThread " + getId());
        if (LOG_THREADS) {

        }

        try {
            guardedRun();
        } catch (InterruptedException e) {

        } finally {
            sGLThreadManager.threadExiting(this);
        }
    }


    private void stopEglLocked() {
        if (mHaveEgl) {
            mHaveEgl = false;
            mEglHelper.destroySurface();
            sGLThreadManager.releaseEglSurface(this);
        }
    }

    private void guardedRun() throws InterruptedException {
        mEglHelper = new EglHelper(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, mGLWrapper);
        try {
            GL10 gl = null;
            boolean tellRendererSurfaceCreated = true;
            boolean tellRendererSurfaceChanged = true;


            while (!isDone()) {

                int w = 0;
                int h = 0;
                boolean changed = false;
                boolean needStart = false;
                boolean eventsWaiting = false;

                synchronized (sGLThreadManager) {
                    while (true) {

                        if (mPaused) {
                            stopEglLocked();
                        }
                        if (!mHasSurface) {
                            if (!mWaitingForSurface) {
                                stopEglLocked();
                                mWaitingForSurface = true;
                                sGLThreadManager.notifyAll();
                            }
                        } else {
                            if (!mHaveEgl) {
                                if (sGLThreadManager.tryAcquireEglSurface(this)) {
                                    mHaveEgl = true;
                                    mEglHelper.start();
                                    mRequestRender = true;
                                    needStart = true;
                                }
                            }
                        }


                        if (mDone) {
                            return;
                        }

                        if (mEventsWaiting) {
                            eventsWaiting = true;
                            mEventsWaiting = false;
                            break;
                        }

                        if ((!mPaused) && mHasSurface && mHaveEgl && (mWidth > 0) && (mHeight > 0) && (mRequestRender || (mRenderMode == GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY))) {
                            changed = mSizeChanged;
                            w = mWidth;
                            h = mHeight;
                            mSizeChanged = false;
                            mRequestRender = false;
                            if (mHasSurface && mWaitingForSurface) {
                                changed = true;
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }
                            break;
                        }


                        if (LOG_THREADS) {

                        }
                        sGLThreadManager.wait();
                    }
                }
                if (eventsWaiting) {
                    Runnable r;
                    while ((r = getEvent()) != null) {
                        r.run();
                        if (isDone()) {
                            return;
                        }
                    }

                    continue;
                }

                if (needStart) {
                    tellRendererSurfaceCreated = true;
                    changed = true;
                }
                if (changed) {
                    gl = (GL10) mEglHelper.createSurface(mHolder);
                    tellRendererSurfaceChanged = true;
                }
                if (tellRendererSurfaceCreated) {
                    mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                    tellRendererSurfaceCreated = false;
                }
                if (tellRendererSurfaceChanged) {
                    mRenderer.onSurfaceChanged(gl, w, h);
                    tellRendererSurfaceChanged = false;
                }
                if ((w > 0) && (h > 0)) {

                    mRenderer.onDrawFrame(gl);


                    mEglHelper.swap();
                    Thread.sleep(10);
                }
            }
        } finally {

            synchronized (sGLThreadManager) {
                stopEglLocked();
                mEglHelper.finish();
            }
        }
    }

    private boolean isDone() {
        synchronized (sGLThreadManager) {
            return mDone;
        }
    }

    public int getRenderMode() {
        synchronized (sGLThreadManager) {
            return mRenderMode;
        }
    }

    public void setRenderMode(int renderMode) {
        if (!((GLWallpaperService.GLEngine.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY))) {
            throw new IllegalArgumentException("renderMode");
        }
        synchronized (sGLThreadManager) {
            mRenderMode = renderMode;
            if (renderMode == GLWallpaperService.GLEngine.RENDERMODE_CONTINUOUSLY) {
                sGLThreadManager.notifyAll();
            }
        }
    }

    public void requestRender() {
        synchronized (sGLThreadManager) {
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {

            }
            mHasSurface = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void surfaceDestroyed() {
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {

            }
            mHasSurface = false;
            sGLThreadManager.notifyAll();
            while (!mWaitingForSurface && isAlive() && !mDone) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void onPause() {
        synchronized (sGLThreadManager) {
            mPaused = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void onResume() {
        synchronized (sGLThreadManager) {
            mPaused = false;
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void onWindowResize(int w, int h) {
        synchronized (sGLThreadManager) {
            mWidth = w;
            mHeight = h;
            mSizeChanged = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void requestExitAndWait() {

        synchronized (sGLThreadManager) {
            mDone = true;
            sGLThreadManager.notifyAll();
        }
        try {
            join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    public void queueEvent(Runnable r) {
        synchronized (this) {
            mEventQueue.add(r);
            synchronized (sGLThreadManager) {
                mEventsWaiting = true;
                sGLThreadManager.notifyAll();
            }
        }
    }

    private Runnable getEvent() {
        synchronized (this) {
            if (mEventQueue.size() > 0) {
                return mEventQueue.remove(0);
            }

        }
        return null;
    }

    private class GLThreadManager {

        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {

            }
            thread.mDone = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }


        public synchronized boolean tryAcquireEglSurface(GLThread thread) {
            if (mEglOwner == thread || mEglOwner == null) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }
            return false;
        }

        public synchronized void releaseEglSurface(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }
    }
}

