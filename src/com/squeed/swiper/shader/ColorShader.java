package com.squeed.swiper.shader;

/**
 * VERY useful shader. Not pretty, but gives us the ability to render solid
 * color (to a FrameBufferObject) which we can subsequently use for picking purposes.
 * 
 * @author Erik
 *
 */
public class ColorShader extends Shader {

	/**
	 * Handle to the color.
	 */
	public int colorHandle;

	public ColorShader(int program, String name, int mMVPMatrixHandle,
			int mPositionHandle, int colorHandle) {
		super(program, name, mMVPMatrixHandle, -1, mPositionHandle);
		this.colorHandle = colorHandle;		
	}

}
