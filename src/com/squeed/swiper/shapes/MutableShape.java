package com.squeed.swiper.shapes;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

import com.squeed.swiper.fw.Transition;

/**
 * The base class for 3D-meshes. Contains common stuff such as
 * xyz position (in world coordinates), xyz rotation
 * and of course the actual vertice data.
 * 
 * (Actually, since our application mostly uses the same vertice data over and over,
 * it might be a better idea to store the verticies at a single location with a handle
 * in the MutableShape)
 * 
 * The transitionQueue is a simple way of queuing animations for this particular instance.
 * 
 * Yes, all these public variables make the old-school java coder in me a bit sick, but the Android
 * SDK guidelines specifically stress the fact that virtual method calls are very expensive compared to just looking up
 * a public field. This might have changed a bit since JIT was introduced in Android 2.2 though. JIT
 * probably inlines such stuff really well.
 * 
 * @author Erik
 *
 */
public abstract class MutableShape {
	
	public static final int FLOAT_SIZE_BYTES = 4;
	public static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
	public static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	public static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	public static final int TRIANGLE_NORMALS_DATA_SIZE = 3;
	
	public float x = 0.0f;
	public float y = 0.0f;
	public float z = 0.0f;
	
	public float xRot = 0.0f;
	public float yRot = 0.0f;
	public float zRot = 0.0f;
	
	/**
	 * Reference to the OpenGL texture id.
	 */
	public int textureId;
	
	/**
	 * Each contact shall have a unique color index assigned so we can perform color-based picking.
	 */
	public float[] colorIndex = new float[3];
	
	public FloatBuffer verticesBuffer;
	public FloatBuffer normalsBuffer;
	
	private Queue<Transition> transitionQueue = new LinkedList<Transition>();
	public Transition currentTransition = null;
		
	public final void pushTransitionOntoQueue(Transition transition) {
		transitionQueue.offer(transition);
		//Log.i("MutableShape", "Pushed " + transition.name + " onto queue. Queue has " + transitionQueue.size() + " items");
	}
	
	/**
	 * Pushes a transition onto the queue, and then automatically pops the oldest one.
	 * @param transition
	 */
	public final void pushTransitionOntoQueueAndStart(Transition transition) {
		pushTransitionOntoQueue(transition);
		popTransition();
	}
	
	public final void clearTransitionQueue() {
		transitionQueue.clear();
	}
	
	public final void popTransition() {
		currentTransition = transitionQueue.poll();
		if(currentTransition != null) {
			currentTransition.reset();
		} else {
			Log.e("MutableShape", "Tried to pop transition, but failed. No transition on queue.");
		}
		//Log.i("MutableShape", "Popped " + currentTransition.name + " from queue. Queue has " + transitionQueue.size() + " items");
	}
	
	/**
	 * Pops the next transition from the queue, making it active, but also places it in the last position of the 
	 * queue. Useful for repeating in/out transitions.
	 */
	public final void popAndRequeTransition() {
		popTransition();
		transitionQueue.offer(currentTransition);
		//Log.i("MutableShape", "Requeud " + currentTransition.name + " onto queue. Queue has " + transitionQueue.size() + " items");
	}
	
	public final void applyTransition() {
		if(currentTransition != null && !currentTransition.isComplete) {
			currentTransition.applyFrame(this);			
		}		
	}
}
