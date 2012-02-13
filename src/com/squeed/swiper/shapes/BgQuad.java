package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.shader.Shaders;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class BgQuad extends MutableShape {
	
	private static FloatBuffer mTriangleVertices;
	private static final float y = 11f;
	private static final float x = 6.6f;
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-x, y, 0.0f, 0.0f, 0.0f,
    	x, y, 0.0f, 1.0f, 0.0f,
    	-x, -y, 0.0f, 0.0f, 1.0f,
    	x, -y, 0.0f, 1.0f, 1.0f};
	
    private int[] texture = new int[1];

    public BgQuad(int[]texture) {
        this.texture = texture;
        mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0); 
    }
   
    public void draw() {
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glDisable(GL10.GL_BLEND);
        
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.maPositionHandle);

        GLES20.glVertexAttribPointer(Shaders.maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        GLES20.glEnableVertexAttribArray(Shaders.maTextureHandle);

        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.muMVPMatrixHandle, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glEnable(GL10.GL_BLEND);

    }
}