package com.squeed.swiper.shader;

public class BasicTextureShader extends Shader {

	public BasicTextureShader(int program, String name, int mMVPMatrixHandle, int mPositionHandle, int mTextureHandle) {
		super(program, name, mMVPMatrixHandle, -1, mPositionHandle,
				mTextureHandle);
	}
}
