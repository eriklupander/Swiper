package com.squeed.swiper.shader;

public class ColorShader extends Shader {

	public int colorHandle;

	public ColorShader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle, int colorHandle) {
		super(program, name, mMVPMatrixHandle, mPositionHandle);
		this.colorHandle = colorHandle;		
	}

}
