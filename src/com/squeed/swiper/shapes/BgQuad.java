package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BgQuad extends BaseMesh {
	
	
	private static final float quadY = 11f;
	private static final float quadX = 6.6f;
	private static float mTriangleVerticesData[] = {
    	// X, Y, Z, U, V
    	-quadX, quadY, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	quadX, quadY, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	-quadX, -quadY, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    	quadX, -quadY, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
	
    public int[] texture = new int[1];
    
    public static FloatBuffer verticesBuffer;
    
    static {
    	verticesBuffer = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                 * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	verticesBuffer.put(mTriangleVerticesData).position(0); 
    }

    public BgQuad(int[]texture) {
        this.texture = texture;
    }
}