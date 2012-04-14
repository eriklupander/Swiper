package com.squeed.swiper.fw;

import static android.opengl.GLES10.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES10.GL_LINEAR;
import static android.opengl.GLES10.GL_NEAREST;
import static android.opengl.GLES10.GL_TEXTURE_2D;
import static android.opengl.GLES10.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES10.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES10.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES10.GL_TEXTURE_WRAP_T;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.helper.MatrixStack;
import com.squeed.swiper.helper.TextCreator;
import com.squeed.swiper.shader.Shader;

/**
 * Renders text
 * @author Erik
 *
 */
public class TextRenderer {
	
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	
	HashMap<String, Bitmap> registry = new HashMap<String, Bitmap>();
	
	private static int[] currentTextTextureId = new int[1];
	
	
	public void render(float x, float y, float z, String text, Shader shader) {
		GLES20.glUseProgram(shader.program);

		if(registry.containsKey(text)) {
			initTextTexture(registry.get(text));
		} else {
			Bitmap textBitmap = TextCreator.createText(text);	
			registry.put(text, textBitmap);
			initTextTexture(textBitmap);
		}
			
		GLES20.glUniform1f(shader.mTextureHandle, currentTextTextureId[0]);
		
		render(x, y, z, 0, 0, 0, getVerticies(text), currentTextTextureId[0], shader);
	}
	
	
	
	private FloatBuffer getVerticies(String text) {		
		int half = text.length() / 2;
		float vx = 0.3f*half;
		float vy = 0.3f;
		float mTriangleVerticesData[] = {
		    	// X, Y, Z, U, V
		    	-vx, vy, 0.0f, 0.0f, 0.0f,
		    	vx, vy, 0.0f, 1.0f, 0.0f,
		    	-vx, -vy, 0.0f, 0.0f, 1.0f,
		    	vx, -vy, 0.0f, 1.0f, 1.0f};
		
		/********* START GL ES2.0 code **************/
		FloatBuffer mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
        return mTriangleVertices;
	}

	private void initTextTexture(Bitmap bitmap) {
		GLES20.glBindTexture(GL_TEXTURE_2D, currentTextTextureId[0]);

		GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	
		GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

		//bitmap.recycle();		
	}
	
	
	private void render(float x, float y, float z, float xRot, float yRot, float zRot, FloatBuffer verticies, int textureId, Shader shader) {
		
		// 1. Make TEXTURE0 active and bind it.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        // 2. Enable blending
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        
        // 3. Feed the verticies to the vertex shader
        verticies.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(shader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticies);

        // 4. Feed texture coordinates to fragment shader
        verticies.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(shader.mPositionHandle);
        GLES20.glVertexAttribPointer(shader.mTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticies);
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
