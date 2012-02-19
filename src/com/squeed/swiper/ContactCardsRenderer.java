package com.squeed.swiper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.squeed.swiper.composites.RadialMenu;
import com.squeed.swiper.fw.FrameBufferFactory;
import com.squeed.swiper.fw.ObjectRenderer;
import com.squeed.swiper.fw.TextRenderer;
import com.squeed.swiper.fw.Transition;
import com.squeed.swiper.helper.MatrixLogger;
import com.squeed.swiper.helper.TextureLoader;
import com.squeed.swiper.shader.Shaders;
import com.squeed.swiper.shapes.BgQuad;
import com.squeed.swiper.shapes.ContactCard;
import com.squeed.swiper.util.MathHelper;

/**
 * Renders the scene, this is the "main" loop of the 3D-rendering. Note the heavy usage of statics,
 * due to performance constraints on devices every small bit can help.
 * 
 * @author Erik
 */
public class ContactCardsRenderer implements GLSurfaceView.Renderer{
		
		private static String TAG = "ContactCardsRendererGLES20";
		private static final float SWIPE_VELOCITY_DECREASE = 0.008f;
		private static final float BOUNCE_VELOCITY_DECREASE = 0.045f;
		
		private static ContactCard[] contactCards;		
		public static int[] textureIDs;

		public static int currentTextureIndex = 0;

		private ObjectRenderer renderer;
		private Context mContext;
		//private Projector mProjector;
		private static float currentVelocity = 0.0f;
		private static float direction = 0.0f;
		private static float xTranslate = 0.0f;

		private static int selectedIndex = -1;
		
		private static float velocityDecrease = SWIPE_VELOCITY_DECREASE;

		
		private static final AccelerateDecelerateInterpolator acdcIntp = new AccelerateDecelerateInterpolator();
		private static final AnticipateOvershootInterpolator intp = new AnticipateOvershootInterpolator(1.2f);
		private static final OvershootInterpolator bounceIntp = new OvershootInterpolator();
		
		public static boolean inSelectionMode = false;
		private static boolean isBouncing = false;		
		private static boolean hasBeenPaused = false;
		
		private static RadialMenu radialMenu;
		private static BgQuad bgQuad;
		
		private boolean isZoomToFront = false;
		private boolean isZoomToBack = false;
		private boolean renderPulse = false;
		private boolean renderSolid = false;

		public static int[] viewport = null;
	
		/******************* GL ES 2 inits ****************/
		

		/** The ModelView Projection matrix */
	    public static  float[] mMVPMatrix = 	new float[16];
	    /** The Projection Matrix */
	    public static  float[] mProjMatrix = 	new float[16];
	    /** The Model matrix */
	    public static  float[] mMMatrix = 		new float[16];
	    /** The View matrix **/	    
	    public static  float[] mVMatrix = 		new float[16];

	    /** Window coordinates */
	    private static float[] winCoords;
	    
	    
	    public static int time = 0;
		public static float amount = 3.0f;
		/**
		 * Constructs the renderer, the contactcards to render are supplied....
		 * 
		 * @param mContext
		 * @param contacts
		 * @param touchSurfaceView 
		 */
		public ContactCardsRenderer(Activity mContext, ContactCard[] contacts, TouchSurfaceView touchSurfaceView) {			
			this.mContext = mContext;
			renderer = new ObjectRenderer();
			contactCards = contacts;
			
			// Make space for swipe-mode and selected-mode version of each card.
			// Space for 1000 texture id's...
			ContactCardsRenderer.textureIDs = new int[1000];
			
			//mProjector = new Projector();
		}


		/* (non-Javadoc)
		 * @see nu.epsilon.swipebook.IContactCardRenderer#refreshContacts(nu.epsilon.swipebook.shapes.ContactCard[])
		 */
		public void refreshContacts(ContactCard[] contacts) {
			for(int a = 0; a < contactCards.length; a++) {
				if(contactCards[a] != null) {
					contactCards[a].name = contacts[a].name;
					contactCards[a].picture = contacts[a].picture;
				}
			}
			if(hasBeenPaused) {
				isZoomToBack = true;
				contactCards[selectedIndex].popTransition();
			}
		}



		public void handleActionMove(float newVelocity, float direction) {
			if (!isBouncing && !inSelectionMode) {
				ContactCardsRenderer.currentVelocity = newVelocity;
				ContactCardsRenderer.direction = direction;
			}
		}


		public void handleActionDown(float reduceVelocityByFactor) {
			ContactCardsRenderer.currentVelocity /= reduceVelocityByFactor;
		}

		 
		 // FPS counter related.
		 private int frames = 0;
		 private long startTime = 0L;

		 
		public void onDrawFrame(GL10 gl) {
			if(time > Integer.MAX_VALUE - 20) {
				time = 0;				
			}
			time+=15;
			
			if(frames == 0) {
				startTime = System.currentTimeMillis();
			}

			// reset the coordinate system.
			Matrix.setIdentityM(mMMatrix, 0);
			
			/*
			 * Usually, the first thing one might want to do is to clear the
			 * screen. The most efficient way of doing this is to use glClear().
			 */
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		    
		    
		    if(isRotating) {
		    	currentRotation += 1.0f;
		    	if(currentRotation >= 360.0f) {
		    		isRotating = false;
		    		currentRotation = 0.0f;
		    	}
		    	Matrix.setLookAtM(mVMatrix, 0, (float) Math.sin(Math.toRadians(currentRotation))*6.0f, 0.0f, 6.0f, (float) Math.sin(Math.toRadians(currentRotation))*6.0f, 0f, 0f, 0f, 1.0f, 0.0f);
		    }

			// background draw disabled by default, kills fps on emulator.
		    if(renderBg) {		    	
		    	renderBackground();
		    }
			
			renderSwipe();
			if (inSelectionMode) {
				GLES20.glUseProgram(Shaders.defaultShader.program);
				//checkGlError("glUseProgram");
				renderCardTransition();
			}
	
			frames++;
			if(frames == 150) {
				float fps = 150.0f / ((System.currentTimeMillis() - startTime)/1000.0f);
				
				
				Log.i("FPS", "150 frames took : " + (System.currentTimeMillis() - startTime) + " ms to render, which means " + fps + " fps");
				frames = 0;
			}

			// Test render some text...
			textRenderer.render(0.0f, 4.5f, 0.5f, "Swiper Phonebook", Shaders.defaultShader);
		}

		TextRenderer textRenderer = new TextRenderer();
		
		private void renderBackground() {
			GLES20.glUseProgram(Shaders.defaultShader.program);
			bgQuad.draw();
		}
		
		
		
		
		
		
		private void renderCardTransition() {
			if(selectedIndex == -1) 
				return;
			
			ContactCard cc = contactCards[selectedIndex];		
			
//			if(cc.selectedTextureId == -1 && cc.detailBitmap != null) {
//				createTextureOnTheFly(gl, selectedIndex, cc);
//			}
			
			setCurrentTexture(selectedIndex, true);
			
			if(isZoomToFront) {
				cc.applyTransition();
				
				if(cc.currentTransition != null && cc.currentTransition.isComplete ) {
					float[]lastXyz = cc.currentTransition.fromXYZ;
					float lastStartYPos = cc.currentTransition.fromYRot;
					cc.pushTransitionOntoQueue(new Transition(new float[]{cc.x, cc.y, cc.z}, lastXyz, 300, cc.yRot, lastStartYPos));
					isZoomToFront = false;					
				}
			}
			if(isZoomToBack) {
				cc.applyTransition();
				
				if(cc.currentTransition != null && cc.currentTransition.isComplete) {
					cc.currentTransition = null;
					isZoomToBack = false;					
					cc.isSelected = false;
					selectedIndex = -1;
					inSelectionMode = false;
					
					radialMenu.isVisible = false;
				}				
			}
			
			renderer.render(cc, Shaders.defaultShader);
			if(renderReflection) {
				GLES20.glUseProgram(Shaders.reflectionShader.program);
				renderer.render(cc, Shaders.reflectionShader);
			}
			
			if(radialMenu.isVisible) {
				GLES20.glUseProgram(Shaders.defaultShader.program);
				radialMenu.draw();
			}
		}
		

		private void updateVelocity() {
			if(inSelectionMode) {
				currentVelocity = 0.0f;
				return;
			}
			if (currentVelocity < 0.01f) {
				if (currentVelocity != 0.0f)
					currentVelocity = 0.0f;
			} else {
				currentVelocity -= velocityDecrease;
			}			
		}
		

		private void calcXTranslation() {
			if(inSelectionMode) {
				xTranslate = 0.0f;
				return;
			}
			xTranslate = (direction / 1000.0f)
					* intp.getInterpolation(ContactCardsRenderer.currentVelocity);
			if (!isBouncing) {
				if (contactCards[contactCards.length - 1].x - xTranslate < 0.0f
						|| contactCards[0].x - xTranslate > 0.0f) {
					isBouncing = true;
					velocityDecrease = BOUNCE_VELOCITY_DECREASE;
					currentVelocity = 0.5f;
				}
			}
			
			// Note that the code above may set us in bounce mode, thus no else if..
			if (isBouncing) {
				xTranslate = (-direction / 2000.0f)
						* bounceIntp
								.getInterpolation(currentVelocity);
				if (currentVelocity < 0.01f) {
					currentVelocity = 0.0f;
					isBouncing = false;
					velocityDecrease = SWIPE_VELOCITY_DECREASE;
				}				
			}			
		}


		
		
		private void renderSwipe() {
			// each frame, decrease the velocity
			updateVelocity();

			// Get the base "scroll" position. Note the interpolation.
			calcXTranslation();
			
					
			for (int i = 0; i < contactCards.length; i++) {
				if( contactCards[i] == null) {
					//Log.i("TAG", "contact card of index " + i + " is null????!?!?");
					continue;
				}
				ContactCard cc = contactCards[i];
				
				if (cc.isSelected)
					continue;

				cc.x -= xTranslate;				
				cc.z = Z_DEPTH - (Math.abs(cc.x) / 2);
				cc.yRot = cc.x * 9.0f;
				
				//Log.i("TAG", "Z_DEPTH: " + Z_DEPTH);

				
				// Get corresponding 2D x/y coords
				winCoords = get2DCoordsFrom3D(cc.x, cc.y, cc.z);
				
				// The most crude occlusion culling ever...
				if(cc.x < -5 || cc.x > 5) {					
					continue;
				}
					
				if(renderSolid) {
					
					renderer.renderSolidColor(cc, Shaders.colorShader,  Shaders.colorShader.colorHandle, cc.colorIndex);
					//Log.i(TAG, "Rendered solid pass!");
					continue;
				}
				
				if(!renderPulse) {					
					renderer.render(cc, Shaders.defaultShader);					
				} else {
					renderer.render(cc, Shaders.pulseShader, Shaders.pulseShader.timeHandle, time);
				}
					
				if(renderReflection) {
					if(!renderPulse) {
						renderer.renderReflection(cc, Shaders.reflectionShader, Shaders.reflectionShader.amount, amount, 2.1f, -1.0f);
					} else {						
						renderer.renderReflection(cc, Shaders.pulseReflectionShader, new int[]{Shaders.pulseReflectionShader.time, Shaders.pulseReflectionShader.amount}, new float[]{time, amount}, 2.1f, -1.0f);
					}
				}
			}
			
		}

	
		private void setCurrentTexture(int i, boolean selected) {
			if(i >= contactCards.length || i < 0) {
				return;				
			}
			
			GLES20.glActiveTexture(GL10.GL_TEXTURE0);
			GLES20.glBindTexture(GL10.GL_TEXTURE_2D, contactCards[i].textureId);
		}

		private static int X_SIZE, Y_SIZE;
		private static float[] obj = new float[3];
		
		private int[] viewport3 = null;
		private float fNear = 1.0f;
		private float fFar = 20.0f;
		
		private float[] get2DCoordsFrom3D(float x, float y, float z) {					
			
			GLU.gluProject(x, y, z, mMMatrix, 0,
					mProjMatrix, 0, viewport3, 0, obj, 0);

			return obj;
		}
		
		private static float[] result = new float[4];
		private static float[] result3 = new float[4];
		
		private float[] get3DCoordsFrom2D(float x, float y, float z) {
			Log.i(TAG, "3D W:" + x + " H: " + y);
			
			MathHelper.gluUnProject(x, y, z, mMMatrix, 0, mProjMatrix, 0, viewport3, 0, result, 0);
			Log.i(TAG, "3D " + MatrixLogger.vector4ToString(result));
			return result;			
		}
		
		

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.i("onSurfaceChanged","width:" + width + ",height:" + height);
			setViewPort(width, height);
			X_SIZE = width;
		    Y_SIZE = height;
		    mTargetTexture = FrameBufferFactory.createTargetTexture(gl, X_SIZE/8, Y_SIZE/8);
			mFramebuffer = FrameBufferFactory.createFrameBuffer(gl, X_SIZE/8, Y_SIZE/8, mTargetTexture);
		}
		
		private void setViewPort(int width, int height) {

			viewport3 = new int[]{0, 0, width, height}; 
			GLES20.glViewport(0, 0, width, height);
		    float ratio = (float) width / height;
		    Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, fNear, fFar);
		    //Log.i(TAG, MatrixLogger.matrix44ToString(mProjMatrix));		    
		}
		
		
		

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			
			
			currentTextureIndex = 0;
			/*
			 * By default, OpenGL enables features that improve quality but
			 * reduce performance. One might want to tweak that especially on
			 * software renderer.
			 */
			//GLES20.glDisable(GL10.GL_DITHER);
			
			Shaders.initDefaultShader();
			Shaders.initColorShader();
			Shaders.initReflectionShader();
			Shaders.initPulseShader();
			Shaders.initPulseReflectionShader();
			
			// Only setup textures on load.			
			setupTextures(gl);			
			
			radialMenu = new RadialMenu(mContext);
			
			/*
			 * Some one-time OpenGL initialization can be made here probably
			 * based on features of this particular context
			 */
			GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

			//GLES20.glClearColor(1, 1, 1, 1);
			GLES20.glCullFace(GL10.GL_BACK);			
			GLES20.glEnable(GL10.GL_DEPTH_TEST);
			GLES20.glEnable(GL10.GL_BLEND);
			GLES20.glEnable(GL10.GL_TEXTURE_2D);
			
			// Setup camera
			Matrix.setLookAtM(mVMatrix, 0, 0, 0, 6.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		}



		private void setupTextures(GL10 gl) {
			
			bgQuad = TextureLoader.loadBackgroundTexture(gl, mContext);
			
			GLES20.glGenTextures(contactCards.length, textureIDs, 0);
			for (int a = 0; a < contactCards.length; a++) {
				currentTextureIndex = TextureLoader.setupCardTexture(gl, mContext, contactCards[a], textureIDs, currentTextureIndex);
			}
		}


		
		
		

		public static int mTargetTexture;
		private int mFramebuffer;
        private int mFramebufferWidth = 480;
        private int mFramebufferHeight = 800;
		
	/* (non-Javadoc)
	 * @see nu.epsilon.swipebook.IContactCardRenderer#testSelect(int, int)
	 */
		
	static ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).order(ByteOrder.nativeOrder());
		
	public int testSelect(int winX, int winY) {

		// THIS WORKS!!!
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);

	    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		renderSolid = true;
		setViewPort(X_SIZE/8, Y_SIZE/8);
		renderSwipe();
		
		renderSolid = false;
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).order(ByteOrder.nativeOrder());
	    GLES20.glReadPixels(winX/8, Y_SIZE/8-winY/8, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
	    setViewPort(X_SIZE, Y_SIZE);
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	    
	    
	    Log.i(TAG, "Data:" + pixelBuffer.get(0) + "," + pixelBuffer.get(1) + "," + pixelBuffer.get(2) +"," + pixelBuffer.get(3));
	    int index = (int) pixelBuffer.get(0);
	    if(index > 0) {
	    	selectedIndex = index - 1;
	    	Log.i(TAG, "Selected contactCards: " + contactCards[selectedIndex].name);
	    } else {
	    	return -1;
	    }
	    
	    
	    
		// If showing radialmenu, test selection for those first.
		
		if(radialMenu.isVisible) {
			if(radialMenu.inTransition()) {
				return -1;
			}
			 int selectedIconIndex = -1; //radialMenu.testSelect(winX, winY);
			 if(selectedIconIndex > -1) {
				 if(selectedIconIndex == 0 || selectedIconIndex == 1) {
					 radialMenu.execute(selectedIconIndex, contactCards[selectedIndex].id);
				 }				
				 return 1;
			 }
		 }
		
		
		if(contactCards[selectedIndex].isSelected) {					
			isZoomToBack = true;		
			contactCards[selectedIndex].popTransition();
			radialMenu.toggleInOut();
		} else {
			
			inSelectionMode = true;
	    	contactCards[selectedIndex].isSelected = true;
			isZoomToFront = true;
			contactCards[selectedIndex].pushTransitionOntoQueueAndStart(
					new Transition(
							new float[]{contactCards[selectedIndex].x, contactCards[selectedIndex].y, contactCards[selectedIndex].z}, 
							new float[]{0.0f, 0.0f, Z_DEPTH}, 
							300, acdcIntp, contactCards[selectedIndex].yRot, 0.0f)
			);
			
			radialMenu.setStartStop(contactCards[selectedIndex].x, contactCards[selectedIndex].y, contactCards[selectedIndex].z);
			radialMenu.toggleInOut();
			radialMenu.isVisible = true;
			
		}
			
		
		return selectedIndex;
	}

    
	


	private boolean renderReflection = false;
	private boolean renderBg = false;
	private boolean isRotating = false;
	private float currentRotation = 0.0f;


	public void testSelectLongClick(int winX, int winY) {
		
	}


	private void spinToFront(int i, ContactCard cc) {
		cc.isSelected = true;
		selectedIndex = i;
		inSelectionMode = true;					
		isZoomToFront = true;
		cc.clearTransitionQueue();
		cc.pushTransitionOntoQueueAndStart(
				new Transition(
						new float[]{cc.x,cc.y,cc.z}, 
						new float[]{0.0f, 0.0f, -2.0f}, 
						300, acdcIntp, cc.yRot, 180.0f
				)
		);
	}
	
	
	


	public static float Z_DEPTH = 3.0f;

	/**
	 * The pinch-zoom actually only moves the z-index a bit back or forth.
	 * @param f
	 */
	public void pinchZoom(float f) {
		Log.i("CCR", "ENTER - pinchZoom with value " + f);
		if(Z_DEPTH + (f/150.0f) > 1.0f && Z_DEPTH + (f/150.0f) < 5.0) {
			Z_DEPTH += (f/150.0f);
		}
	}

	/**
	 * Toggle state flags depending on menu input.
	 * @param identifier
	 */
	public void toggle(int identifier) {
		switch(identifier) {
		case SwipeActivity.REFLECTION:
			renderReflection = !renderReflection;
			break;
		case SwipeActivity.BACKGROUND:
			renderBg = !renderBg;
			break;
		case SwipeActivity.PULSE:
			renderPulse = !renderPulse;
			break;
		case SwipeActivity.INC_REFL:
			amount-=0.4f;
			break;
		case SwipeActivity.DEC_REFL:
			amount+=0.4f;
			break;
		case SwipeActivity.ROTATE:
			isRotating = true;
			break;
		case SwipeActivity.SOLID:
			renderSolid = !renderSolid;
			break;
		}
	}
	
	private static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
