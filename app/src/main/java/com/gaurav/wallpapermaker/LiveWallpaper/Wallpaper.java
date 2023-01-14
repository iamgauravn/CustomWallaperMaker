

package com.gaurav.wallpapermaker.LiveWallpaper;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;


import com.gaurav.wallpapermaker.ConstantClass.GLUtil;

import java.nio.FloatBuffer;

class Wallpaper {
    private static final String VERTEX_SHADER_CODE = "" +

            "uniform mat4 uMVPMatrix;" + "attribute vec4 aPosition;" + "attribute vec2 aTexCoords;" + "varying vec2 vTexCoords;" + "void main(){" + "  vTexCoords = aTexCoords;" + "  gl_Position = uMVPMatrix * aPosition;" + "}";

    private static final String FRAGMENT_SHADER_CODE = "" + "precision mediump float;" + "uniform sampler2D uTexture;"

            + "varying vec2 vTexCoords;" + "void main(){" + "  gl_FragColor = texture2D(uTexture, vTexCoords);"

            + "}";


    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE_BYTES = COORDS_PER_VERTEX * GLUtil.BYTES_PER_FLOAT;
    private static final int VERTICES = 6;


    private static final int COORDS_PER_TEXTURE_VERTEX = 2;
    private static final int TEXTURE_VERTEX_STRIDE_BYTES = COORDS_PER_TEXTURE_VERTEX * GLUtil.BYTES_PER_FLOAT;

    private static final float[] SQUARE_TEXTURE_VERTICES = {0, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 1,
            1, 0,
    };
    private static int sMaxTextureSize;
    private static int sProgramHandle;
    private static int sAttribPositionHandle;
    private static int sAttribTextureCoordsHandle;
    private static int sUniformTextureHandle;
    private static int sUniformMVPMatrixHandle;
    private final float[] mVertices = new float[COORDS_PER_VERTEX * VERTICES];
    private boolean mHasContent = false;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureCoordsBuffer;
    private int mCols = 1;
    private int mRows = 1;
    private int mWidth = 0;
    private int mHeight = 0;
    private float mRatio;
    private int mTileSize = sMaxTextureSize;
    private int[] mTextureHandles;

    Wallpaper(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        mTileSize = sMaxTextureSize;
        mHasContent = true;
        mVertexBuffer = GLUtil.newFloatBuffer(mVertices.length);
        mTextureCoordsBuffer = GLUtil.asFloatBuffer(SQUARE_TEXTURE_VERTICES);

        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mRatio = (float) mWidth / (float) mHeight;
        int leftoverHeight = mHeight % mTileSize;

        mCols = mWidth / (mTileSize + 1) + 1;
        mRows = mHeight / (mTileSize + 1) + 1;

        mTextureHandles = new int[mCols * mRows];
        if (mCols == 1 && mRows == 1) {
            mTextureHandles[0] = GLUtil.loadTexture(bitmap);
        } else {
            Rect rect = new Rect();
            for (int y = 0; y < mRows; y++) {
                for (int x = 0; x < mCols; x++) {
                    rect.set(x * mTileSize, (mRows - y - 1) * mTileSize, (x + 1) * mTileSize, (mRows - y) * mTileSize);

                    if (leftoverHeight > 0) {
                        rect.offset(0, -mTileSize + leftoverHeight);
                    }

                    Bitmap subBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
                    mTextureHandles[y * mCols + x] = GLUtil.loadTexture(subBitmap);
                    subBitmap.recycle();
                }
            }
        }
        bitmap.recycle();
    }

    static void initGl() {
        int vertexShaderHandle = GLUtil.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragShaderHandle = GLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

        sProgramHandle = GLUtil.createAndLinkProgram(vertexShaderHandle, fragShaderHandle, null);
        sAttribPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "aPosition");
        sAttribTextureCoordsHandle = GLES20.glGetAttribLocation(sProgramHandle, "aTexCoords");
        sUniformMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "uMVPMatrix");
        sUniformTextureHandle = GLES20.glGetUniformLocation(sProgramHandle, "uTexture");

        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        sMaxTextureSize = maxTextureSize[0];
    }

    void draw(float[] mvpMatrix) {
        if (!mHasContent) {
            return;
        }

        GLES20.glUseProgram(sProgramHandle);

        GLES20.glUniformMatrix4fv(sUniformMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLUtil.checkGlError("glUniformMatrix4fv");

        GLES20.glEnableVertexAttribArray(sAttribPositionHandle);
        GLES20.glVertexAttribPointer(sAttribPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE_BYTES, mVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sUniformTextureHandle, 0);
        GLES20.glVertexAttribPointer(sAttribTextureCoordsHandle, COORDS_PER_TEXTURE_VERTEX, GLES20.GL_FLOAT, false, TEXTURE_VERTEX_STRIDE_BYTES, mTextureCoordsBuffer);
        GLES20.glEnableVertexAttribArray(sAttribTextureCoordsHandle);
        for (int y = 0; y < mRows; y++) {
            for (int x = 0; x < mCols; x++) {
                mVertices[0] = mVertices[3] = mVertices[9] = -mRatio * Math.min(-1 + 2f * 1 * x * mTileSize / mWidth, 1);
                mVertices[1] = mVertices[10] = mVertices[16] = Math.min(-1 + 2f * (y + 1) * mTileSize / mHeight, 1);
                mVertices[6] = mVertices[12] = mVertices[15] = -mRatio * Math.min(-1 + 2f * 1 * (x + 1) * mTileSize / mWidth, 1);
                mVertices[4] = mVertices[7] = mVertices[13] = Math.min(-1 + 2f * y * mTileSize / mHeight, 1);
                mVertexBuffer.put(mVertices);
                mVertexBuffer.position(0);

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandles[y * mCols + x]);
                GLUtil.checkGlError("glBindTexture");

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertices.length / COORDS_PER_VERTEX);
            }
        }

        GLES20.glDisableVertexAttribArray(sAttribPositionHandle);
        GLES20.glDisableVertexAttribArray(sAttribTextureCoordsHandle);
    }

    void destroy() {
        if (mTextureHandles != null) {
            GLES20.glDeleteTextures(mTextureHandles.length, mTextureHandles, 0);
            GLUtil.checkGlError("Destroy picture");
        }
    }
}
