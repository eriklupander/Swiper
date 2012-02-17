package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.actions.Command;
import com.squeed.swiper.shader.Shaders;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class IconQuad extends MutableShape {
	
	public Command actionWhenClicked;
	
	public static int highLightTextureIndex;
	
	public boolean isHighlighted = false;

	private static float vx = 0.3f;
	
	private static FloatBuffer mTriangleVertices;
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-vx, vx, 0.0f, 0.0f, 0.0f,
    	vx, vx, 0.0f, 1.0f, 0.0f,
    	-vx, -vx, 0.0f, 0.0f, 1.0f,
    	vx, -vx, 0.0f, 1.0f, 1.0f};

	public IconQuad(Command actionWhenClicked) {		
		this.actionWhenClicked = actionWhenClicked;
		
		/********* START GL ES2.0 code **************/
		mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
	}
	
	
	
	public void draw() {
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[this.textureId]);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.defaultShader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mPositionHandle);

        GLES20.glVertexAttribPointer(Shaders.defaultShader.mTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mTextureHandle);
        
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, -y, -z*1.4f);        
         
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.defaultShader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, -x, y, z*1.4f); 

        
	}

}
