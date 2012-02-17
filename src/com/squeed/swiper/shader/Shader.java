package com.squeed.swiper.shader;

public class Shader {
	
	public int program;
	public String name;
	public int mMVPMatrixHandle;
    public int mPositionHandle;
    public int mTextureHandle;
    
	

	public Shader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle) {
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mPositionHandle = mPositionHandle;
	}
    
	public Shader(int program, String name, int mMVPMatrixHandle, int mPositionHandle,
			int mTextureHandle) {
		super();
		this.program = program;
		this.name = name;
		this.mMVPMatrixHandle = mMVPMatrixHandle;
		this.mPositionHandle = mPositionHandle;
		this.mTextureHandle = mTextureHandle;
	}
    
}
