package com.squeed.swiper.shader;

public class ReflectionShader extends Shader {

	public int amount;

	public ReflectionShader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle, int mTextureHandle, int amount) {
		super(program, name, mMVPMatrixHandle, mPositionHandle, mTextureHandle);
		
		this.amount = amount;
	}

}
