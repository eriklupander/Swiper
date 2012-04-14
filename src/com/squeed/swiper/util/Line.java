package com.squeed.swiper.util;

import java.util.ArrayList;

/**
 * @deprecated Currently not used in the swiper app.
 * @author Koen Samyn
 */
public class Line {
    public static void intersectPlane(ArrayList<Intersection> result,
            Float3 lp1, Float3 lp2, Float3 p1, Float3 p2, Float3 p3)
    {
        Float3 e1 = Float3.Subtract(p2,p1);
        Float3 e2 = Float3.Subtract(p3, p1);

        Float3 normal = Float3.Cross(e1,e2);
        intersectPlane(result, lp1,lp2,p1,normal);
    }

    public static void intersectPlane(ArrayList<Intersection> result,
            Float3 lp1, Float3 lp2, Float3 p1, Float3 normal)
    {
        Float3 ldir = Float3.Subtract(lp2,lp1);
        float numerator = Float3.Dot(normal,ldir);
        if ( Math.abs(numerator) > 0.000001f)
        {
            Float3 p1tolp1 = Float3.Subtract(p1,lp1);
            float t = Float3.Dot(normal, p1tolp1) / numerator;
            Float3 intersection = Float3.LinearCombination(1, lp1, t, ldir);
            result.add( new Intersection( intersection, t));
        }
    }
}
