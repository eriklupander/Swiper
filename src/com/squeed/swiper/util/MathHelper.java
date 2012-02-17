package com.squeed.swiper.util;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;

public class MathHelper {

	public static float[] getViewRay(float x, float y, int nScreenWidth, int nScreenHeight, float[] posCamera, float[] modelViewMatrix, float[] projectionMatrix)
	{
	    // view port
	    int[] viewport = { 0, 0, nScreenWidth, nScreenHeight };
	 
	    // far eye point
	    float[] eye = new float[4];
	    GLU.gluUnProject(x, nScreenHeight - y, 0.9f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, eye, 0);
	 
	    // fix
	    if (eye[3] != 0)
	    {
	        eye[0] = eye[0] / eye[3];
	        eye[1] = eye[1] / eye[3];
	        eye[2] = eye[2] / eye[3];
	    }
	 
	    // ray vector
	    float[] ray = { eye[0] - posCamera[0], eye[1] - posCamera[1], eye[2] - posCamera[2], 0.0f };
	    return ray;
	}
	
	
	public static void quarternionToMatrix(float[] matrix, Float4 q) {
		matrix[0] = 1 - 2 * (q.y * q.y + q.z * q.z);
	    matrix[1] = 2 * (q.x * q.y + q.z * q.w);
	    matrix[2] = 2 * (q.x * q.z - q.y * q.w);
	    matrix[3] = 0;
	 
	    // Second Column
	    matrix[4] = 2 * (q.x * q.y - q.z * q.w);
	    matrix[5] = 1 - 2 * (q.x * q.x + q.z * q.z);
	    matrix[6] = 2 * (q.z * q.y + q.x * q.w);
	    matrix[7] = 0;
	 
	    // Third Column
	    matrix[8] = 2 * (q.x * q.z + q.y * q.w);
	    matrix[9] = 2 * (q.y * q.z - q.x * q.w);
	    matrix[10] = 1 - 2 * (q.x * q.x + q.y * q.y);
	    matrix[11] = 0;
	 
	    // Fourth Column
	    matrix[12] = 0;
	    matrix[13] = 0;
	    matrix[14] = 0;
	    matrix[15] = 1;
	}
	
	public static void multiplyQuarternion(Float4 q1, Float4 q2, Float4 newQ) {
		newQ.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
	    newQ.x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
	    newQ.y = q1.w * q2.y - q1.x * q2.z + q1.y * q2.w + q1.z * q2.x;
	    newQ.z = q1.w * q2.z + q1.x * q2.y - q1.y * q2.x + q1.z * q2.w;
	}
	
	
    
    
    
    private static final float[] _tempGluUnProjectData = new float[40];
    private static final int     _temp_m   = 0;
    private static final int     _temp_A   = 16;
    private static final int     _temp_in  = 32;
    private static final int     _temp_out = 36;
    public static int gluUnProject(float winx, float winy, float winz,
                    float model[], int offsetM,
                    float proj[], int offsetP,
                    int viewport[], int offsetV,
                float[] xyz, int offset)
    {
       /* Transformation matrices */
    //   float[] m = new float[16], A = new float[16];
    //   float[] in = new float[4], out = new float[4];

       /* Normalize between -1 and 1 */
       _tempGluUnProjectData[_temp_in]   = (winx - viewport[offsetV]) *
    2f / viewport[offsetV+2] - 1.0f;
       _tempGluUnProjectData[_temp_in+1] = (winy - viewport[offsetV+1]) *
    2f / viewport[offsetV+3] - 1.0f;
       _tempGluUnProjectData[_temp_in+2] = 2f * winz - 1.0f;
       _tempGluUnProjectData[_temp_in+3] = 1.0f;

       /* Get the inverse */
       android.opengl.Matrix.multiplyMM(_tempGluUnProjectData, _temp_A,
    proj, offsetP, model, offsetM);
       android.opengl.Matrix.invertM(_tempGluUnProjectData, _temp_m,
    _tempGluUnProjectData, _temp_A);

       android.opengl.Matrix.multiplyMV(_tempGluUnProjectData, _temp_out,
            _tempGluUnProjectData, _temp_m,
            _tempGluUnProjectData, _temp_in);
       if (_tempGluUnProjectData[_temp_out+3] == 0.0)
          return GL10.GL_FALSE;

       xyz[offset]  =  _tempGluUnProjectData[_temp_out  ] /
    _tempGluUnProjectData[_temp_out+3];
       xyz[offset+1] = _tempGluUnProjectData[_temp_out+1] /
    _tempGluUnProjectData[_temp_out+3];
       xyz[offset+2] = _tempGluUnProjectData[_temp_out+2] /
    _tempGluUnProjectData[_temp_out+3];
       return GL10.GL_TRUE;

    } 
}
