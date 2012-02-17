package com.squeed.swiper.fw;

import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.helper.MatrixStack;
import com.squeed.swiper.shader.Shader;
import com.squeed.swiper.shapes.MutableShape;

/**
 * Generic renderer. Implements a few different "render" methods that
 * each take a {@link MutableShape}, a Shader and some parameter variants.
 * 
 * @author Erik
 */
public class ObjectRenderer {
	
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	
	public void render(MutableShape shape, Shader shader) {
		GLES20.glUseProgram(shader.program);
		
		render(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shape.textureId, shader, 1.0f);
	}
	
	/**
	 * A single attrib.
	 * @param shape
	 * @param shader
	 * @param attrib
	 * @param value
	 */
	public void render(MutableShape shape, Shader shader, int attrib, float value) {
		GLES20.glUseProgram(shader.program);
//		checkGlError("glUseProgram");

		GLES20.glUniform1f(attrib, value);		
				
		render(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shape.textureId, shader, 1.0f);
	}
	
	public void renderReflection(MutableShape shape, Shader shader, int attrib, float value, float yOffset, float scale) {
		GLES20.glUseProgram(shader.program);
//		checkGlError("glUseProgram");

		GLES20.glUniform1f(attrib, value);		
				
		render(shape.x, shape.y-yOffset, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shape.textureId, shader, scale);
	}
	
	public void renderReflection(MutableShape shape, Shader shader, int[] attribs, float[] values, float yOffset, float scale) {
		GLES20.glUseProgram(shader.program);
		for(int a = 0; a < attribs.length; a++) {
			GLES20.glUniform1f(attribs[a], values[a]);
		}
		render(shape.x, shape.y-yOffset, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shape.textureId, shader, scale);
	}
	
	public void renderSolidColor(MutableShape shape, Shader shader, int attrib, float value) {
		GLES20.glUseProgram(shader.program);
		GLES20.glUniform1f(attrib, value/255.0f);
		renderSolidColor(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shader);
	}

	
	/**
	 * 
	 * @param shape
	 * @param shader
	 * @param attribs
	 * 		Length of attribs must be 0, 2, 4... first value is handle to attribute, the second is value to use for that attribute.
	 */
	public void render(MutableShape shape, Shader shader, int[] attribs, float[] values) {
		GLES20.glUseProgram(shader.program);
//		checkGlError("glUseProgram");
		
		for(int a = 0; a < attribs.length; a++) {
			GLES20.glUniform1f(attribs[a], values[a]);
		} // Shaders.maTimeHandlePulse, ContactCardsRenderer.time
				
		render(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.verticesBuffer, shape.textureId, shader, 1.0f);
	}

	public void render(float x, float y, float z, float xRot, float yRot, float zRot, FloatBuffer verticies, int textureId, Shader shader, float scale) {
		
		// 1. Make TEXTURE0 active and bind it.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
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
        MatrixStack.push(ContactCardsRenderer.mVMatrix);
        MatrixStack.push2(ContactCardsRenderer.mMMatrix);
        
        // 6. First translate to WHERE we want to draw...
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y, z);    
        
        // 7. ... and THEN rotate (and scale, if applicable)
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
        if(scale != 1.0f)
        	Matrix.scaleM(ContactCardsRenderer.mMMatrix, 0, 1.0f, scale, 1.0f);
        
        // 8. Multiply the VMMatrix with the ModelMatrix, store the result in the ModelViewProject Matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        
        // 9. Then multiply the projection matrix by the MVP matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        // 10. Feed the newly calculated MVP matrix to the shader
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mMVPMatrix, 0);

        // 11. And DRAW! (TODO switch to VertexBufferObject). Only 10 steps before we could actually draw something. OpenGL ES 2.0 FTW :-)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // 12. Finally, reverse translations... urgh, this should probably be performed using some fancy inverse transform...
        MatrixStack.pop2(ContactCardsRenderer.mMMatrix);
        MatrixStack.pop(ContactCardsRenderer.mVMatrix);
	}
	
	
	
	private void renderSolidColor(float x, float y, float z, float xRot, float yRot, float zRot, FloatBuffer verticies, Shader shader) {
		
       
        verticies.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(shader.mPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticies);

        // Removed the UV stuff since that's not needed when rendering to a solid color.
        
        MatrixStack.push(ContactCardsRenderer.mVMatrix);
        MatrixStack.push2(ContactCardsRenderer.mMMatrix);
        
        // Translate, then rotate, as always.
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y, z);    
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
                
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
		
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // Reverse translations... urgh, this should probably be performed using some fancy inverse transform...
        // Im quite fond of the old-school OpenGL glPushMatrix() and glPopMatrix() I guess.
        MatrixStack.pop2(ContactCardsRenderer.mMMatrix);
        MatrixStack.pop(ContactCardsRenderer.mVMatrix);
	}
	
	
	private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ContactCard", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

	
	
}
