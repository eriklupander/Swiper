package com.squeed.swiper;

/**
 * This class holds flags that control internal rendering state flags. 
 * 
 * Render reflections or not, render solid color etc.
 * 
 * @author Erik
 *
 */
public class RenderState {
	
	static boolean renderPulse = false;
	static boolean renderSolid = false;
	static boolean renderReflection = false;
	static boolean renderBg = false;
	static boolean renderLightPos = true;
	
	public static void reset() {
		renderPulse = false;
		renderSolid = false;
		renderReflection = false;
		renderBg = false;
		renderLightPos = true;
	}
}
