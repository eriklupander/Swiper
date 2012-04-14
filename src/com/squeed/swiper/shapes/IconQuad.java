package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.squeed.swiper.actions.Command;

public class IconQuad extends BaseMesh {
	
	public Command actionWhenClicked;
	
	private static float vx = 0.3f;
	
	public static FloatBuffer verticesBuffer;	
	
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-vx, vx, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	vx, vx, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	-vx, -vx, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    	vx, -vx, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};

	static {
		/********* START GL ES2.0 code **************/
		verticesBuffer = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesBuffer.put(mTriangleVerticesData).position(0);
	}

	public IconQuad(Command actionWhenClicked, int colorIndex) {		
		this.actionWhenClicked = actionWhenClicked;
		this.colorIndex[0] = ((colorIndex>>16)&0x0ff) / 255.0f;
		this.colorIndex[1] = ((colorIndex>>8) &0x0ff) / 255.0f;
		this.colorIndex[2] = ((colorIndex)    &0x0ff) / 255.0f;
//		this.colorIndex[0] = ((colorIndex>>16)&0x0ff);
//		this.colorIndex[1] = ((colorIndex>>8) &0x0ff);
//		this.colorIndex[2] = ((colorIndex)    &0x0ff);
	}

}
