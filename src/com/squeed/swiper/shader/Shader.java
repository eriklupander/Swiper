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
	 * Handle used to feed the ModelView matrix to the shader
	 */
	public int mMVMatrixHandle;
	
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

    /**
     * Handle to the light position.
     */
    public int mLightPosHandle;
	

	public Shader(int program, String name, int mMVPMatrixHandle,  int mMVMatrixHandle,
			int mPositionHandle) {
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mMVMatrixHandle = mMVMatrixHandle;
		this.mPositionHandle = mPositionHandle;
	}
    
	public Shader(int program, String name, int mMVPMatrixHandle,  int mMVMatrixHandle, int mPositionHandle,
			int mTextureHandle) {
		super();
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mMVMatrixHandle = mMVMatrixHandle;
		this.mPositionHandle = mPositionHandle;
		this.mTextureHandle = mTextureHandle;
	}
	
	/**
	 * 
	 * @param program
	 * 		Handle to the shader program
	 * @param name
	 * 		A name for the shader program. Not used by OpenGL ES 2.0
	 * @param mMVPMatrixHandle
	 * 		Handle to the ModelViewProjection matrix
	 * @param mMVMatrixHandle
	 * 		Handle to the ModelView matrix
	 * @param mPositionHandle
	 * 		Handle to the vertex position
	 * @param mTextureHandle
	 * 		Handle to the texture coordinates
	 * @param mNormalHandle
	 * 		Handle to the normal vector
	 * @param mLightHandle
	 * 		Handle to the light position
	 */
	public Shader(int program, String name, int mMVPMatrixHandle,  int mMVMatrixHandle, int mPositionHandle,
			int mTextureHandle, int mNormalHandle, int mLightHandle) {
		super();
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mMVMatrixHandle = mMVMatrixHandle;
		this.mPositionHandle = mPositionHandle;
		this.mTextureHandle = mTextureHandle;
		this.mNormalHandle = mNormalHandle;
		this.mLightPosHandle = mLightHandle;
	}
    
}
