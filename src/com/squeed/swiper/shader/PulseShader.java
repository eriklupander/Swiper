package com.squeed.swiper.shader;

/**
 * Used to encapsulate the "pulsing" vertex shader, e.g. varying z-value by a sin(time + x-position) function.
 * 
 * @author Erik
 *
 */
public class PulseShader extends Shader {

	/**
	 * Handle to the 'uniform time' shader variable. 
	 */
	public int timeHandle;

	public PulseShader(int program, String name, int mMVPMatrixHandle, int mMVMatrixHandle,
			int mPositionHandle, int mTextureHandle, int mNormalHandle, int timeHandle, int mLightPosHandle) {
		super(program, name, mMVPMatrixHandle, mMVMatrixHandle, mPositionHandle, mTextureHandle, mNormalHandle, mLightPosHandle);
		this.timeHandle = timeHandle;		
	}

}
