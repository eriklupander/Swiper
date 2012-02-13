package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.helper.MxStack;
import com.squeed.swiper.shader.Shaders;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Encapsulates data about a Contact Card. Holds both stuff like name and Bitmap, but
 * also the internal xyz 3D coords and XY 2D (translated) coords.
 * 
 * @author Erik
 *
 */
public class ContactCard extends MutableShape {
	
	
	
	
	/**
	 * Primary key (from SqlLite) of the contact.
	 */
	public String id;
	
	/**
	 * Name of the contact person.
	 */
	public String name;
		
	/**
	 * Texture ID to use when item is selected
	 */
	public int selectedTextureId = -1;
	
	public Bitmap picture;
	public Bitmap detailBitmap;
	public boolean isSelected = false;
	
	public int winX, winY;
	
	private static FloatBuffer mTriangleVertices;

//	public static char indicesAsChar[] = {
//        3, 2, 0, 1               
//	};
//	
//	public static char indicesAsChar2[] = {
//        0, 1, 2, 3           
//	};
	
    private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
    	1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
    	-1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
    	1.0f, -1.0f, 0.0f, 1.0f, 1.0f};
	
	public ContactCard(String id, String name, Bitmap picture)
    {
		this.id = id;
		this.name = name;
		this.picture = picture;
		        
		/********* START GL ES2.0 code **************/
		mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

    }
	
	
    public void draw(int textureId)
    {    	  
		
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.maPositionHandle);

        GLES20.glVertexAttribPointer(Shaders.maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        GLES20.glEnableVertexAttribArray(Shaders.maTextureHandle);
        
        MxStack.push(ContactCardsRenderer.mVMatrix);
        MxStack.push2(ContactCardsRenderer.mMMatrix);
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y, z);        
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
        
        
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.muMVPMatrixHandle, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // Reverse translations... urgh
        MxStack.pop2(ContactCardsRenderer.mMMatrix);
        MxStack.pop(ContactCardsRenderer.mVMatrix);
    }


    public void drawPulse(int textureId)
    {    	  
    	GLES20.glUniform1f(Shaders.maTimeHandlePulse, ContactCardsRenderer.time);
		
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.maPositionHandlePulse, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.maPositionHandlePulse);
        
        
        // checkGlError("glUniform1f maTimeHandlePulse");
        GLES20.glVertexAttribPointer(Shaders.maTextureHandlePulse, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(Shaders.maTextureHandlePulse);
        
        MxStack.push(ContactCardsRenderer.mVMatrix);
        MxStack.push2(ContactCardsRenderer.mMMatrix);
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y, z);        
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
        
        
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.muMVPMatrixHandlePulse, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // Reverse translations... urgh
        MxStack.pop2(ContactCardsRenderer.mMMatrix);
        MxStack.pop(ContactCardsRenderer.mVMatrix);
    }
    
    


    public void drawAsReflection(int textureId)
    {    	
    	GLES20.glUniform1f(Shaders.maReflectionAmount, ContactCardsRenderer.amount);
    	
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.maPositionHandleReflection, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.maPositionHandleReflection);

        GLES20.glVertexAttribPointer(Shaders.maTextureHandleReflection, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        GLES20.glEnableVertexAttribArray(Shaders.maTextureHandleReflection);
        
        MxStack.push(ContactCardsRenderer.mVMatrix);
        MxStack.push2(ContactCardsRenderer.mMMatrix);
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y-2.1f, z);        
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
        Matrix.scaleM(ContactCardsRenderer.mMMatrix, 0, 1.0f, -1.0f, 1.0f);
        
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.muMVPMatrixHandleReflection, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // Reverse translations... urgh
        MxStack.pop2(ContactCardsRenderer.mMMatrix);
        MxStack.pop(ContactCardsRenderer.mVMatrix);
            	
    }
    
    
    public void drawAsPulseReflection(int textureId)
    {    	
    	GLES20.glUniform1f(Shaders.maTimeHandlePulseReflection, ContactCardsRenderer.time);
    	GLES20.glUniform1f(Shaders.maReflectionPulseAmount, ContactCardsRenderer.amount);
    	
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(Shaders.maPositionHandlePulseReflection, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(Shaders.maPositionHandlePulseReflection);

        GLES20.glVertexAttribPointer(Shaders.maTextureHandlePulseReflection, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);

        GLES20.glEnableVertexAttribArray(Shaders.maTextureHandlePulseReflection);
        
        MxStack.push(ContactCardsRenderer.mVMatrix);
        MxStack.push2(ContactCardsRenderer.mMMatrix);
        
        Matrix.translateM(ContactCardsRenderer.mVMatrix, 0, x, y-2.1f, z);
        Matrix.setRotateM(ContactCardsRenderer.mMMatrix, 0, yRot, 0, 1.0f, 0);
        Matrix.scaleM(ContactCardsRenderer.mMMatrix, 0, 1.0f, -1.0f, 1.0f);
        
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mVMatrix, 0, ContactCardsRenderer.mMMatrix, 0);
        Matrix.multiplyMM(ContactCardsRenderer.mMVPMatrix, 0, ContactCardsRenderer.mProjMatrix, 0, ContactCardsRenderer.mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(Shaders.muMVPMatrixHandlePulseReflection, 1, false, ContactCardsRenderer.mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        

        // Reverse translations... urgh
        MxStack.pop2(ContactCardsRenderer.mMMatrix);
        MxStack.pop(ContactCardsRenderer.mVMatrix);
            	
    }

   private float[] trans = new float[12];
    
    public float[] getTranslatedVerticies() {
    	
    	trans[0] = x+mTriangleVerticesData[0];    	trans[1] = y+mTriangleVerticesData[1];    	trans[2] = z+mTriangleVerticesData[2];
    	trans[3] = x+mTriangleVerticesData[5];    	trans[4] = y+mTriangleVerticesData[6];    	trans[5] = z+mTriangleVerticesData[7];
    	trans[6] = x+mTriangleVerticesData[11];    	trans[7] = y+mTriangleVerticesData[12];    	trans[8] = z+mTriangleVerticesData[13];
    	trans[9] = x+mTriangleVerticesData[16];    	trans[10] = y+mTriangleVerticesData[17];    	trans[11] = z+mTriangleVerticesData[18];
    
    	return trans;

    }
    
    private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ContactCard", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
   
}
