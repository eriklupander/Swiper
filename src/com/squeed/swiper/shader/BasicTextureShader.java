package com.squeed.swiper.shader;

/**
 * Encapsulates the standard fragment shader.
 * 
 * @author Erik
 *
 */
public class BasicTextureShader extends Shader {

	/**
	 * 
	 * @param program
	 * 		Handle to the program
	 * @param name
	 * 		A name
	 * @param mMVPMatrixHandle
	 * 		Handle to the ModelViewProjection matrix
	 * @param mPositionHandle
	 * 		Handle to the vertex position
	 * @param mTextureHandle
	 * 		Handle to the texture coordinates
	 */
	public BasicTextureShader(int program, String name, int mMVPMatrixHandle, int mPositionHandle, int mTextureHandle) {
		super(program, name, mMVPMatrixHandle, -1, mPositionHandle,
				mTextureHandle);
	}
}
