package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.squeed.swiper.actions.Command;

public class IconQuad extends MutableShape {
	
	public Command actionWhenClicked;
	
	private static float vx = 0.3f;
	
	
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-vx, vx, 0.0f, 0.0f, 0.0f,
    	vx, vx, 0.0f, 1.0f, 0.0f,
    	-vx, -vx, 0.0f, 0.0f, 1.0f,
    	vx, -vx, 0.0f, 1.0f, 1.0f};

	

	public IconQuad(Command actionWhenClicked, int colorIndex) {		
		this.actionWhenClicked = actionWhenClicked;
		this.colorIndex[0] = ((colorIndex>>16)&0x0ff) / 255.0f;
		this.colorIndex[1] = ((colorIndex>>8) &0x0ff) / 255.0f;
		this.colorIndex[2] = ((colorIndex)    &0x0ff) / 255.0f;

		
		
		/********* START GL ES2.0 code **************/
		verticesBuffer = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesBuffer.put(mTriangleVerticesData).position(0);
	}
	
	
	
//	public void drawOLD() {
//		
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[this.textureId]);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glEnable(GLES20.GL_BLEND);
//        
//        verticesBuffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
//        GLES20.glVertexAttribPointer(Shaders.defaultShader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticesBuffer);
//
//        verticesBuffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
//        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mPositionHandle);
//
//        GLES20.glVertexAttribPointer(Shaders.defaultShader.mTextureHandle, 2, GLES20.GL_FLOAT, false,
//                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticesBuffer);
//
//        GLES20.glEnableVertexAttribArray(Shaders.defaultShader.mTextureHandle);
//        
//        
//        Matrix.translateM(ContactCardsRenderer.mViewMatrix, 0, x, -y, 5.5f);        
//         
//        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);
//        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
//
//        GLES20.glUniformMatrix4fv(Shaders.defaultShader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
//        
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        
//        Matrix.translateM(ContactCardsRenderer.mViewMatrix, 0, -x, y, -5.5f); 
//
//        
//	}

}
