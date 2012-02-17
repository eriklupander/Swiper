package com.squeed.swiper.helper;

/**
 * Helper class that can dump a matrix into a string for debugging purposes.
 * 
 * @author Erik
 *
 */
public class MatrixLogger {
	
	public static String vector3ToString(float[] vector) {
		if(vector.length != 3) {
			throw new IllegalArgumentException("This log method only accepts 3 vector");
		}
		return "[" + vector[0] + "][" + vector[1] + "][" + vector[2] + "]";
	}
	
	public static String vector4ToString(float[] vector) {
		if(vector.length != 4) {
			throw new IllegalArgumentException("This log method only accepts 4 vector");
		}
		return "[" + vector[0] + "][" + vector[1] + "][" + vector[2] + "][" + vector[3] + "]";
	}
	
	public static String matrix44ToString(float[] matrix) {
		if(matrix.length != 16) {
			throw new IllegalArgumentException("This log method only accepts 4x4 matrix");
		}
		return "[" + matrix[0] + "][" + matrix[1] + "][" + matrix[2] + "][" + matrix[3] + "]\n" +
				"[" + matrix[4] + "][" + matrix[5] + "][" + matrix[6] + "][" + matrix[7] + "]\n" +
				"[" + matrix[8] + "][" + matrix[9] + "][" + matrix[10] + "][" + matrix[11] + "]\n" +
				"[" + matrix[12] + "][" + matrix[13] + "][" + matrix[14] + "][" + matrix[15] + "]";
	}
	
	public static String vectorSphereToString(float[] sphere) {
		if(sphere.length != 4) {
			throw new IllegalArgumentException("This log method only accepts 4 vector");
		}
		return "x[" + sphere[0] + "] y[" + sphere[1] + "] z[" + sphere[2] + "] r[" + sphere[3] + "]";
	}
}
