package com.squeed.swiper.util;

public class Float4 {
    
    /**
     * Creates a new Float3 object with all fields set to zero.
     */
    public Float4(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    /**
     * Creates a new Float3 object from the provided parameters.
     * @param x the x value .
     * @param y the y value.
     * @param z the z value.
     * @param w the w value.
     */
    public Float4(float x, float y, float z, float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Float4(float[] xyzw) {
		this.x = xyzw[0];
		this.y = xyzw[1];
		this.z = xyzw[2];
		this.w = xyzw[3];
	}


    /**
     * Creates a String representation of this object.
     * @return this Float3 as a String object.
     */
    @Override
    public String toString(){
        return new String("["+x+","+y+","+z+","+w+"]");
    }

    public float x,y,z,w;
}

