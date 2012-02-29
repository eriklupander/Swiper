package com.squeed.swiper.shader;


/**
 * Encapsulates a single shader program, e.g. a program
 * reference and some basic handles we use to bind runtime
 * data (from Java) to the shader 
 * (using GLES20.glUniform1f(int handle, float value) for example.
 * 
 * @author Erik
 *
 */
public class Shader {
	
	/**
	 * Handle to the shader program (e.g. vertex/fragment shader combo)
	 */
	public int program;
	
	/**
	 * Just a name, might throw this away...
	 */
	public String name;
	
	/**
	 * Handle used to feed the MVP matrix to the shader.
	 */
	public int mMVPMatrixHandle;
	
	/**
	 * Handle used to feed vertex data to the shader
	 */
    public int mPositionHandle;
    
    /**
     * Handle used to feed texture coordinate data to the shader. (?) check
     */
    public int mTextureHandle;
    
    /**
     * Handle used to feed texture coordinate data to the shader. (?) check
     */
    public int mNormalHandle;
    
	

	public Shader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle) {
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mPositionHandle = mPositionHandle;
	}
    
	public Shader(int program, String name, int mMVPMatrixHandle, int mPositionHandle,
			int mTextureHandle) {
		super();
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mPositionHandle = mPositionHandle;
		this.mTextureHandle = mTextureHandle;
	}
	
	public Shader(int program, String name, int mMVPMatrixHandle, int mPositionHandle,
			int mTextureHandle, int mNormalHandle) {
		super();
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mPositionHandle = mPositionHandle;
		this.mTextureHandle = mTextureHandle;
		this.mNormalHandle = mNormalHandle;
	}
    
}
