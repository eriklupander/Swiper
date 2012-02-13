package com.squeed.swiper.helper;

/**
 * Extremely simple Matrix Stack implementation.
 * 
 * Keeps up to 10 levels of depth for three 4x4 matrices.
 * 
 * @author Erik
 *
 */
public class MxStack {
	
	private static final int MAX_DEPTH = 10;
	
	public static float[] mStack = new float[MAX_DEPTH*16];
	public static float[] mStack2 = new float[MAX_DEPTH*16];
	public static float[] mStack3 = new float[MAX_DEPTH*16];
	
	private static int index = -1;
	private static int index2 = -1;
	private static int index3 = -1;

	
	
	public static void push(float[] matrix) {
		index++;
		for(int a = 0; a < 16; a++) {
			mStack[index*16+a] = matrix[a];
		}		
	}
	
	public static float[] pop(float[] ret) {
		for(int a = 0; a < 16; a++) {
			ret[a] = mStack[index*16+a];
		}
		index--;
		return ret;
	}
	
	
	
	public static void push2(float[] matrix) {
		index2++;
		for(int a = 0; a < 16; a++) {
			mStack2[index2*16+a] = matrix[a];
		}
		
	}
	
	public static float[] pop2(float[] ret) {
		for(int a = 0; a < 16; a++) {
			ret[a] = mStack2[index2*16+a];
		}
		index2--;
		return ret;
	}
	
	public static void push3(float[] matrix) {
		index3++;
		for(int a = 0; a < 16; a++) {
			mStack3[index3*16+a] = matrix[a];
		}
		
	}
	
	public static float[] pop3(float[] ret) {
		for(int a = 0; a < 16; a++) {
			ret[a] = mStack3[index3*16+a];
		}
		index3--;
		return ret;
	}
	
}
