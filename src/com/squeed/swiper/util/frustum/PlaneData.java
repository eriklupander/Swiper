package com.squeed.swiper.util.frustum;

public enum PlaneData {
	
	A(0),				// The X value of the plane's normal
	B(1),				// The Y value of the plane's normal
	C(2),				// The Z value of the plane's normal
	D(3);
	
	int index;
	
	private PlaneData(int index) {
		this.index = index;
	}
	
	public int idx() {
		return index;
	}
}
