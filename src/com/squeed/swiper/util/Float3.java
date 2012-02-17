package com.squeed.swiper.util;

public class Float3 {
    
    /**
     * Creates a new Float3 object with all fields set to zero.
     */
    public Float3(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Creates a new Float3 object from the provided parameters.
     * @param x the x value .
     * @param y the y value.
     * @param z the z value.
     */
    public Float3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Float3(float[] xyz) {
		this.x = xyz[0];
		this.y = xyz[1];
		this.z = xyz[2];
	}

	/**
     * The length of this Float3 object = sqrt(x^2 + y^2 + z^2)
     * @return the length of this Float3 object.
     */
    public float getLength(){
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    /**
     * The squared length of this Float3 object = (x^2 + y^2 + z^2)
     * @return the squared length of this Float3 object.
     */
    public float getLengthSquared(){
        return x*x+y*y+z*z;
    }

    /**
     * Normalizes this Float3 object.
     */
    public void normalize()
    {
        float l = getLength();
        if ( Math.abs(l) > Float.MIN_NORMAL)
        {
            x/=l;
            y/=l;
            z/=l;
        }
    }

    /**
     * Adds two Float3 objects together
     * @param op1 the first Float3 object to add.
     * @param op2 the second Float3 object to add.
     * @return a new Float3 object with the result of the add operation.
     */
    public static Float3 Add(Float3 op1, Float3 op2){
        return new Float3(op1.x+op2.x,op1.y+op2.y,op1.z+op2.z);
    }

    /**
     * Creates a linear combination of two operands. Returns the result of
     * a*op1+b*op2
     * @param a the coefficient for the first operand.
     * @param op1 the first Float3 operand.
     * @param b the coefficient for the second operand.
     * @param op2 the second Float3 operand.
     */
    public static Float3 LinearCombination(float a, Float3 op1, float b, Float3 op2){
        float x = a*op1.x+b*op2.x;
        float y = a*op1.y+b*op2.y;
        float z = a*op1.z+b*op2.z;
        return new Float3(x,y,z);
    }

    /**
     * Subtracts the second Float3 object from the first Float3 object.
     * @param op1 the first Float3 object to add.
     * @param op2 the second Float3 object to add.
     * @return a new Float3 object with the result of the subtract operation.
     */
    public static Float3 Subtract(Float3 op1, Float3 op2){
        return new Float3(op1.x-op2.x,op1.y-op2.y,op1.z-op2.z);
    }

    /**
     * Calculates the dot product of two Float3 objects.
     * @param op1 the first Float3 object.
     * @param op2 the second Float3 object.
     * @return the dot product of op1 and op2
     */
    public static float Dot(Float3 op1, Float3 op2){
        return op1.x*op2.x + op1.y*op2.y + op1.z*op2.z;
    }

    /**
     * Calculates the cross product of two Float3 objects.
     * @param op1 the first Float3 object.
     * @param op2 the second Float3 object.
     * @return the cross product of op1 and op2.
     */
    public static Float3 Cross(Float3 op1, Float3 op2){
        float xr = op1.y * op2.z - op1.z * op2.y;
        float yr = op1.z * op2.x - op1.x * op2.z;
        float zr = op1.x * op2.y - op1.y * op2.x;
        return new Float3(xr, yr, zr);
    }

    /**
     * Creates a String representation of this object.
     * @return this Float3 as a String object.
     */
    @Override
    public String toString(){
        return new String("["+x+","+y+","+z+"]");
    }

    public float x,y,z;
}

