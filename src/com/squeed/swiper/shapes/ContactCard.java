package com.squeed.swiper.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;

/**
 * Encapsulates data about a Contact Card. Holds stuff like name and Bitmap
 * 
 * @author Erik
 *
 */
public class ContactCard extends BaseMesh {
	
	/**
	 * Primary key (from SqlLite) of the contact.
	 */
	public String id;
	
	/**
	 * Name of the contact person.
	 */
	public String name;	
	
	public Bitmap picture;
	
	public static FloatBuffer verticesBuffer;
	
	// All contact cards share the same verticies, texture coords and normals.
    private static float mVerticesData[] = {
    	// X, Y, Z, U, V, Nx, Ny, Nz (U, V are texture coordinates, Nx... are normal data)
    	-1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
    	-1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    	1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,};
    
   
    
    static {
    	/********* START GL ES2.0 code **************/
		verticesBuffer = ByteBuffer.allocateDirect(mVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(mVerticesData).position(0);
	}
	
	public ContactCard(String id, String name, Bitmap picture)
    {
		this.id = id;
		this.name = name;
		this.picture = picture;	
    }
}
