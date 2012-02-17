package com.squeed.swiper.shader;

import android.opengl.GLES20;
import android.util.Log;

/**
 * This class holds some shader code and boilerplate code to load them.
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
	
	/** Start of handles to vertix/pixel shader programs and 'variables' **/
	//public static int mProgram;
   // public static int mProgramReflection;
   // public static int mProgramPulse;
    //public static int mProgramPulseReflection;

    //public static  int muMVPMatrixHandle;
    //public static  int maPositionHandle;
    //public static  int maTextureHandle;
    
//    public static  int muMVPMatrixHandleReflection;
//    public static  int maPositionHandleReflection;
//    public static  int maTextureHandleReflection;
//    public static  int maReflectionAmount;
	
//    public static  int muMVPMatrixHandlePulse;
//    public static  int maPositionHandlePulse;
//    public static  int maTextureHandlePulse;
//    public static  int maTimeHandlePulse;
    
//    public static  int muMVPMatrixHandlePulseReflection;
//    public static  int maPositionHandlePulseReflection;
//    public static  int maTextureHandlePulseReflection;
//    public static  int maTimeHandlePulseReflection;
//    public static  int maReflectionPulseAmount;
	
    /**
     * The standard vertex shader. This mimics the behaviour of the OpenGL ES 1.0 static vertex pipeline.
     */
	public static final String mVertexShader =
        "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * aPosition;\n" +
        "  vTextureCoord = aTextureCoord;\n" +
        "}\n";
	
	
	/**
     * The color vertex shader. E.g. no texture coordinate
     */
	public static final String mColorVertexShader =
        "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +     
        "void main() {\n" +       
        "  gl_Position = uMVPMatrix * aPosition;\n" +
        "}\n";
	
	/**
	 * The color only pixel shader
	 */
	public static final String mColorFragmentShader =
        "precision mediump float;\n" +   
        "uniform float color;\n" +
        "void main() {\n" +
        "  gl_FragColor = vec4(color, color, color, 1.0);\n" +
        "}\n";
	
	
	/**
	 * The vertex "pulsing" shader. Uses a sin function and a feeded "time"  in order to
	 * change the z-value based on the x-value.
	 */
	public static final String mVertexShaderPulse =		
		"uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform float time;\n" +
        "void main() {\n" +
        "  vec4 v = vec4(aPosition);\n" +
        "  v.z = sin(5.0*v.x + time*0.01)*0.25;\n" +
        "  gl_Position = uMVPMatrix * v;\n" +
        "  vTextureCoord = aTextureCoord;\n" +
        "}\n";
	
	/**
	 * The standard pixel shader, mimics the OpenGL ES 1.0 static pipeline.
	 */
	public static final String mFragmentShader =
        "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +	        
        "}\n";
	
	
	
    
	/**
	 * A more advanced pixel shader used to render the reflection effect. The texture coordinate
	 * is used to set a decreasing alpha value for the pixel.
	 */
	public static final String mFragmentShaderReflection =
    	"precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
        "uniform float amount;\n" +
        "void main() {\n" +
        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
        "  color[3] = ((vTextureCoord[1])/amount)-0.07;\n" +
        "  gl_FragColor = color;\n" +
        "}\n";
	private static final String TAG = null;
	
	
	public static void initDefaultShader() {
		
		int mProgram = createProgram(Shaders.mVertexShader, Shaders.mFragmentShader);
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
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        defaultShader = new Shader(mProgram, "Default Shader", muMVPMatrixHandle, maPositionHandle, maTextureHandle);
	}
	
	
	public static void initColorShader() {
		
		int mProgram = createProgram(Shaders.mColorVertexShader, Shaders.mColorFragmentShader);
        if (mProgram == 0) {
        	throw new RuntimeException("Could not create default shader");	            
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
        
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "color");
        checkGlError("glGetUniformLocation time");
        if (colorHandle == -1) {
            throw new RuntimeException("Could not get attrib location for time");
        }
        
        colorShader = new ColorShader(mProgram, "Color Shader", uMVPMatrixHandle, positionHandle, colorHandle);
	}
	
	/**
	 * Uses the standard vertex program, but the reflection pixel shader
	 */
	public static void initReflectionShader() {

		int mProgramReflection = createProgram(Shaders.mVertexShader, Shaders.mFragmentShaderReflection);
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

        int muMVPMatrixHandleReflection = GLES20.glGetUniformLocation(mProgramReflection, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandleReflection == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        
        int maReflectionAmount = GLES20.glGetUniformLocation(mProgramReflection, "amount");
        checkGlError("glGetUniformLocation amount");
        if (maReflectionAmount == -1) {
            throw new RuntimeException("Could not get attrib location for amount");
        }
        
        reflectionShader = new ReflectionShader(mProgramReflection, "Reflection Shader", muMVPMatrixHandleReflection, maPositionHandleReflection, maTextureHandleReflection, maReflectionAmount);
	}
	
	
	
	/**
	 * For the pulsating contact cards, uses the pulsating vertex shader, the normal pixel shader
	 */
	public static void initPulseShader() {
		int mProgramPulse = createProgram(Shaders.mVertexShaderPulse, Shaders.mFragmentShader);
        if (mProgramPulse == 0) {
        	throw new RuntimeException("Could not create pulse vertex shader");
            //return;
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
        
        int maTimeHandlePulse = GLES20.glGetUniformLocation(mProgramPulse, "time");
        checkGlError("glGetUniformLocation time");
        if (maTimeHandlePulse == -1) {
            throw new RuntimeException("Could not get attrib location for time");
        }
        
        pulseShader = new PulseShader(mProgramPulse, "Pulse Shader", muMVPMatrixHandlePulse, maPositionHandlePulse, maTextureHandlePulse, maTimeHandlePulse);
	}
	
	/**
	 * Program for the pulsating reflection, uses the pulsating vertex shader, the reflection pixel shader
	 */
	public static void initPulseReflectionShader() {
		int mProgramPulseReflection = createProgram(Shaders.mVertexShaderPulse, Shaders.mFragmentShaderReflection);
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
        
        pulseReflectionShader = new PulseReflectionShader(mProgramPulseReflection, "Pulse Reflection Shader", muMVPMatrixHandlePulseReflection, maPositionHandlePulseReflection, maTextureHandlePulseReflection, maTimeHandlePulseReflection, maReflectionPulseAmount);
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

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
    
    private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
