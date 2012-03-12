package com.squeed.swiper.shader;

public class ReflectionShader extends Shader {

	public int amount;

	public ReflectionShader(int program, String name, int mMVPMatrixHandle, int mMVMatrixHandle,
			int mPositionHandle, int mTextureHandle, int amount, int mNormalHandle, int mLightHandle) {
		super(program, name, mMVPMatrixHandle, mMVMatrixHandle, mPositionHandle, mTextureHandle, mNormalHandle, mLightHandle);
		
		this.amount = amount;
	}

}
