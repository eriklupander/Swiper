package com.squeed.swiper.shader;


import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources.NotFoundException;
import android.opengl.GLES20;
import android.util.Log;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.R;

/**
 * This class holds some boilerplate code to load shaders. The actual shaders are stored in res/raw
 * 
 * This makes it really simple to switch between shaders in onDrawFrame()
 * 
 * Example: GLES20.glUseProgram(Shaders.mProgram);
 * 
 * @author Erik
 *
 */
public class Shaders {
	
	public static Shader defaultShader;
	public static PulseShader pulseShader;
	public static ReflectionShader reflectionShader;
	public static PulseReflectionShader pulseReflectionShader;	
	public static ColorShader colorShader;
	public static BasicTextureShader basicShader;

	private static final String TAG = "Shaders";
	
	public static void initBasichader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.basic_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.basic_fragment_shader);
		int mProgram = createProgram(vertexShaderSrc, fragmentShaderSrc, new String[]{"aPosition", "aTextureCoord"});
		
		if (mProgram == 0) {
        	throw new RuntimeException("Could not create default shader");	            
        }        
        
        int maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        
        int maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        
        int muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVPMatrix");
        }
        basicShader = new BasicTextureShader(mProgram, "Default Shader", muMVPMatrixHandle, maPositionHandle, maTextureHandle);
	}
	
	
	public static void initDefaultShader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.default_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.default_fragment_shader);

		int mProgram = createProgram(vertexShaderSrc, fragmentShaderSrc, new String[]{"aPosition", "aTextureCoord", "aNormal"});
        if (mProgram == 0) {
        	throw new RuntimeException("Could not create default shader");	            
        }        
        
        int maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        
        int maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        
        int muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVPMatrix");
        }
        
        int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
        checkGlError("glGetUniformLocation uMVMatrix");
        if (muMVMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVMatrix");
        }
                
        int maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        checkGlError("glGetAttribLocation aNormal");
        if (maNormalHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aNormal");
        }

        int muLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
        checkGlError("glGetUniformLocation uLightPos");
        if (muLightPosHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uLightPos");
        }
        defaultShader = new Shader(mProgram, "Default Shader", muMVPMatrixHandle, muMVMatrixHandle, maPositionHandle, maTextureHandle, maNormalHandle, muLightPosHandle);
	}
	
	
	private static String loadShaderFromResource(int resourceId) {
		try {
			InputStream is = ContactCardsRenderer.mContext.getResources().openRawResource(resourceId);
			byte[] buffer = new byte[is.available()];   
			is.read(buffer);  
			return new String(buffer);
		} catch (NotFoundException e) {
			Log.e("Shaders", "NotFoundException loading shader: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			Log.e("Shaders", "IOException loading shader: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}		
	}


	public static void initColorShader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.color_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.color_fragment_shader);
		
		int mProgram = createProgram(vertexShaderSrc, fragmentShaderSrc, new String[]{"aPosition", "rgb"});
        if (mProgram == 0) {
        	throw new RuntimeException("Could not create color shader");	            
        }
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (positionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }        

        int uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (uMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "rgb");
        checkGlError("glGetUniformLocation rgb");
        if (colorHandle == -1) {
            throw new RuntimeException("Could not get attrib location for rgb");
        }
        
        colorShader = new ColorShader(mProgram, "Color Shader", uMVPMatrixHandle, positionHandle, colorHandle);
	}
	
	/**
	 * Uses the standard vertex program, but the reflection pixel shader
	 */
	public static void initReflectionShader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.default_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.reflection_fragment_shader);
		
		int mProgramReflection = createProgram(vertexShaderSrc, fragmentShaderSrc, 
				new String[]{"aPosition", "aTextureCoord", "amount"}
		);
		
        if (mProgramReflection == 0) {
        	throw new RuntimeException("Could not create reflection pixel shader");	            
        }
        int maPositionHandleReflection = GLES20.glGetAttribLocation(mProgramReflection, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandleReflection == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        int maTextureHandleReflection = GLES20.glGetAttribLocation(mProgramReflection, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandleReflection == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        
        int maNormalHandle = GLES20.glGetAttribLocation(mProgramReflection, "aNormal");
        checkGlError("glGetAttribLocation aNormal");
        if (maNormalHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aNormal");
        }
       
        int muLightPosHandle = GLES20.glGetUniformLocation(mProgramReflection, "uLightPos");
        checkGlError("glGetUniformLocation uLightPos");
        if (muLightPosHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uLightPos");
        }
        
        int muMVPMatrixHandleReflection = GLES20.glGetUniformLocation(mProgramReflection, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandleReflection == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgramReflection, "uMVMatrix");
        checkGlError("glGetUniformLocation uMVMatrix");
        if (muMVMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVMatrix");
        }
        
        int maReflectionAmount = GLES20.glGetUniformLocation(mProgramReflection, "amount");
        checkGlError("glGetUniformLocation amount");
        if (maReflectionAmount == -1) {
            throw new RuntimeException("Could not get attrib location for amount");
        }
        
        reflectionShader = new ReflectionShader(mProgramReflection, "Reflection Shader", muMVPMatrixHandleReflection, muMVMatrixHandle, maPositionHandleReflection, maTextureHandleReflection, maReflectionAmount, maNormalHandle, muLightPosHandle);
	}
	
	
	
	/**
	 * For the pulsating contact cards, uses the pulsating vertex shader, the normal pixel shader
	 */
	public static void initPulseShader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.pulse_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.default_fragment_shader);
		int mProgramPulse = createProgram(vertexShaderSrc, fragmentShaderSrc, new String[]{"aPosition", "aTextureCoord", "time"});
        if (mProgramPulse == 0) {
        	throw new RuntimeException("Could not create pulse vertex shader");
        }
        int maPositionHandlePulse = GLES20.glGetAttribLocation(mProgramPulse, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandlePulse == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        int maTextureHandlePulse = GLES20.glGetAttribLocation(mProgramPulse, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandlePulse == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        int muMVPMatrixHandlePulse = GLES20.glGetUniformLocation(mProgramPulse, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandlePulse == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgramPulse, "uMVMatrix");
        checkGlError("glGetUniformLocation uMVMatrix");
        if (muMVMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVMatrix");
        }
        
        int maTimeHandlePulse = GLES20.glGetUniformLocation(mProgramPulse, "time");
        checkGlError("glGetUniformLocation time");
        if (maTimeHandlePulse == -1) {
            throw new RuntimeException("Could not get attrib location for time");
        }
        
        int maNormalHandle = GLES20.glGetAttribLocation(mProgramPulse, "aNormal");
        checkGlError("glGetAttribLocation aNormal");
        if (maNormalHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aNormal");
        }

        int muLightPosHandle = GLES20.glGetUniformLocation(mProgramPulse, "uLightPos");
        checkGlError("glGetUniformLocation uLightPos");
        if (muLightPosHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uLightPos");
        }
                
        pulseShader = new PulseShader(mProgramPulse, "Pulse Shader", muMVPMatrixHandlePulse, muMVMatrixHandle, maPositionHandlePulse, maTextureHandlePulse, maNormalHandle, maTimeHandlePulse, muLightPosHandle);
	}
	
	/**
	 * Program for the pulsating reflection, uses the pulsating vertex shader, the reflection pixel shader
	 */
	public static void initPulseReflectionShader() {
		String vertexShaderSrc = loadShaderFromResource(R.raw.pulse_vertex_shader);
		String fragmentShaderSrc = loadShaderFromResource(R.raw.reflection_fragment_shader);
		
		int mProgramPulseReflection = createProgram(vertexShaderSrc, fragmentShaderSrc, new String[]{"aPosition", "aTextureCoord", "time", "amount"});
        if (mProgramPulseReflection == 0) {
            return;
        }
        int maPositionHandlePulseReflection = GLES20.glGetAttribLocation(mProgramPulseReflection, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandlePulseReflection == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        int maTextureHandlePulseReflection = GLES20.glGetAttribLocation(mProgramPulseReflection, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandlePulseReflection == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        int muMVPMatrixHandlePulseReflection = GLES20.glGetUniformLocation(mProgramPulseReflection, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandlePulseReflection == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgramPulseReflection, "uMVMatrix");
        checkGlError("glGetUniformLocation uMVMatrix");
        if (muMVMatrixHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uMVMatrix");
        }
        
        int maTimeHandlePulseReflection = GLES20.glGetUniformLocation(mProgramPulseReflection, "time");
        checkGlError("glGetUniformLocation time");
        if (maTimeHandlePulseReflection == -1) {
            throw new RuntimeException("Could not get attrib location for time");
        }
        
        int maReflectionPulseAmount = GLES20.glGetUniformLocation(mProgramPulseReflection, "amount");
        checkGlError("glGetUniformLocation amount");
        if (maReflectionPulseAmount == -1) {
            throw new RuntimeException("Could not get attrib location for amount");
        }
        
        int maNormalHandle = GLES20.glGetAttribLocation(mProgramPulseReflection, "aNormal");
        checkGlError("glGetAttribLocation aNormal");
        if (maNormalHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aNormal");
        }

        int muLightPosHandle = GLES20.glGetUniformLocation(mProgramPulseReflection, "uLightPos");
        checkGlError("glGetUniformLocation uLightPos");
        if (muLightPosHandle == -1) {
            throw new RuntimeException("Could not get uniform location for uLightPos");
        }
        
        pulseReflectionShader = new PulseReflectionShader(mProgramPulseReflection, "Pulse Reflection Shader", muMVPMatrixHandlePulseReflection, muMVMatrixHandle, maPositionHandlePulseReflection, maTextureHandlePulseReflection, maTimeHandlePulseReflection, maReflectionPulseAmount, maNormalHandle, muLightPosHandle);
	}
	

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private static int createProgram(String vertexSource, String fragmentSource, String[] attributes) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int programHandle = GLES20.glCreateProgram();
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(programHandle, pixelShader);
            checkGlError("glAttachShader");
            
			// Bind attributes
//			if (attributes != null) {
//				final int size = attributes.length;
//				for (int i = 0; i < size; i++) {
//					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
//				}
//			}
            
            GLES20.glLinkProgram(programHandle);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        return programHandle;
    }
    
    private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
