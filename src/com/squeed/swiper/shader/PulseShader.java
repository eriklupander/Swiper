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

	public PulseShader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle, int mTextureHandle, int time) {
		super(program, name, mMVPMatrixHandle, mPositionHandle, mTextureHandle);
		this.timeHandle = time;
		
	}

}
