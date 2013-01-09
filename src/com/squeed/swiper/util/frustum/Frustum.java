package com.squeed.swiper.util.frustum;

/**
 * This is a port of an ancient (2001) .cpp class I had lying around, original credits goes to 
 * DigiBen		digiben@gametutorials.com and his "Talk to me like I'm a 3 year old!" programming lessons.
 * 
 * I've adapted this one to take the modelview and projection matrices as parameters as this class
 * is meant to be used with OpenGL ES 2.0 which keeps the matricies in code.
 * 
 * @author erik
 */
public final class Frustum {
	
	float[][] m_Frustum = new float[6][4];

	private final static void normalizePlane(float[][] frustum, int side) {
		
		// Here we calculate the magnitude of the normal to the plane (point A B C)
		// Remember that (A, B, C) is that same thing as the normal's (X, Y, Z).
		// To calculate magnitude you use the equation:  magnitude = sqrt( x^2 + y^2 + z^2)
		float magnitude = (float)android.util.FloatMath.sqrt( frustum[side][PlaneData.A.idx()] * frustum[side][PlaneData.A.idx()] + 
									   frustum[side][PlaneData.B.idx()] * frustum[side][PlaneData.B.idx()] + 
									   frustum[side][PlaneData.C.idx()] * frustum[side][PlaneData.C.idx()] );

		// Then we divide the plane's values by it's magnitude.
		// This makes it easier to work with.
		frustum[side][PlaneData.A.idx()] /= magnitude;
		frustum[side][PlaneData.B.idx()] /= magnitude;
		frustum[side][PlaneData.C.idx()] /= magnitude;
		frustum[side][PlaneData.D.idx()] /= magnitude; 
	}
	
	public final void calculateFrustum(float[] projMatrix, float[] mvMatrix) {    
//		float[]   proj = new float[16];								// This will hold our projection matrix
//		float[]   modl = new float[16];								// This will hold our modelview matrix
		float[]   clip = new float[16];								// This will hold the clipping planes

		// glGetFloatv() is used to extract information about our OpenGL world.
		// Below, we pass in GL_PROJECTION_MATRIX to abstract our projection matrix.
		// It then stores the matrix into an array of [16].
		//glGetFloatv( GL_PROJECTION_MATRIX, proj );

		// By passing in GL_MODELVIEW_MATRIX, we can abstract our model view matrix.
		// This also stores it in an array of [16].
		//glGetFloatv( GL_MODELVIEW_MATRIX, modl );

		// Now that we have our modelview and projection matrix, if we combine these 2 matrices,
		// it will give us our clipping planes.  To combine 2 matrices, we multiply them.

		clip[ 0] = mvMatrix[ 0] * projMatrix[ 0] + mvMatrix[ 1] * projMatrix[ 4] + mvMatrix[ 2] * projMatrix[ 8] + mvMatrix[ 3] * projMatrix[12];
		clip[ 1] = mvMatrix[ 0] * projMatrix[ 1] + mvMatrix[ 1] * projMatrix[ 5] + mvMatrix[ 2] * projMatrix[ 9] + mvMatrix[ 3] * projMatrix[13];
		clip[ 2] = mvMatrix[ 0] * projMatrix[ 2] + mvMatrix[ 1] * projMatrix[ 6] + mvMatrix[ 2] * projMatrix[10] + mvMatrix[ 3] * projMatrix[14];
		clip[ 3] = mvMatrix[ 0] * projMatrix[ 3] + mvMatrix[ 1] * projMatrix[ 7] + mvMatrix[ 2] * projMatrix[11] + mvMatrix[ 3] * projMatrix[15];

		clip[ 4] = mvMatrix[ 4] * projMatrix[ 0] + mvMatrix[ 5] * projMatrix[ 4] + mvMatrix[ 6] * projMatrix[ 8] + mvMatrix[ 7] * projMatrix[12];
		clip[ 5] = mvMatrix[ 4] * projMatrix[ 1] + mvMatrix[ 5] * projMatrix[ 5] + mvMatrix[ 6] * projMatrix[ 9] + mvMatrix[ 7] * projMatrix[13];
		clip[ 6] = mvMatrix[ 4] * projMatrix[ 2] + mvMatrix[ 5] * projMatrix[ 6] + mvMatrix[ 6] * projMatrix[10] + mvMatrix[ 7] * projMatrix[14];
		clip[ 7] = mvMatrix[ 4] * projMatrix[ 3] + mvMatrix[ 5] * projMatrix[ 7] + mvMatrix[ 6] * projMatrix[11] + mvMatrix[ 7] * projMatrix[15];

		clip[ 8] = mvMatrix[ 8] * projMatrix[ 0] + mvMatrix[ 9] * projMatrix[ 4] + mvMatrix[10] * projMatrix[ 8] + mvMatrix[11] * projMatrix[12];
		clip[ 9] = mvMatrix[ 8] * projMatrix[ 1] + mvMatrix[ 9] * projMatrix[ 5] + mvMatrix[10] * projMatrix[ 9] + mvMatrix[11] * projMatrix[13];
		clip[10] = mvMatrix[ 8] * projMatrix[ 2] + mvMatrix[ 9] * projMatrix[ 6] + mvMatrix[10] * projMatrix[10] + mvMatrix[11] * projMatrix[14];
		clip[11] = mvMatrix[ 8] * projMatrix[ 3] + mvMatrix[ 9] * projMatrix[ 7] + mvMatrix[10] * projMatrix[11] + mvMatrix[11] * projMatrix[15];

		clip[12] = mvMatrix[12] * projMatrix[ 0] + mvMatrix[13] * projMatrix[ 4] + mvMatrix[14] * projMatrix[ 8] + mvMatrix[15] * projMatrix[12];
		clip[13] = mvMatrix[12] * projMatrix[ 1] + mvMatrix[13] * projMatrix[ 5] + mvMatrix[14] * projMatrix[ 9] + mvMatrix[15] * projMatrix[13];
		clip[14] = mvMatrix[12] * projMatrix[ 2] + mvMatrix[13] * projMatrix[ 6] + mvMatrix[14] * projMatrix[10] + mvMatrix[15] * projMatrix[14];
		clip[15] = mvMatrix[12] * projMatrix[ 3] + mvMatrix[13] * projMatrix[ 7] + mvMatrix[14] * projMatrix[11] + mvMatrix[15] * projMatrix[15];
		
		// Now we actually want to get the sides of the frustum.  To do this we take
		// the clipping planes we received above and extract the sides from them.

		// This will extract the RIGHT side of the frustum
		m_Frustum[FrustumSide.RIGHT.idx()][PlaneData.A.idx()] = clip[ 3] - clip[ 0];
		m_Frustum[FrustumSide.RIGHT.idx()][PlaneData.B.idx()] = clip[ 7] - clip[ 4];
		m_Frustum[FrustumSide.RIGHT.idx()][PlaneData.C.idx()] = clip[11] - clip[ 8];
		m_Frustum[FrustumSide.RIGHT.idx()][PlaneData.D.idx()] = clip[15] - clip[12];

		// Now that we have a normal (A,B,C) and a distance (D) to the plane,
		// we want to normalize that normal and distance.

		// Normalize the RIGHT side
		normalizePlane(m_Frustum, FrustumSide.RIGHT.idx());

		// This will extract the LEFT side of the frustum
		m_Frustum[FrustumSide.LEFT.idx()][PlaneData.A.idx()] = clip[ 3] + clip[ 0];
		m_Frustum[FrustumSide.LEFT.idx()][PlaneData.B.idx()] = clip[ 7] + clip[ 4];
		m_Frustum[FrustumSide.LEFT.idx()][PlaneData.C.idx()] = clip[11] + clip[ 8];
		m_Frustum[FrustumSide.LEFT.idx()][PlaneData.D.idx()] = clip[15] + clip[12];

		// Normalize the LEFT side
		normalizePlane(m_Frustum, FrustumSide.LEFT.idx());

		// This will extract the BOTTOM side of the frustum
		m_Frustum[FrustumSide.BOTTOM.idx()][PlaneData.A.idx()] = clip[ 3] + clip[ 1];
		m_Frustum[FrustumSide.BOTTOM.idx()][PlaneData.B.idx()] = clip[ 7] + clip[ 5];
		m_Frustum[FrustumSide.BOTTOM.idx()][PlaneData.C.idx()] = clip[11] + clip[ 9];
		m_Frustum[FrustumSide.BOTTOM.idx()][PlaneData.D.idx()] = clip[15] + clip[13];

		// Normalize the BOTTOM side
		normalizePlane(m_Frustum, FrustumSide.BOTTOM.idx());

		// This will extract the TOP side of the frustum
		m_Frustum[FrustumSide.TOP.idx()][PlaneData.A.idx()] = clip[ 3] - clip[ 1];
		m_Frustum[FrustumSide.TOP.idx()][PlaneData.B.idx()] = clip[ 7] - clip[ 5];
		m_Frustum[FrustumSide.TOP.idx()][PlaneData.C.idx()] = clip[11] - clip[ 9];
		m_Frustum[FrustumSide.TOP.idx()][PlaneData.D.idx()] = clip[15] - clip[13];

		// Normalize the TOP side
		normalizePlane(m_Frustum, FrustumSide.TOP.idx());

		// This will extract the BACK side of the frustum
		m_Frustum[FrustumSide.BACK.idx()][PlaneData.A.idx()] = clip[ 3] - clip[ 2];
		m_Frustum[FrustumSide.BACK.idx()][PlaneData.B.idx()] = clip[ 7] - clip[ 6];
		m_Frustum[FrustumSide.BACK.idx()][PlaneData.C.idx()] = clip[11] - clip[10];
		m_Frustum[FrustumSide.BACK.idx()][PlaneData.D.idx()] = clip[15] - clip[14];

		// Normalize the BACK side
		normalizePlane(m_Frustum, FrustumSide.BACK.idx());

		// This will extract the FRONT side of the frustum
		m_Frustum[FrustumSide.FRONT.idx()][PlaneData.A.idx()] = clip[ 3] + clip[ 2];
		m_Frustum[FrustumSide.FRONT.idx()][PlaneData.B.idx()] = clip[ 7] + clip[ 6];
		m_Frustum[FrustumSide.FRONT.idx()][PlaneData.C.idx()] = clip[11] + clip[10];
		m_Frustum[FrustumSide.FRONT.idx()][PlaneData.D.idx()] = clip[15] + clip[14];

		// Normalize the FRONT side
		normalizePlane(m_Frustum, FrustumSide.FRONT.idx());
	}
	
	public final boolean pointInFrustum( float x, float y, float z ) {
		// If you remember the plane equation (A*x + B*y + C*z + D = 0), then the rest
		// of this code should be quite obvious and easy to figure out yourself.
		// In case don't know the plane equation, it might be a good idea to look
		// at our Plane Collision tutorial at www.GameTutorials.com in OpenGL Tutorials.
		// I will briefly go over it here.  (A,B,C) is the (X,Y,Z) of the normal to the plane.
		// They are the same thing... but just called ABC because you don't want to say:
		// (x*x + y*y + z*z + d = 0).  That would be wrong, so they substitute them.
		// the (x, y, z) in the equation is the point that you are testing.  The D is
		// The distance the plane is from the origin.  The equation ends with "= 0" because
		// that is true when the point (x, y, z) is ON the plane.  When the point is NOT on
		// the plane, it is either a negative number (the point is behind the plane) or a
		// positive number (the point is in front of the plane).  We want to check if the point
		// is in front of the plane, so all we have to do is go through each point and make
		// sure the plane equation goes out to a positive number on each side of the frustum.
		// The result (be it positive or negative) is the distance the point is front the plane.

		// Go through all the sides of the frustum
		for(int i = 0; i < 6; i++ )
		{
			// Calculate the plane equation and check if the point is behind a side of the frustum
			if(m_Frustum[i][PlaneData.A.idx()] * x + m_Frustum[i][PlaneData.B.idx()] * y + m_Frustum[i][PlaneData.C.idx()] * z + m_Frustum[i][PlaneData.D.idx()] <= 0)
			{
				// The point was behind a side, so it ISN'T in the frustum
				return false;
			}
		}

		// The point was inside of the frustum (In front of ALL the sides of the frustum)
		return true;
	}
	
	
	public final boolean sphereInFrustum( float x, float y, float z, float radius )
	{
		// Now this function is almost identical to the PointInFrustum(), except we
		// now have to deal with a radius around the point.  The point is the center of
		// the radius.  So, the point might be outside of the frustum, but it doesn't
		// mean that the rest of the sphere is.  It could be half and half.  So instead of
		// checking if it's less than 0, we need to add on the radius to that.  Say the
		// equation produced -2, which means the center of the sphere is the distance of
		// 2 behind the plane.  Well, what if the radius was 5?  The sphere is still inside,
		// so we would say, if(-2 < -5) then we are outside.  In that case it's false,
		// so we are inside of the frustum, but a distance of 3.  This is reflected below.

		// Go through all the sides of the frustum
		for(int i = 0; i < 6; i++ )	
		{
			// If the center of the sphere is farther away from the plane than the radius
			if( m_Frustum[i][PlaneData.A.idx()] * x + m_Frustum[i][PlaneData.B.idx()] * y + m_Frustum[i][PlaneData.C.idx()] * z + m_Frustum[i][PlaneData.D.idx()] <= -radius )
			{
				// The distance was greater than the radius so the sphere is outside of the frustum
				return false;
			}
		}
		
		// The sphere was inside of the frustum!
		return true;
	}
	
	
	public final boolean cubeInFrustum( float x, float y, float z, float size )
	{
		// This test is a bit more work, but not too much more complicated.
		// Basically, what is going on is, that we are given the center of the cube,
		// and half the length.  Think of it like a radius.  Then we checking each point
		// in the cube and seeing if it is inside the frustum.  If a point is found in front
		// of a side, then we skip to the next side.  If we get to a plane that does NOT have
		// a point in front of it, then it will return false.

		// *Note* - This will sometimes say that a cube is inside the frustum when it isn't.
		// This happens when all the corners of the bounding box are not behind any one plane.
		// This is rare and shouldn't effect the overall rendering speed.

		for(int i = 0; i < 6; i++ )
		{
			if(m_Frustum[i][PlaneData.A.idx()] * (x - size) + m_Frustum[i][PlaneData.B.idx()] * (y - size) + m_Frustum[i][PlaneData.C.idx()] * (z - size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x + size) + m_Frustum[i][PlaneData.B.idx()] * (y - size) + m_Frustum[i][PlaneData.C.idx()] * (z - size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x - size) + m_Frustum[i][PlaneData.B.idx()] * (y + size) + m_Frustum[i][PlaneData.C.idx()] * (z - size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x + size) + m_Frustum[i][PlaneData.B.idx()] * (y + size) + m_Frustum[i][PlaneData.C.idx()] * (z - size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x - size) + m_Frustum[i][PlaneData.B.idx()] * (y - size) + m_Frustum[i][PlaneData.C.idx()] * (z + size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x + size) + m_Frustum[i][PlaneData.B.idx()] * (y - size) + m_Frustum[i][PlaneData.C.idx()] * (z + size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x - size) + m_Frustum[i][PlaneData.B.idx()] * (y + size) + m_Frustum[i][PlaneData.C.idx()] * (z + size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;
			if(m_Frustum[i][PlaneData.A.idx()] * (x + size) + m_Frustum[i][PlaneData.B.idx()] * (y + size) + m_Frustum[i][PlaneData.C.idx()] * (z + size) + m_Frustum[i][PlaneData.D.idx()] > 0)
			   continue;

			// If we get here, it isn't in the frustum
			return false;
		}

		return true;
	}
}
