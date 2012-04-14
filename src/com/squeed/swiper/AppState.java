package com.squeed.swiper;

/**
 * This class holds some flags that define some application states, e.g.
 * In selection mode?
 * Currently bouncing?
 * Currently zooming in/out?
 * 
 * For rendering state flags, see {@link RenderState}
 * 
 * @author Erik
 *
 */
public class AppState {
	
	public static boolean inSelectionMode = false;
	
	static boolean isBouncing = false;		
	static boolean hasBeenPaused = false;
	
	// Some more state flags
	static boolean isZoomToFront = false;
	static boolean isZoomToBack = false;

	public static void reset() {
		inSelectionMode = false;
		isBouncing = false;
		hasBeenPaused = false;
		isZoomToFront = false;
		isZoomToBack = true;
	}
}
