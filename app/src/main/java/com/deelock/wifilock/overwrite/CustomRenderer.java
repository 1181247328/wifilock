package com.deelock.wifilock.overwrite;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

/**
 * Created by forgive for on 2017\11\17 0017.
 */

public class CustomRenderer implements GLSurfaceView.Renderer {

    private SurfaceTexture surfaceTexture = null;
    private Surface surface = null;
    private int glSurfaceTex;
    private int TEXTURE_WIDTH = 360;
    private int TEXTURE_HEIGHT = 360;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surface = null;
        surfaceTexture = null;
        glSurfaceTex = Engine_CreateSurfaceTexture(TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this){
            surfaceTexture.updateTexImage();
        }
    }

    int Engine_CreateSurfaceTexture(int width, int height) {
        /*
         * Create our texture. This has to be done each time the surface is
         * created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        glSurfaceTex = textures[0];

        if (glSurfaceTex > 0) {
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, glSurfaceTex);

            // Notice the use of GL_TEXTURE_2D for texture creation
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);

            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        return glSurfaceTex;
    }
}
