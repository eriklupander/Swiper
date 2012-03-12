package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BgQuad extends MutableShape {
	
	
	private static final float quadY = 11f;
	private static final float quadX = 6.6f;
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-quadX, quadY, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	quadX, quadY, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	-quadX, -quadY, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    	quadX, -quadY, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
	
    public int[] texture = new int[1];
    
    public static FloatBuffer verticesBuffer;
    
    static {
    	verticesBuffer = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                 * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	verticesBuffer.put(mTriangleVerticesData).position(0); 
    }

    public BgQuad(int[]texture) {
        this.texture = texture;
    }
   
    public void draw() {
    	
//    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
//    	//GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ContactCardsRenderer.mTargetTexture);
//        GLES20.glDisable(GL10.GL_BLEND);
//        
//        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
//        GLES20.glVertexAttribPointer(Shaders.defaultShader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
//
//        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
//        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mPositionHandle);
//
//        GLES20.glVertexAttribPointer(Shaders.defaultShader.mTextureHandle, 2, GLES20.GL_FLOAT, false,
//                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
//
//        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mTextureHandle);
//
//        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);
//        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
//
//        GLES20.glUniformMatrix4fv(Shaders.defaultShader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
//        
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//
//        GLES20.glEnable(GL10.GL_BLEND);

    }
}