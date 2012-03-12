package com.squeed.swiper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.squeed.swiper.composites.RadialMenu;
import com.squeed.swiper.fw.FrameBufferFactory;
import com.squeed.swiper.fw.NumberRenderer;
import com.squeed.swiper.fw.ObjectRenderer;
import com.squeed.swiper.fw.TextRenderer;
import com.squeed.swiper.fw.Transition;
import com.squeed.swiper.helper.TextureLoader;
import com.squeed.swiper.shader.Shaders;
import com.squeed.swiper.shapes.BgQuad;
import com.squeed.swiper.shapes.Buffers;
import com.squeed.swiper.shapes.ContactCard;
import com.squeed.swiper.shapes.IconQuad;
import com.squeed.swiper.shapes.NumberQuad;

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
		public static Context mContext;
		//private Projector mProjector;
		private static float currentVelocity = 0.0f;
		private static float direction = 0.0f;
		private static float xTranslate = 0.0f;

		private static int selectedIndex = -1;
		
		private static float velocityDecrease = SWIPE_VELOCITY_DECREASE;

		
		private static final AccelerateDecelerateInterpolator acdcIntp = new AccelerateDecelerateInterpolator();
		private static final AnticipateOvershootInterpolator intp = new AnticipateOvershootInterpolator(1.2f);
		private static final OvershootInterpolator bounceIntp = new OvershootInterpolator();
		
		private static RadialMenu radialMenu;
		private static BgQuad bgQuad;
		private static NumberQuad numberQuad;
		
		private float[] mLightColor = new float[]{1.0f, 0.8f, 0.8f};

		// Clipping planes
		private float fNear = 1.0f;
		private float fFar = 20.0f;
	
		/******************* GL ES 2 inits ****************/
		// Borrowed comments for each matrix from learnopengles.com tutorials!

		/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
		public static float[] mModelViewProjectionMatrix = new float[16];
		
		/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	    public static  float[] mProjectionMatrix = new float[16];
	    
	    /**
		* Store the model matrix. This matrix is used to move models from object space (where each model can be thought
		* of being located at the center of the universe) to world space.
		*/
	    public static  float[] mModelMatrix = new float[16];
	    
	    /**
		* Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
		* it positions things relative to our eye.
		*/	    
	    public static  float[] mViewMatrix = new float[16];

	    /**
	    * Stores a copy of the model matrix specifically for the light position.
	    */
	    public static final float[] mLightModelMatrix = new float[16]; 
	    
	    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	    * we multiply this by our transformation matrices. */
	    public static final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

	    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
	    public static final float[] mLightPosInWorldSpace = new float[4];
	    
	    /**
	     * A single vertex position to use for the light position.
	     */
	    public static final float[] mLightPosInEyeSpace = new float[4];
	   
	    public static int time = 0;
		public static float amount = 3.0f;
		
		// FPS counter related.
		private int frames = 0;
		private long startTime = 0L;
		private float fps;

		/** Renderer for text/numbers */
		private TextRenderer textRenderer = new TextRenderer();
		private NumberRenderer numberRenderer = new NumberRenderer();
		
		/**
		 * Constructs the renderer, the contactcards to render are supplied.
		 * 
		 * @param mContext
		 * @param contacts
		 * @param touchSurfaceView 
		 */
		public ContactCardsRenderer(ContactCard[] contacts, TouchSurfaceView touchSurfaceView) {			
			mContext = touchSurfaceView.getContext();
			renderer = new ObjectRenderer();
			contactCards = contacts;
			
			// Space for 1000 texture id's...
			ContactCardsRenderer.textureIDs = new int[1000];			
		}


		public void refreshContacts(ContactCard[] contacts) {
			for(int a = 0; a < contactCards.length; a++) {
				if(contactCards[a] != null) {
					contactCards[a].name = contacts[a].name;
					contactCards[a].picture = contacts[a].picture;
				}
			}
			if(AppState.hasBeenPaused) {
				AppState.isZoomToBack = true;
				contactCards[selectedIndex].popTransition();
			}
			
			//AppState.reset();
			//RenderState.reset();
		}



		public void handleActionMove(float newVelocity, float direction) {
			if (!AppState.isBouncing && !AppState.inSelectionMode) {
				ContactCardsRenderer.currentVelocity = newVelocity;
				ContactCardsRenderer.direction = direction;
			}
		}


		public void handleActionDown(float reduceVelocityByFactor) {
			ContactCardsRenderer.currentVelocity /= reduceVelocityByFactor;
		}
		 
		 
		public void onDrawFrame(GL10 gl) {
			updateFpsCounter();

			clearFrame();
			
			if(RenderState.renderLightPos) {
				renderLight();
			}
		    
			// background draw disabled by default, kills fps on emulator.
		    if(RenderState.renderBg) {		    	
		    	renderBackground();
		    }
			
			renderSwipe();
			if (AppState.inSelectionMode) {
				GLES20.glUseProgram(Shaders.defaultShader.program);
				//checkGlError("glUseProgram");
				renderCardTransition();
			}
			
			
	
			frames++;
			if(frames == 150) {
				fps = 150.0f / ((System.currentTimeMillis() - startTime)/1000.0f);
				
				
				//Log.i("FPS", "150 frames took : " + (System.currentTimeMillis() - startTime) + " ms to render, which means " + fps + " fps");
				frames = 0;
			}
			
			// Test render some text...
			//textRenderer.render(0.0f, 4.5f, 0.5f, "Swiper Phonebook", Shaders.defaultShader);
			//numberRenderer.renderNumber(-2.0f, -4.5f, 0.5f, 0f, 0f, 0f, ("" + time), numberQuad.numbersTexture[0]);
		}


		private void renderLight() {
			GLES20.glUseProgram(Shaders.colorShader.program);
			GLES20.glUniform3fv(Shaders.colorShader.colorHandle, 1, mLightColor, 0);
			renderer.renderSolidColorVBO(mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2], 0,0,0, Shaders.colorShader, 1);
		}


		private void clearFrame() {
			// reset the coordinate system.
			Matrix.setIdentityM(mModelMatrix, 0);
			
			/*
			 * Usually, the first thing one might want to do is to clear the
			 * screen. The most efficient way of doing this is to use glClear().
			 */
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		}


		private void updateFpsCounter() {
			if(time > Integer.MAX_VALUE - 20) {
				time = 0;				
			}
			time+=15;
			
			if(frames == 0) {
				startTime = System.currentTimeMillis();
			}
		}

		
		
		private void renderBackground() {
			GLES20.glUseProgram(Shaders.basicShader.program);
			renderer.renderBasicVBO(0, 0, 0, 0, 0, 0, bgQuad.texture[0], Shaders.basicShader, 1.0f, 2);
		}
		
		
		
		
		private float camZ = 6.0f;
		
		private void renderCardTransition() {
			if(selectedIndex == -1) 
				return;
			
			ContactCard cc = contactCards[selectedIndex];		
			
			if(AppState.isZoomToFront) {
				
				cc.applyTransition();
				camZ += 0.1f;
				Matrix.setLookAtM(mViewMatrix, 0, 0, 0,camZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
				if(cc.currentTransition != null && cc.currentTransition.isComplete ) {
					float[]lastXyz = cc.currentTransition.fromXYZ;
					float lastStartYPos = cc.currentTransition.fromYRot;
					cc.pushTransitionOntoQueue(new Transition(new float[]{cc.x, cc.y, cc.z}, lastXyz, 300, cc.yRot, lastStartYPos));
					AppState.isZoomToFront = false;					
				}
			}
			if(AppState.isZoomToBack) {
				cc.applyTransition();
				camZ -= 0.1f;
				Matrix.setLookAtM(mViewMatrix, 0, 0, 0, camZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
				if(cc.currentTransition != null && cc.currentTransition.isComplete) {
					cc.currentTransition = null;
					AppState.isZoomToBack = false;				
					selectedIndex = -1;
					AppState.inSelectionMode = false;
					
					radialMenu.isVisible = false;
				}				
			}
			
			if(RenderState.renderSolid) {
				renderer.renderSolidColor(cc, Shaders.colorShader, Shaders.colorShader.colorHandle, cc.colorIndex, 0);
				return;
			}
			
			renderer.render(cc, Shaders.defaultShader);
			if(RenderState.renderReflection) {
				GLES20.glUseProgram(Shaders.reflectionShader.program);
				renderer.renderReflection(cc, Shaders.reflectionShader, Shaders.reflectionShader.amount, amount, 2.1f, -1.0f);
			}
			
			
			if(radialMenu.isVisible) {
				if(RenderState.renderSolid) {					
					radialMenu.drawSelection();
					radialMenu.draw();
				} else {
					radialMenu.draw();
				}				
			}
		}
		

		private void updateVelocity() {
			if(AppState.inSelectionMode) {
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
			if(AppState.inSelectionMode) {
				xTranslate = 0.0f;
				return;
			}
			xTranslate = (direction / 1000.0f)
					* intp.getInterpolation(ContactCardsRenderer.currentVelocity);
			if (!AppState.isBouncing) {
				if (contactCards[contactCards.length - 1].x - xTranslate < 0.0f
						|| contactCards[0].x - xTranslate > 0.0f) {
					AppState.isBouncing = true;
					velocityDecrease = BOUNCE_VELOCITY_DECREASE;
					currentVelocity = 0.5f;
				}
			}
			
			// Note that the code above may set us in bounce mode, thus no else if..
			if (AppState.isBouncing) {
				xTranslate = (-direction / 2000.0f)
						* bounceIntp
								.getInterpolation(currentVelocity);
				if (currentVelocity < 0.01f) {
					currentVelocity = 0.0f;
					AppState.isBouncing = false;
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
				
				if (i == selectedIndex)
					continue;

				cc.x -= xTranslate;				
				cc.z = Z_DEPTH - (Math.abs(cc.x) / 2);
				cc.yRot = cc.x * 9.0f;
				
				// The most crude occlusion culling ever...
				if(cc.x < -5 || cc.x > 5) {					
					continue;
				}
				
					
				if(RenderState.renderSolid) {
					
					renderer.renderSolidColor(cc, Shaders.colorShader,  Shaders.colorShader.colorHandle, cc.colorIndex, 0);
					//Log.i(TAG, "Rendered solid pass!");
					continue;
				}
				
				if(!RenderState.renderPulse) {					
					renderer.render(cc, Shaders.defaultShader);
				} else {
					renderer.render(cc, Shaders.pulseShader, Shaders.pulseShader.timeHandle, time);
				}
					
				if(RenderState.renderReflection) {
					if(!RenderState.renderPulse) {
						renderer.renderReflection(cc, Shaders.reflectionShader, Shaders.reflectionShader.amount, amount, 2.1f, -1.0f);
					} else {						
						renderer.renderReflection(cc, Shaders.pulseReflectionShader, new int[]{Shaders.pulseReflectionShader.time, Shaders.pulseReflectionShader.amount}, new float[]{time, amount}, 2.1f, -1.0f);
					}
				}
			}			
		}


		private static int X_SIZE, Y_SIZE;
//		private static float[] obj = new float[3];
//		
		
//		public static int[] numbersTexture = new int[1];
//		
//		
//		private float[] get2DCoordsFrom3D(float x, float y, float z) {					
//			
//			GLU.gluProject(x, y, z, mModelMatrix, 0,
//					mProjectionMatrix, 0, viewport3, 0, obj, 0);
//			return obj;
//		}
//		
//		private static float[] result = new float[4];
//		private static float[] result3 = new float[4];
//		
//		private float[] get3DCoordsFrom2D(float x, float y, float z) {
//			Log.i(TAG, "3D W:" + x + " H: " + y);
//			
//			MathHelper.gluUnProject(x, y, z, mModelMatrix, 0, mProjectionMatrix, 0, viewport3, 0, result, 0);
//			Log.i(TAG, "3D " + MatrixLogger.vector4ToString(result));
//			return result;			
//		}
		
		

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			Log.i("onSurfaceChanged","width:" + width + ",height:" + height);
			setViewPort(width, height);
			X_SIZE = width;
		    Y_SIZE = height;
		    mTargetTexture = FrameBufferFactory.createTargetTexture(gl, X_SIZE/8, Y_SIZE/8);
			mFramebuffer = FrameBufferFactory.createFrameBuffer(gl, X_SIZE/8, Y_SIZE/8, mTargetTexture);
		}
		
		private void setViewPort(int width, int height) {
			GLES20.glViewport(0, 0, width, height);
		    float ratio = (float) width / height;
		    Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, fNear, fFar);	    
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
			Shaders.initBasichader();
			
			// Only setup textures on load.			
			setupTextures(gl);			
			
			
			// Init shapes
			initVertexBufferObjects(gl);
			
			radialMenu = new RadialMenu(mContext, renderer);
			
			/*
			 * Some one-time OpenGL initialization can be made here probably
			 * based on features of this particular context
			 */
			GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

			//GLES20.glClearColor(1, 1, 1, 1);
			GLES20.glCullFace(GL10.GL_BACK);	
			// Use culling to remove back faces.
			//GLES20.glEnable(GL10.GL_CULL_FACE);
			GLES20.glEnable(GL10.GL_DEPTH_TEST);
			GLES20.glEnable(GL10.GL_BLEND);
			GLES20.glEnable(GL10.GL_TEXTURE_2D);
			



			// Setup camera
			Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 6.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		}



		private void initVertexBufferObjects(GL10 gl) {
			Buffers.currentBufferIdx = 0;
			// Generate three buffers
			GLES20.glGenBuffers(3, Buffers.vboBuffer, 0);			 
			
			
			// Bind to the buffer, index 0
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Buffers.vboBuffer[0]);
			 
			// Transfer data from client memory to the buffer.
			// Data for the contact card mesh.
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, ContactCard.verticesBuffer.capacity() * 4,
					ContactCard.verticesBuffer, GLES20.GL_STATIC_DRAW);
						 
			
			Buffers.currentBufferIdx++;
			
			// Bind to the buffer. Future commands will affect this buffer specifically.
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Buffers.vboBuffer[1]);
			 
			// Transfer data from client memory to the buffer.
			// Data for the icon quad mesh.
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, IconQuad.verticesBuffer.capacity() * 4,
					IconQuad.verticesBuffer, GLES20.GL_STATIC_DRAW);
			
			
			Buffers.currentBufferIdx++;
			
			// Bind to the buffer. Future commands will affect this buffer specifically.
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Buffers.vboBuffer[2]);
			 
			// Transfer data from client memory to the buffer.
			// Data for the icon quad mesh.
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BgQuad.verticesBuffer.capacity() * 4,
					BgQuad.verticesBuffer, GLES20.GL_STATIC_DRAW);
			

			// IMPORTANT: Unbind from the buffer when we're done with it.
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		}


		private void setupTextures(GL10 gl) {
			
			bgQuad = 		TextureLoader.loadBackgroundTexture(gl, mContext);
			numberQuad = 	TextureLoader.loadNumbersTexture(gl, mContext);
			
			GLES20.glGenTextures(contactCards.length, textureIDs, 0);
			for (int a = 0; a < contactCards.length; a++) {
				currentTextureIndex = TextureLoader.setupCardTexture(gl, mContext, contactCards[a], textureIDs, currentTextureIndex);
			}
			
		}


		
		
		

		public static int mTargetTexture;
		private int mFramebuffer;
		private float ROTATION_TO_FRONT;

		
	/* (non-Javadoc)
	 * @see nu.epsilon.swipebook.IContactCardRenderer#testSelect(int, int)
	 */
		
	static ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).order(ByteOrder.nativeOrder());
		
	public int testSelect(int winX, int winY) {
		int hitIndex = -1;
		// THIS WORKS!!!
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
	    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
	    RenderState.renderSolid = true;
		
		setViewPort(X_SIZE/8, Y_SIZE/8);
		
		if(AppState.inSelectionMode) {
			renderCardTransition();
			radialMenu.drawSelection();
		} else {
			renderSwipe();
		}
		
		RenderState.renderSolid = false;
	    GLES20.glReadPixels(winX/8, Y_SIZE/8-winY/8, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
	    setViewPort(X_SIZE, Y_SIZE);
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	    
	    
	    Log.i(TAG, "Data:" + pixelBuffer.get(0) + "," + pixelBuffer.get(1) + "," + pixelBuffer.get(2) +"," + pixelBuffer.get(3));
	    int r = 0xFF &(int) pixelBuffer.get(0);
	    int g = 0xFF &(int) pixelBuffer.get(1);
	    int b = 0xFF &(int) pixelBuffer.get(2);
	    if(r == 0 && g == 0 && b == 0) {
	    	return -1;
	    }
	    int color = ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);

	    Log.i(TAG, "Parsed into:" + r + "," + g + "," + b +"," + color);
	    int index = color/16;
	    if(index > 0) {
	    	hitIndex = index - 1;
	    	if(hitIndex < contactCards.length) {
	    		Log.i(TAG, "Selected contactCards: " + contactCards[hitIndex].name);
	    	}
	    } else {
	    	return -1;
	    }	    
	    
	    
	    
		// If showing radialmenu, test selection for those first.
		
		if (radialMenu.isVisible) {
			if (radialMenu.inTransition()) {
				return -1;
			}
			// TODO test if the color index picked is one of the menu buttons.
			int selectedIconIndex = -1;
			int len = radialMenu.getMenuItems().length;
			for(int a = 0; a < len; a++) {
				IconQuad icon = radialMenu.getMenuItems()[a];
				if(icon.colorIndex[0] == r/255.0f && icon.colorIndex[1] == g/255.0f && icon.colorIndex[2] == b/255.0f) {
					// HIT!
					selectedIconIndex = a;
					break;
				}
			}

			if (selectedIconIndex > -1) {
				if (selectedIconIndex == 0 || selectedIconIndex == 1) {
					radialMenu.execute(selectedIconIndex,
							contactCards[selectedIndex].id);
				}
				return -1;
			}
		}
		
		
		if(AppState.inSelectionMode) {					
			AppState.isZoomToBack = true;	
			AppState.isZoomToFront = false;
			contactCards[selectedIndex].popTransition();
			radialMenu.toggleInOut();
			//selectedIndex = -1;
			
		} else {
			selectedIndex = hitIndex;
			AppState.inSelectionMode = true;	    	
			AppState.isZoomToFront = true;
			//isZoomToBack = false;
			contactCards[selectedIndex].pushTransitionOntoQueueAndStart(
					new Transition(
							new float[]{contactCards[selectedIndex].x, contactCards[selectedIndex].y, contactCards[selectedIndex].z}, 
							new float[]{0.0f, 0.0f, Z_DEPTH+2.0f}, 
							300, acdcIntp, contactCards[selectedIndex].yRot, ROTATION_TO_FRONT)
			);
			
			radialMenu.setStartStop(contactCards[selectedIndex].x, contactCards[selectedIndex].y, contactCards[selectedIndex].z);
			radialMenu.toggleInOut();
			radialMenu.isVisible = true;			
		}
		return selectedIndex;
	}

	
	public void testSelectLongClick(int winX, int winY) {
		
	}


	private void spinToFront(int i, ContactCard cc) {
		
		selectedIndex = i;
		AppState.inSelectionMode = true;					
		AppState.isZoomToFront = true;
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
			RenderState.renderReflection = !RenderState.renderReflection;
			break;
		case SwipeActivity.BACKGROUND:
			RenderState.renderBg = !RenderState.renderBg;
			break;
		case SwipeActivity.PULSE:
			RenderState.renderPulse = !RenderState.renderPulse;
			break;
		case SwipeActivity.INC_REFL:
			amount-=0.4f;
			break;
		case SwipeActivity.DEC_REFL:
			amount+=0.4f;
			break;		
		case SwipeActivity.SOLID:
			RenderState.renderSolid = !RenderState.renderSolid;
			break;
		case SwipeActivity.ROTATE_TO_FRONT:
			if(ROTATION_TO_FRONT == 360.0f) {
				ROTATION_TO_FRONT = 0.0f;
			} else {
				ROTATION_TO_FRONT = 360.0f;
			}
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
