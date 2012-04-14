package com.squeed.swiper.fw;

import com.squeed.swiper.helper.MatrixLogger;
import com.squeed.swiper.shapes.BaseMesh;

import android.view.animation.Interpolator;

/**
 * Defines an object transition / animation from point xyz1 to point xyz2, 
 * where the transition has a set duration in milliseconds.
 * 
 * TODO Maybe implement support for non-duration velocity based movement.
 * 
 * Also, implement xyz rotation.
 * 
 * @author Erik
 *
 */
public class Transition {
	
	public final float[] fromXYZ;
	public final float[] toXYZ;
	private float[] directionVector = new float[3];
	private float vLength;
	
	
	private long durationInMs;
	public long timeOfStart;
	
	public boolean isComplete = false;
	
	public boolean isInterruptable = false;
	
	private Interpolator intp;
	public float fromYRot;
	private float toYRot;
	
	
	/**
	 * Constructs the most basic Transition: From a to b, linear movement, set duration. 
	 * 
	 * @param fromXYZ
	 * @param toXYZ
	 * @param durationInMs
	 */
	public Transition(float[] fromXYZ, float[] toXYZ, int durationInMs) {
		this.fromXYZ = new float[]{fromXYZ[0], fromXYZ[1], fromXYZ[2]};
		this.toXYZ = new float[]{toXYZ[0], toXYZ[1], toXYZ[2]};
		this.durationInMs = durationInMs;		
		init();
	}
	
	// TODO change from / to arrays for better memory reuse.
	public Transition(float[] fromXYZ, float[] toXYZ, int durationInMs, float fromYRot, float toYRot) {
		this.fromXYZ = new float[]{fromXYZ[0], fromXYZ[1], fromXYZ[2]};
		this.toXYZ = new float[]{toXYZ[0], toXYZ[1], toXYZ[2]};
		this.durationInMs = durationInMs;		
		this.fromYRot = fromYRot;
		this.toYRot = toYRot;
		init();
	}
	
	/**
	 * Constructs the most basic Transition: From a to b, linear movement, set duration. But adds
	 * an animation interpolator. {@link Interpolator}
	 * 
	 * @param fromXYZ
	 * @param toXYZ
	 * @param durationInMs
	 */
	public Transition(float[] fromXYZ, float[] toXYZ, int durationInMs, Interpolator intp) {
		this(fromXYZ, toXYZ, durationInMs);
		this.intp = intp;
	}
	
	// TODO copy primitives into existing from/to float arrays.
	public Transition(float[] fromXYZ, float[] toXYZ, int durationInMs, Interpolator intp, float fromYRot, float toYRot) {
		this(fromXYZ, toXYZ, durationInMs);
		this.intp = intp;
		this.fromYRot = fromYRot;
		this.toYRot = toYRot;
	}
	
	private void init() {	
		timeOfStart = 0;
		calcDirectionVector();
		calcVLength();
	}

	private void calcDirectionVector() {
		directionVector[0] = toXYZ[0]-fromXYZ[0];
		directionVector[1] = toXYZ[1]-fromXYZ[1];
		directionVector[2] = toXYZ[2]-fromXYZ[2];
		//Log.i("Transition", "Direction vector: " + MatrixLogger.vector3ToString(directionVector));
	}
	
	private void calcVLength() {
		this.vLength = (float) Math.sqrt(
						(toXYZ[0]-fromXYZ[0])* (toXYZ[0]-fromXYZ[0]) + 
						(toXYZ[1]-fromXYZ[1])* (toXYZ[1]-fromXYZ[1]) +
						(toXYZ[2]-fromXYZ[2])* (toXYZ[2]-fromXYZ[2])
		);
	
	}
	
	private float factor;
	
	/**
	 * returns true if this transition has completed.
	 * 
	 * @param currentXyz
	 * @return
	 */
	public boolean applyFrame(BaseMesh shape) {
//		if(isComplete)
//			return true;
		if(timeOfStart == 0) {
			timeOfStart = System.currentTimeMillis();
		}
		float percentage = (float) (System.currentTimeMillis() - timeOfStart) / (float) durationInMs;

		if(percentage > 1.0f) {
			percentage = 1.0f;
		}
		
		if(intp != null) {
			factor = intp.getInterpolation(percentage);
		} else {
			factor = percentage;
		}
		
		shape.x = fromXYZ[0] + (factor * directionVector[0]);
		shape.y = fromXYZ[1] + (factor * directionVector[1]);
		shape.z = fromXYZ[2] + (factor * directionVector[2]);
		
		// Handle Y-axis rotation
		if(fromYRot != toYRot) {
			shape.yRot = fromYRot + (factor * (toYRot - fromYRot));
		}

		if(percentage >= 1.0f) {
			timeOfStart = 0;
			isComplete = true;
			return true;
		}
		return false;
	}
	
	public void reset() {
		isComplete = false;
		timeOfStart = 0;		
	}
	
	
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("fromXYZ: " + MatrixLogger.vector3ToString(this.fromXYZ) + "\n");
		buf.append("toXYZ: " + MatrixLogger.vector3ToString(this.toXYZ) + "\n");
		buf.append("directionVector: " + MatrixLogger.vector3ToString(this.directionVector) + "\n");
		buf.append("vLength: " + this.vLength + "\n");
		return buf.toString();
	}
}
