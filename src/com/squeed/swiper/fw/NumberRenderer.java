package com.squeed.swiper.fw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.helper.MatrixStack;
import com.squeed.swiper.shader.Shader;
import com.squeed.swiper.shader.Shaders;

/**
 * Test class for rendering numbers. One number per "quad"
 * 
 * @author Erik
 *
 */
public class NumberRenderer {
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	
	float xOffset = 0.16f;
	float yOffset = 0.25f;
	private float uMin;
	private float uMax;
	private float vMin;
	private float vMax;

	private static FloatBuffer mTriangleVertices;
	private static float[] mTriangleVerticesData; // = new float[20];
	static {
		float vx = 0.2f;
		float vy = 0.2f;
		mTriangleVerticesData = new float[]{
		    	// X, Y, Z, U, V
		    	-vx, vy, 0.0f, 0.0f, 0.0f,
		    	vx, vy, 0.0f, 0.0f, 0.0f,
		    	-vx, -vy, 0.0f, 0.0f, 0.0f,
		    	vx, -vy, 0.0f, 0.0f, 0.0f};
		mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangleVertices.put(mTriangleVerticesData).position(0);
	}
	
	public void renderNumber(float x, float y, float z, float xRot, float yRot, float zRot, String number, int textureId) {
		for(int a = 0; a < number.length(); a++) {			
			//GLES20.glUniform1f(Shaders.defaultShader.mTextureHandle, ContactCardsRenderer.numbersTexture[0]);
			setUVCoordinates((int) number.charAt(a));
			render(x + 0.6f*a, y, z, xRot, yRot, zRot, textureId, Shaders.defaultShader);
		}
	}
	
	private void setUVCoordinates(int i) {		
		int row = i / 6;
		int col = i % 6;
		
		uMin = col*xOffset;
		uMax = (col+1)*xOffset;
		vMin = row*yOffset;
		vMax = (row+1)*yOffset;
	
		mTriangleVerticesData[3] = uMin;
		mTriangleVerticesData[4] = vMin;
		mTriangleVerticesData[8] = uMax;
		mTriangleVerticesData[9] = vMin;
		mTriangleVerticesData[13] = uMin;
		mTriangleVerticesData[14] = vMax;
		mTriangleVerticesData[18] = uMax;
		mTriangleVerticesData[19] = vMax;
						
//		{
//		    	// X, Y, Z, U, V
//		    	-vx, vy, 0.0f, uMin, vMin,
//		    	vx, vy, 0.0f, uMax, vMin,
//		    	-vx, -vy, 0.0f, uMin, vMax,
//		    	vx, -vy, 0.0f, uMax, vMax};
		
		/********* START GL ES2.0 code **************/
		mTriangleVertices.position(0);
        mTriangleVertices.put(mTriangleVerticesData);
	}
	
	private void render(float x, float y, float z, float xRot, float yRot, float zRot, int textureId, Shader shader) {
		
		// 1. Make TEXTURE0 active and bind it.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        // 2. Enable blending
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        
        // 3. Feed the verticies to the vertex shader
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(shader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        // 4. Feed texture coordinates to fragment shader
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(shader.mPositionHandle);
        GLES20.glVertexAttribPointer(shader.mTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(shader.mTextureHandle);
        
        // 5. Save current matrices on our home-crafted matrix stack.
        MatrixStack.push(ContactCardsRenderer.mViewMatrix);
        MatrixStack.push2(ContactCardsRenderer.mModelMatrix);
        
        // 6. First translate to WHERE we want to draw...
        Matrix.translateM(ContactCardsRenderer.mViewMatrix, 0, x, y, z);    
        
        // 7. ... and THEN rotate (and scale, if applicable)
        Matrix.setRotateM(ContactCardsRenderer.mModelMatrix, 0, yRot, 0, 1.0f, 0);
        
        // 8. Multiply the VMMatrix with the ModelMatrix, store the result in the ModelViewProject Matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);
        
        // 9. Then multiply the projection matrix by the MVP matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);

        // 10. Feed the newly calculated MVP matrix to the shader
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);

        // 11. And DRAW! (TODO switch to VertexBufferObject). Only 10 steps before we could actually draw something. OpenGL ES 2.0 FTW :-)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // 12. Finally, reverse translations... urgh, this should probably be performed using some fancy inverse transform...
        MatrixStack.pop2(ContactCardsRenderer.mModelMatrix);
        MatrixStack.pop(ContactCardsRenderer.mViewMatrix);
	}
}
