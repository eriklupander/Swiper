package com.squeed.swiper;

import static android.opengl.GLES10.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES10.GL_LINEAR;
import static android.opengl.GLES10.GL_NEAREST;
import static android.opengl.GLES10.GL_TEXTURE_2D;
import static android.opengl.GLES10.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES10.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES10.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES10.GL_TEXTURE_WRAP_T;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.squeed.swiper.composites.RadialMenu;
import com.squeed.swiper.fw.FrameBufferFactory;
import com.squeed.swiper.fw.ObjectRenderer;
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
		 
		/* (non-Javadoc)
		 * @see nu.epsilon.swipebook.IContactCardRenderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
		 */
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
				//float fps = 150.0f / ((System.currentTimeMillis() - startTime)/1000.0f);
				//Log.i("FPS", "150 frames took : " + (System.currentTimeMillis() - startTime) + " ms to render, which means " + fps + " fps");
				frames = 0;
			}
			
//			if(trySelectNextFrame) {
//				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
//				GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//			    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//				renderSolid = true;
//				renderSwipe();
//				renderSolid = false;
//				ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).order(ByteOrder.nativeOrder());
//			    GLES20.glReadPixels(gWinX, Y_SIZE-gWinY, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
//			    Log.i(TAG, "Data:" + pixelBuffer.get(0) + "," + pixelBuffer.get(1) + "," + pixelBuffer.get(2) +"," + pixelBuffer.get(3));
//				
//			    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//				trySelectNextFrame = false;
//			}

//			mProjector.getCurrentModelView(gl);
//			mProjector.getCurrentProjection(gl);
		}

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
			winCoords = get2DCoordsFrom3D(cc.x, cc.y, cc.z);
			cc.winX = (int) winCoords[0];
			cc.winY = (int) (Y_SIZE-winCoords[1]);
			
			//GLES20.glUseProgram(Shaders.defaultShader.program);
			//cc.draw(textureIDs[cc.textureIndex]);
			renderer.render(cc, Shaders.defaultShader);
			if(renderReflection) {
				GLES20.glUseProgram(Shaders.reflectionShader.program);
				renderer.render(cc, Shaders.reflectionShader);
				//cc.drawAsReflection(textureIDs[cc.textureId]);
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
				//Log.i("TAG", "Z_DEPTH: " + Z_DEPTH);
				cc.z = Z_DEPTH - (Math.abs(cc.x) / 2);
				cc.yRot = cc.x * 9.0f;
				

				
				// Get corresponding 2D x/y coords
				winCoords = get2DCoordsFrom3D(cc.x, cc.y, cc.z);
				cc.winX = (int) (X_SIZE-winCoords[0]);
				cc.winY = (int) (winCoords[1]); 
				
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
					renderer.render(cc, Shaders.pulseShader, Shaders.pulseShader.time, time);
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
			GLES20.glBindTexture(GL10.GL_TEXTURE_2D,
					textureIDs[contactCards[i].textureId]);
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
			//GLU.gluUnProject(x, y, z, mProjector.mGrabber.mModelView, 0,  mProjector.mGrabber.mProjection, 0, viewport3, 0, result3, 0);
			//return result;
//			result3[0] /= result3[3];
//			result3[1] /= result3[3];
//			result3[2] /= result3[3];
//			//xyzw[3] /= xyzw[3];
//			result[3] = 1;
//			Log.i(TAG, "3D " + MatrixLogger.vector4ToString(result3));
//			return result3;
		}
		
		

		/* (non-Javadoc)
		 * @see nu.epsilon.swipebook.IContactCardRenderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
		 */
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.i("onSurfaceChanged","width:" + width + ",height:" + height);
			viewport3 = new int[]{0, 0, width, height}; 
			GLES20.glViewport(0, 0, width, height);
		    float ratio = (float) width / height;
		    Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, fNear, fFar);
		    Log.i(TAG, MatrixLogger.matrix44ToString(mProjMatrix));
		    X_SIZE = width;
		    Y_SIZE = height;
		}
		
		
		

		/* (non-Javadoc)
		 * @see nu.epsilon.swipebook.IContactCardRenderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
		 */
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			mTargetTexture = FrameBufferFactory.createTargetTexture(gl, mFramebufferWidth, mFramebufferHeight);
			mFramebuffer = FrameBufferFactory.createFrameBuffer(gl, mFramebufferWidth, mFramebufferHeight, mTargetTexture);
			
			currentTextureIndex = 0;
			/*
			 * By default, OpenGL enables features that improve quality but
			 * reduce performance. One might want to tweak that especially on
			 * software renderer.
			 */
			GLES20.glDisable(GL10.GL_DITHER);
			
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
			for (int a = 0; a < contactCards.length; a++)
				setupCardTexture(gl, contactCards[a]);

		}


		
		
		private void setupCardTexture(GL10 gl, ContactCard contactCard) {
			if(contactCard == null)
				return;
			Log.i("CNT", "Creating texture for " + contactCard.name);

			GLES20.glBindTexture(GL_TEXTURE_2D, textureIDs[currentTextureIndex]);

			GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

			GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		
			GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			Bitmap bm = null;
			if (contactCard.picture != null) {
				try {
					bm = contactCard.picture.copy(contactCard.picture.getConfig(),
							true);
					bm = TextureLoader.rescaleBitmapToPOT(bm, 128, 128);
				} catch (Exception e) {
					return;
				}
				Log.i("TextureLoader", "Contact picture config is: " + contactCard.picture.getConfig().toString());
				bm = TextureLoader.drawContactCardBitmap2(bm, contactCard.name, false);
				
				// Note, we use the bitmap from the contactCard one more time when we create
				// the texture for "selected" mode.
				contactCard.picture.recycle();
			} else {
				InputStream is = mContext.getResources().openRawResource(
						R.drawable.ic_launcher_android);
				Bitmap tempBitmap = null;
				try {
					tempBitmap = BitmapFactory.decodeStream(is);
					Log.i("TextureLoader", "Default icon config is: " + tempBitmap.getConfig().toString());
					
					bm = tempBitmap.copy(tempBitmap.getConfig(), true);	
					tempBitmap.recycle();
					TextureLoader.drawContactCardBitmap(bm, contactCard.name, true);
				} finally {
					try {
						is.close();
					} catch (IOException e) {}
				}
			}
			
			GLUtils.texImage2D(GL_TEXTURE_2D, 0, bm, 0);

			bm.recycle();
			contactCard.textureId = textureIDs[currentTextureIndex];
			currentTextureIndex++;
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
		renderSwipe();
		renderSolid = false;
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).order(ByteOrder.nativeOrder());
	    GLES20.glReadPixels(winX, Y_SIZE-winY, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
	    Log.i(TAG, "Data:" + pixelBuffer.get(0) + "," + pixelBuffer.get(1) + "," + pixelBuffer.get(2) +"," + pixelBuffer.get(3));
		
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	    // END WORKING BLOCK
	    
		//float[] viewRay = MathHelper.getViewRay(winX, winY, X_SIZE, Y_SIZE, new float[]{0.0f, 0.0f, 6.0f}, mVMatrix, mProjMatrix);
		
		// New code. render a full frame to the back buffer. Do color checking.
		 //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
//		 renderSolid = true;
//		 GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//		 GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//		 //renderSwipe();
//		 try {
//			Thread.sleep(500L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		 renderSolid = false;
		 
//		byte b[] = new byte[4];
//		ByteBuffer pixels = ByteBuffer.allocateDirect(4);
//		pixels.order(ByteOrder.nativeOrder());
// 
//		 GLES20.glReadPixels(winX, Y_SIZE - winY, 1, 1, GLES20.GL_RGBA,
//				 GLES20.GL_UNSIGNED_BYTE, pixels);
//		 Log.i(TAG, "Data::: " + pixels.get(0)); // + " " + fb.getFloat(2) + " " + fb.getFloat(3));
		 //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		return -1;
//		float fDistance = 6.0f;
//		float winZ = (1.0f/fNear-1.0f/fDistance)/(1.0f/fNear-1.0f/fFar);
//		float[] xyz = get3DCoordsFrom2D(winX, winY, -0.99f);
//		//float[] xyz2 = get3DCoordsFrom2D(winX, winY, 20);
//		Log.i(TAG, "3D coordinate: " + xyz[0] + "," + xyz[1] + "," + xyz[2]);
//		//Log.i(TAG, "3D/2 coordinate: " + xyz2[0] + "," + xyz2[1] + "," + xyz2[2]);
//		ArrayList<Intersection> intersects = new ArrayList<Intersection>();
//		
//		// If showing radialmenu, test selection for those first.
//		
//		if(radialMenu.isVisible) {
//			if(radialMenu.inTransition()) {
//				return -1;
//			}
//			 int selectedIconIndex = radialMenu.testSelect(winX, winY);
//			 if(selectedIconIndex > -1) {
//				 if(selectedIconIndex == 0 || selectedIconIndex == 1) {
//					 radialMenu.execute(selectedIconIndex, contactCards[selectedIndex].id);
//				 }				
//				 return 1;
//			 }
//		 }
//		
//		// If we're currently in a transition, break selection.
//		
//		
//		for (int i = 0; i < contactCards.length; i++) {
//			
//			
//			
//			ContactCard cc = contactCards[i];
//			if(cc.name.startsWith("Alexander B")) {
//				Log.i(TAG, "3D " + cc.x + "," + cc.y + "," + cc.z);
//			}
////			Line.intersectPlane(intersects, new Float3(xyz), new Float3(xyz2), new Float3(cc.x-1.0f, cc.y-1.0f, cc.z), new Float3(cc.x+1.0f, cc.y-1.0f, cc.z), new Float3(cc.x+1.0f, cc.y+1.0f, cc.z));
////			Log.i(TAG, "Intersects: " + intersects.size());
//			if(cc == null) {
//				continue;
//			}
//			if(cc.winX > X_SIZE || cc.winX < 0 || cc.currentTransition != null && cc.currentTransition.timeOfStart != 0)
//				continue;
//			if (checkSelection(winX, winY, cc)) {
//				Log.i("Selection", "Selected: " + cc.name + " at winX: " + cc.winX + ", winY: "+ cc.winY);
//				
//				// if already selected...unselect..
//				if(cc.isSelected) {					
//					isZoomToBack = true;		
//					cc.popTransition();
//					radialMenu.toggleInOut();
//				} else {
//					if(selectedIndex != -1) {
//						if(i != selectedIndex)
//							continue;
//					}
//					cc.isSelected = true;
//					selectedIndex = i;
//					inSelectionMode = true;
//					
//					isZoomToFront = true;
//					cc.pushTransitionOntoQueueAndStart(
//							new Transition(
//									new float[]{cc.x,cc.y,cc.z}, 
//									new float[]{0.0f, 0.0f, Z_DEPTH}, 
//									300, acdcIntp, cc.yRot, 0.0f)
//					);
//					
//					radialMenu.setStartStop(cc.x, cc.y, cc.z);
//					radialMenu.toggleInOut();
//					radialMenu.isVisible = true;
//					
//				}
//			} else {
//				Log.i("Selection", "Not selected: " + cc.name + " at winX: " + cc.winX + ", winY: "+ cc.winY);
//			}
//		}
//		return -1;
	}

	
	
	
	/* (non-Javadoc)
	 * @see nu.epsilon.swipebook.IContactCardRenderer#setHighlightIcon(int, int)
	 */
	public void setHighlightIcon(int winX, int winY) {
		if(radialMenu.isVisible) {
			
			radialMenu.resetHighlightIcon();
			
			int selectedIconIndex = radialMenu.testSelect(winX, winY);
			if(selectedIconIndex != -1) {
				radialMenu.setHighlightIcon(selectedIconIndex, true);
			}
		}
	}
	
	

    
	private boolean renderReflection = false;
	private boolean renderBg = false;
	private boolean isRotating = false;
	private float currentRotation = 0.0f;



	/* (non-Javadoc)
	 * @see nu.epsilon.swipebook.IContactCardRenderer#testSelectLongClick(int, int)
	 */
	public void testSelectLongClick(int winX, int winY) {
		for (int i = 0; i < contactCards.length; i++) {
			ContactCard cc = contactCards[i];
			if(cc.winX > X_SIZE || cc.winX < 0 || cc.currentTransition != null && cc.currentTransition.timeOfStart != 0)
				continue;
			if (checkSelection(winX, winY, cc)) {
				
				// UNSELECT
				if(cc.isSelected) {					
					isZoomToBack = true;					
				} 
				
				// SELECT
				else {
					if(selectedIndex != -1) {
						if(i != selectedIndex)
							continue;
					}					
					spinToFront(i, cc);
					break;
				}
			}
		}			
	}


	private boolean checkSelection(int winX, int winY, ContactCard cc) {
		
		// TODO, calculate window coordinates for all 4 corners and use those for selection.
		// or at least calculate the bounding box (default:40) size dynamically.
		// 1. Get the depth (z diff) from the camera to the card: (6.0 z is the depth of our static camera)
//		float zDiff = 6.0f - cc.z;
//		float angle = (float) Math.sin(zDiff/1.0f );
//		float diff = zDiff * angle;
//		Log.i("checkSelection", "z diff: " + zDiff + " angle: " + angle + " diff:" + diff);
//		float diff = -1.0f/-cc.z;
//		Log.i("checkSelection", "z diff: " + diff + " cc.z is: " + cc.z);
//		float[] result = new float[3];
//		
//		// This SHOULD give us the upper-left corner in window coordinates... 
//		GLU.gluProject(cc.x+diff, cc.y, cc.z, mMMatrix, 0,
//				mProjMatrix, 0, viewport3, 0, result, 0);
//		
//		Log.i("checkSelectionX","Result X: " + result[0] + " Y: " + result[1] + " Z: " + result[2]);
		
		return winX > cc.winX - 40 && winX < cc.winX + 40
				&& winY > cc.winY - 40 && winY < cc.winY + 40;
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
		
		// 2. Create a bitmap using Canvas and Paint.
//		CreateDetailBitmap t = new CreateDetailBitmap(cc);
//		t.start();
	}
	
	
	
//	class CreateDetailBitmap extends Thread {
//		
//		private ContactCard cc;	
//		
//		CreateDetailBitmap(ContactCard cc) {
//			this.cc = cc;	
//		}
//		
//		public void run() {
//			// 1. Load contact details, render them to a bitmap, set texture id. on cc somehow.
//			String[] personDetails = contactLoader.loadPersonDetails(cc.id);			
//			
//			// 2. Create a bitmap using Canvas and Paint.
//			cc.detailBitmap = TextureLoader.createPersonDetailTexture(personDetails);
//		}
//	}

	public static float Z_DEPTH = 3.0f;

	/* (non-Javadoc)
	 * @see nu.epsilon.swipebook.IContactCardRenderer#pinchZoom(float)
	 */
	public void pinchZoom(float f) {
		Log.i("CCR", "ENTER - pinchZoom with value " + f);
		if(Z_DEPTH + (f/150.0f) > 1.0f && Z_DEPTH + (f/150.0f) < 5.0) {
			Z_DEPTH += (f/150.0f);
		}
	}


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
