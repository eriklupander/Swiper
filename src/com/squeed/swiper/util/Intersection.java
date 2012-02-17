package com.squeed.swiper.util;

public class Intersection {
    public Float3 intersection;
    public float t;

    public Intersection(Float3 intersection, float t){
        this.intersection = intersection;
        this.t = t;
    }

    public boolean isInside(){
        return t > 0 && t < 1;
    }

    public String toString(){
        return "Intersection : " + intersection.toString() + " , t = " + t;
    }
}
