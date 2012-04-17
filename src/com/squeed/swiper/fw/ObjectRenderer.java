package com.squeed.swiper.fw;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.shader.BasicTextureShader;
import com.squeed.swiper.shader.Shader;
import com.squeed.swiper.shapes.Buffers;
import com.squeed.swiper.shapes.BaseMesh;

/**
 * Generic renderer. Implements a few different "render" methods that
 * each take a {@link BaseMesh}, a Shader and some parameter variants.
 * 
 * @author Erik
 */
public class ObjectRenderer {
		
	public void render(BaseMesh shape, Shader shader) {
		GLES20.glUseProgram(shader.program);
		
		renderVBO(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.textureId, shader, 1.0f, shape.getVertexBufferIdx());
	}
	
	public void render(float x, float y, float z, float xRot, float yRot, float zRot, int textureId, Shader shader, float scale, int vertexBufferIdx) {
		renderVBO(x, y, z, xRot, yRot, zRot, textureId, shader, 1.0f, vertexBufferIdx);
	}
	
	/**
	 * A single attrib.
	 * @param shape
	 * @param shader
	 * @param attrib
	 * @param value
	 */
	public void render(BaseMesh shape, Shader shader, int attrib, float value) {
		GLES20.glUseProgram(shader.program);
//		checkGlError("glUseProgram");

		GLES20.glUniform1f(attrib, value);
				
		renderVBO(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.textureId, shader, 1.0f, shape.getVertexBufferIdx());
	}
	
	public void renderReflection(BaseMesh shape, Shader shader, int attrib, float value, float yOffset, float scale) {
		GLES20.glUseProgram(shader.program);
//		checkGlError("glUseProgram");

		GLES20.glUniform1f(attrib, value);		
				
		renderVBO(shape.x, shape.y-yOffset, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.textureId, shader, scale, shape.getVertexBufferIdx());
	}
	
	public void renderReflection(BaseMesh shape, Shader shader, int[] attribs, float[] values, float yOffset, float scale) {
		GLES20.glUseProgram(shader.program);
		for(int a = 0; a < attribs.length; a++) {
			GLES20.glUniform1f(attribs[a], values[a]);
		}
		renderVBO(shape.x, shape.y-yOffset, shape.z, shape.xRot, shape.yRot, shape.zRot, shape.textureId, shader, scale, shape.getVertexBufferIdx());
	}
	
	public void renderSolidColor(BaseMesh shape, Shader shader, int attrib, float[] value, int vertexBufferIdx) {
		GLES20.glUseProgram(shader.program);
		GLES20.glUniform3fv(attrib, 1, value, 0); // the value is a 3-element vector with rgb values.
		renderSolidColorVBO(shape.x, shape.y, shape.z, shape.xRot, shape.yRot, shape.zRot, shader, vertexBufferIdx);
	}

	
	public void renderBasicVBO(float x, float y, float z, float xRot, float yRot, float zRot, int textureId, BasicTextureShader shader, float scale, int vertexBufferIdx) {
		/** BLOCK 1, set glEnable state flags for texture and blending */
		// 1. Make TEXTURE0 active and bind it.
		setTextureAndEnableBlending(textureId);
        
        /** BLOCK 2, handle translations, rotations and scale. Feed the final computed MVP matrix to the shader */ 
        // 6. First translate to WHERE we want to draw...
        Matrix.setIdentityM(ContactCardsRenderer.mModelMatrix, 0);
        Matrix.translateM(ContactCardsRenderer.mModelMatrix, 0, x, y, z);
        
        // 7. ... and THEN rotate (and scale, if applicable)
        Matrix.rotateM(ContactCardsRenderer.mModelMatrix, 0, yRot, 0, 1.0f, 0);
        
        if(scale != 1.0f)
        	Matrix.scaleM(ContactCardsRenderer.mModelMatrix, 0, 1.0f, scale, 1.0f);
        
        // 8. Multiply the VMMatrix with the ModelMatrix, store the result in the ModelViewProjection Matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);
                
        // 9. Then multiply the projection matrix by the MVP matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);

        // 10. Feed the newly calculated MVP matrix to the shader
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
        
        /** BLOCK 3, Bind geometry from the vertex buffer object to the shader */
        bindVbo(shader, vertexBufferIdx);
		
		
		/** BLOCK 4, Draw and release vbo */ 
		// Draw
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void setTextureAndEnableBlending(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        // 2. Enable blending
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
	}

	private void bindVbo(Shader shader, int vertexBufferIdx) {
		// Pass in the verticies
		glBindBuffer(GL_ARRAY_BUFFER, Buffers.vboBuffer[vertexBufferIdx]);
		glEnableVertexAttribArray(shader.mPositionHandle);
		glVertexAttribPointer(shader.mPositionHandle, 3, GL_FLOAT, false, BaseMesh.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, 0);

		// Pass in the texture information
		glBindBuffer(GL_ARRAY_BUFFER, Buffers.vboBuffer[vertexBufferIdx]);
		glEnableVertexAttribArray(shader.mTextureHandle);
		glVertexAttribPointer(shader.mTextureHandle, 2, GL_FLOAT, false, BaseMesh.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, BaseMesh.TRIANGLE_VERTICES_DATA_UV_OFFSET*BaseMesh.FLOAT_SIZE_BYTES);
    	
	}
	
	public void renderVBO(float x, float y, float z, float xRot, float yRot, float zRot, int textureId, Shader shader, float scale, int vertexBufferIdx) {
		/** Block 1, some rendering pipeline setup */
		setTextureAndEnableBlending(textureId);
        
       
		
        /** BLOCK 2, handle translations, rotations and scale. Feed the final computed MVP matrix to the shader */ 
        // 6. First translate to WHERE we want to draw...
        Matrix.setIdentityM(ContactCardsRenderer.mModelMatrix, 0);
        Matrix.translateM(ContactCardsRenderer.mModelMatrix, 0, x, y, z);
        
        // 7. ... and THEN rotate (and scale, if applicable)
        Matrix.rotateM(ContactCardsRenderer.mModelMatrix, 0, yRot, 0, 1.0f, 0);
        
        if(scale != 1.0f)
        	Matrix.scaleM(ContactCardsRenderer.mModelMatrix, 0, 1.0f, scale, 1.0f);
        
        // 8. Multiply the VMMatrix with the ModelMatrix, store the result in the ModelViewProjection Matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);
        
        // Pass in the modelview matrix that we use for lights stuff
        // I.e, we use the ModelViewProjection matrix array, which currently only contains model * view.
        GLES20.glUniformMatrix4fv(shader.mMVMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0); 
        
        // 9. Then multiply the projection matrix by the MVP matrix.
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);

        // 10. Feed the newly calculated MVP matrix to the shader
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
        
        // 11. Feed the light position in eye-space to the shader
        Matrix.setIdentityM(ContactCardsRenderer.mLightModelMatrix, 0);
        Matrix.translateM(ContactCardsRenderer.mLightModelMatrix, 0, 0.0f, 0.0f, 7.0f);
                    
        Matrix.multiplyMV(ContactCardsRenderer.mLightPosInWorldSpace, 0, ContactCardsRenderer.mLightModelMatrix, 0, ContactCardsRenderer.mLightPosInModelSpace, 0);
        Matrix.multiplyMV(ContactCardsRenderer.mLightPosInEyeSpace, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mLightPosInWorldSpace, 0); 
        GLES20.glUniform3f(shader.mLightPosHandle, ContactCardsRenderer.mLightPosInEyeSpace[0], ContactCardsRenderer.mLightPosInEyeSpace[1], ContactCardsRenderer.mLightPosInEyeSpace[2]);
        	
        
        /** BLOCK 3, Bind geometry from the vertex buffer object to the shader */
        bindVbo(shader, vertexBufferIdx);
		
		// Pass in the normal information
		glBindBuffer(GL_ARRAY_BUFFER, Buffers.vboBuffer[vertexBufferIdx]);
		glEnableVertexAttribArray(shader.mNormalHandle);
		glVertexAttribPointer(shader.mNormalHandle, 3, GL_FLOAT, false, BaseMesh.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, BaseMesh.TRIANGLE_VERTICES_DATA_NORMAL_OFFSET*BaseMesh.FLOAT_SIZE_BYTES);
		
		/** BLOCK 4, Draw and release vbo */ 
		// Draw
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	
	public void renderSolidColorVBO(float x, float y, float z, float xRot, float yRot, float zRot, Shader shader, int vertexBufferIdx) {
     
        // Translate, then rotate, as always.
        Matrix.setIdentityM(ContactCardsRenderer.mModelMatrix, 0);
        Matrix.translateM(ContactCardsRenderer.mModelMatrix, 0, x, y, z);    
        Matrix.rotateM(ContactCardsRenderer.mModelMatrix, 0, yRot, 0, 1.0f, 0);
                
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mViewMatrix, 0, ContactCardsRenderer.mModelMatrix, 0);        
        Matrix.multiplyMM(ContactCardsRenderer.mModelViewProjectionMatrix, 0, ContactCardsRenderer.mProjectionMatrix, 0, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
        
        // Pass in the final Model View Projetion matrix.
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, ContactCardsRenderer.mModelViewProjectionMatrix, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, Buffers.vboBuffer[vertexBufferIdx]);
		glEnableVertexAttribArray(shader.mPositionHandle);
		glVertexAttribPointer(shader.mPositionHandle, 3, GL_FLOAT, false, BaseMesh.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, 0);
		
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ContactCard", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

	
	
}
