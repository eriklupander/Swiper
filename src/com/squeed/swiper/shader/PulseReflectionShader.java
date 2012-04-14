package com.squeed.swiper.shader;

/**
 * Encapsulates the program where we both have the pulsating vertex shader and the reflection fragment shader.
 * @author Erik
 *
 */
public class PulseReflectionShader extends Shader {

	/**
	 * Handle to the 'uniform time' shader variable. 
	 */
	public int time;
	
	/**
	 * Used to control the amount of base transparency.
	 */
	public int amount;

	public PulseReflectionShader(int program, String name,
			int mMVPMatrixHandle, int mMVMatrixHandle, int mPositionHandle, int mTextureHandle, int time, int amount, int mNormalHandle, int mLightPosHandle) {
		super(program, name, mMVPMatrixHandle, mMVMatrixHandle, mPositionHandle, mTextureHandle, mNormalHandle, mLightPosHandle);

		this.time = time;
		this.amount = amount;
	}

}
