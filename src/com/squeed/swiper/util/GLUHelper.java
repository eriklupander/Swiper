package com.squeed.swiper.util;

/**
 * See http://stackoverflow.com/questions/6699387/android-opengl-3d-picking, this is a workaround
 * for the broken(?) gluUnProject.
 * 
 */
public class GLUHelper {
	
	public float[] gluUnProject(float rx, float ry, float rz, MatrixGrabber mg, int[] viewport) {
		float[] xyzw = {0, 0, 0, 0};
		android.opengl.GLU.gluUnProject(rx, ry, rz, mg.mModelView, 0, mg.mProjection, 0, viewport, 0, xyzw, 0);
		xyzw[0] /= xyzw[3];
		xyzw[1] /= xyzw[3];
		xyzw[2] /= xyzw[3];
		//xyzw[3] /= xyzw[3];
		xyzw[3] = 1;
		return xyzw;
	}
}
