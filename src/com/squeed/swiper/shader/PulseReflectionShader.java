package com.squeed.swiper.shader;

public class PulseReflectionShader extends Shader {

	public int time;
	public int amount;

	public PulseReflectionShader(int program, String name,
			int mMVPMatrixHandle, int mPositionHandle, int mTextureHandle, int time, int amount) {
		super(program, name, mMVPMatrixHandle, mPositionHandle, mTextureHandle);
		// TODO Auto-generated constructor stub
		this.time = time;
		this.amount = amount;
	}

}
