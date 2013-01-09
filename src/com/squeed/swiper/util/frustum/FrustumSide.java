package com.squeed.swiper.util.frustum;

public enum FrustumSide {
	RIGHT(0),		// The RIGHT side of the frustum
	LEFT(1),		// The LEFT	 side of the frustum
	BOTTOM(2),		// The BOTTOM side of the frustum
	TOP	(3),		// The TOP side of the frustum
	BACK(4),		// The BACK	side of the frustum
	FRONT(5);			// The FRONT side of the frustum
	
	int index;
	
	private FrustumSide(int index) {
		this.index = index;
	}
	
	public int idx() {
		return index;
	}
}
