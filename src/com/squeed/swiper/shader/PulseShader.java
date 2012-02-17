package com.squeed.swiper.shader;

public class PulseShader extends Shader {

	public int time;

	public PulseShader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle, int mTextureHandle, int time) {
		super(program, name, mMVPMatrixHandle, mPositionHandle, mTextureHandle);
		this.time = time;
		
	}

}
