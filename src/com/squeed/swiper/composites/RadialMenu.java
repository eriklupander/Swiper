package com.squeed.swiper.composites;

import static android.opengl.GLES20.GL_TEXTURE_2D;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.R;
import com.squeed.swiper.actions.LaunchDial;
import com.squeed.swiper.actions.LaunchEditContact;
import com.squeed.swiper.actions.MakeInvisible;
import com.squeed.swiper.actions.MakeVisible;
import com.squeed.swiper.fw.Transition;
import com.squeed.swiper.shapes.IconQuad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.animation.AnticipateOvershootInterpolator;

/**
 * The radial menu is made up of (n) number of IconQuads, evenly distributed across 360 degrees.
 * 
 * Note - this class is specific to the SwipeBook app for now. 
 * 
 * @author erlu708
 *
 */
public class RadialMenu {
	private static final int MENU_SIZE = 4;
	private IconQuad[] menuItems;
	//private int[] textures = new int[MENU_SIZE*4 + 1];
	private Context mContext;
	
	private float degreeSeparation = 0.0f;
	public boolean isVisible = false;
	
	
	public RadialMenu(Context mContext) {
		this.mContext = mContext;
		menuItems = new IconQuad[MENU_SIZE];
		this.degreeSeparation = 360 / MENU_SIZE;
		
		initMenuItems();
		loadTextures();		
	}

	private void initMenuItems() {		
		for(int a = 0; a < MENU_SIZE; a++) {			
			menuItems[a] = getIcon(a);			
		}		
	}
	
	
	public void setStartStop(float startX, float startY, float startZ) {
		float currentAngle = 0.0f;
		final float[] startPos = new float[]{startX, startY, startZ};
		
		AnticipateOvershootInterpolator intp = new AnticipateOvershootInterpolator(1.5f);
		
		for(int a = 0; a < MENU_SIZE; a++) {		
			
			float targetX = (float)Math.sin(Math.toRadians(currentAngle));
			float targetY = (float)Math.cos(Math.toRadians(currentAngle));
			
			// Add out transition
			
			final float[] stopPos = new float[]{targetX, targetY, -2.4f};
			
			//Log.i("RadialMenu", "Currrent angle: " + currentAngle + " menu items: Start: " + MatrixLogger.vector3ToString(startPos) + " Stop: " + MatrixLogger.vector3ToString(stopPos));
			
			Transition outTransition = new Transition(startPos, stopPos, 500, intp);
			menuItems[a].pushTransitionOntoQueue(outTransition);
			
			Transition inTransition = new Transition(stopPos, startPos, 500, intp);			
			menuItems[a].pushTransitionOntoQueue(inTransition);
			
			currentAngle+=degreeSeparation;
		}
	}
	
	public void toggleInOut() {
		for(int a = 0; a < MENU_SIZE; a++) {
			menuItems[a].popTransition();
		}
	}

	public void draw() {
		
		//gl.glMatrixMode(GL10.GL_PROJECTION);	
		
		for(int a = 0; a < MENU_SIZE; a++) {
			menuItems[a].applyTransition();
			menuItems[a].draw();			
		}
		
		//gl.glMatrixMode(GL10.GL_MODELVIEW);
	}
	
	private void loadTextures() {
		
		GLES20.glGenTextures(MENU_SIZE + 1, ContactCardsRenderer.textureIDs, ContactCardsRenderer.currentTextureIndex);

		// Background highlight texture
		GLES20.glBindTexture(GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[ContactCardsRenderer.currentTextureIndex]); // I.e. last icon texture + 1
		initTextureParams();
		loadTexture(R.drawable.jog_dial_dimple);
		
		IconQuad.highLightTextureIndex = ContactCardsRenderer.currentTextureIndex;
		
		ContactCardsRenderer.currentTextureIndex++;
		for(int a = 0;a < MENU_SIZE; a++) {
			GLES20.glBindTexture(GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[ContactCardsRenderer.currentTextureIndex]);
			initTextureParams();
    
			int resourceId = getIconOfIndex(a);
			loadTexture(resourceId);
			menuItems[a].textureIndex = ContactCardsRenderer.currentTextureIndex;
			
			ContactCardsRenderer.currentTextureIndex++;
		}
	}

	private void initTextureParams() {
		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
	}

	private void loadTexture(int resourceId) {
		InputStream is = mContext.getResources().openRawResource(resourceId);
		Bitmap tempBitmap = null;
		try {
			tempBitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {}
		}
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, tempBitmap, 0);					

		tempBitmap.recycle();
	}
	
	
	private IconQuad getIcon(int index) {
		switch(index) {
			case 0:
				return new IconQuad(new LaunchDial(mContext));
			
			case 1:
				return new IconQuad(new LaunchEditContact(mContext));				
		
			case 2:
				return new IconQuad(new MakeVisible());
	
			case 3:
				return new IconQuad(new MakeInvisible());
		}
		return null;
	}
	
	private int getIconOfIndex(int a) {
		switch(a) {
			case 0:
				return R.drawable.ic_jog_dial_answer;
			
			case 1:
				return R.drawable.btn_rating_star_off_normal;
		
			case 2:
				return R.drawable.btn_rating_star_off_selected;
	
			case 3:
				return R.drawable.btn_rating_star_on_normal;
		}
		throw new IllegalArgumentException("Unknown icon ID: " + a);
	}
	
	/**
	 * Given the supplied win x/y, this method calculates 3d->2d coords for each icon and tests for hit.
	 * 
	 * @param winX
	 * @param winY
	 * @return
	 */
	public int testSelect(int winX, int winY) {
//		for(int a = 0; a < MENU_SIZE; a++) {
//			IconQuad icon = menuItems[a];
//			float[] obj = icon.get2DCoordsFrom3D();
//			if (winX > obj[0] - 20 && winX < obj[0] + 20
//					&& winY > obj[1] - 20 && winY < obj[1] + 20) {		
//				
//				// Set distance (in %) from center of icon.
//				// interpolate x/y in some smart manner
//				int diffX = Math.abs(winX - (int) obj[0]);
//				int diffY = Math.abs(winY - (int) obj[1]);
//				
//				float diff = (float) Math.sqrt(diffX*diffX + diffY*diffY);
//				distanceFromCenter = 1.0f - diff/20.0f;
//				//Log.i("RadialMenu", "Distance from center X/Y: " + diffX + "/" + diffY + " gives diff: " + diff + " gives %: "+ distanceFromCenter);
//				return a;
//			}
//		}			
//		distanceFromCenter = -1.0f;
		return -1;
	}
	
	public static float distanceFromCenter = -1.0f;
	
	
	
	public void execute(int iconIndex, Object...params) {
		if(iconIndex > -1 && menuItems != null && iconIndex < menuItems.length && menuItems[iconIndex] != null && menuItems[iconIndex].actionWhenClicked != null) {
			Log.i("RadialMenu", "Executing command for icon " + iconIndex);
			menuItems[iconIndex].actionWhenClicked.execute(params);
		} else {
			Log.i("RadialMenu", "Could not execute command for icon " + iconIndex + " no action defined");
		}
	}

	public void setHighlightIcon(int selectedIconIndex, boolean isHighlighted) {
		menuItems[selectedIconIndex].isHighlighted = isHighlighted;
	}

	public void resetHighlightIcon() {
		for(int a = 0; a < MENU_SIZE; a++) {
			if(menuItems[a].isHighlighted)
				menuItems[a].isHighlighted = false;
		}
	}

	public boolean inTransition() {
		for(int a = 0; a < MENU_SIZE; a++) {
			if(menuItems[a].currentTransition != null && menuItems[a].currentTransition.timeOfStart != 0) {
				return true;
			}
		}
		return false;
	}
	
}
