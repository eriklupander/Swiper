package com.squeed.swiper.fw;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

/**
 * Helper class for creating a FrameBuffer and a texture for the framebuffer.
 * 
 * More or less copied from the ApiDemos and then adapted to GLES20.
 * 
 * In the context of Swiper, we use a FBO to perform color-based picking.
 * Note that we render the "picking" to a 60x100 pixel size buffer instead of 
 * the full 480x800 buffer which gives a tremendous performance boost while still
 * offering accurate picking. 6000 pixels at 16 bytes each (RGBA) is substantially less
 * than 384 000 x 16 bytes. 96kb vs ~6 megabytes of data..
 * 
 * @author Erik
 *
 */
public class FrameBufferFactory {

    public static int createFrameBuffer(GL10 gl, int width, int height, int targetTextureId) {
       
        int framebuffer;
        int[] framebuffers = new int[1];
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        framebuffer = framebuffers[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);

        int depthbuffer;
        int[] renderbuffers = new int[1];
        GLES20.glGenRenderbuffers(1, renderbuffers, 0);
        depthbuffer = renderbuffers[0];

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthbuffer);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, depthbuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GL10.GL_TEXTURE_2D,
                targetTextureId, 0);
       
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete: " +
                    Integer.toHexString(status));
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return framebuffer;
    }
	
    
    public static int createTargetTexture(GL10 gl, int width, int height) {
        int texture;
        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        texture = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0,
                GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT);
;            return texture;
    }
}
