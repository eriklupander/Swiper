package com.squeed.swiper.shader;

public class PulseReflectionShader extends Shader {

	public int time;
	public int amount;

	public PulseReflectionShader(int program, String name,
			int mMVPMatrixHandle, int mMVMatrixHandle, int mPositionHandle, int mTextureHandle, int time, int amount, int mNormalHandle, int mLightPosHandle) {
		super(program, name, mMVPMatrixHandle, mMVMatrixHandle, mPositionHandle, mTextureHandle, mNormalHandle, mLightPosHandle);

		this.time = time;
		this.amount = amount;
	}

}
