package com.squeed.swiper.shader;

/**
 * Used to encapsulate the "reflection" fragment shader.
 * 
 * @author Erik
 *
 */
public class ReflectionShader extends Shader {

	/**
	 * Used to control the amount of base transparency.
	 */
	public int amount;

	public ReflectionShader(int program, String name, int mMVPMatrixHandle, int mMVMatrixHandle,
			int mPositionHandle, int mTextureHandle, int amount, int mNormalHandle, int mLightHandle) {
		super(program, name, mMVPMatrixHandle, mMVMatrixHandle, mPositionHandle, mTextureHandle, mNormalHandle, mLightHandle);
		
		this.amount = amount;
	}

}
