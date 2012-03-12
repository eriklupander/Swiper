package com.squeed.swiper.composites;

import static android.opengl.GLES20.GL_TEXTURE_2D;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import com.squeed.swiper.ContactCardsRenderer;
import com.squeed.swiper.R;
import com.squeed.swiper.actions.LaunchDial;
import com.squeed.swiper.actions.LaunchEditContact;
import com.squeed.swiper.actions.MakeInvisible;
import com.squeed.swiper.actions.MakeVisible;
import com.squeed.swiper.fw.ObjectRenderer;
import com.squeed.swiper.fw.Transition;
import com.squeed.swiper.shader.Shaders;
import com.squeed.swiper.shapes.IconQuad;

/**
 * The radial menu is made up of (n) number of IconQuads, evenly distributed across 360 degrees.
 * 
 * Note - this class is specific to the Swiper app for now. 
 * 
 * @author Erik
 *
 */
public class RadialMenu {
	
	private static final int MENU_SIZE = 4;
	private static final int UI_COLOR_IDX_OFFSET = 0xFF0000;
	private IconQuad[] menuItems;
	private Context mContext;
	
	/**
	 * How many degrees in rotation separation. If four menu items, we'll want 90 degrees, for 6, 60 degrees.
	 */
	private float degreeSeparation = 0.0f;
	public boolean isVisible = false;
	private ObjectRenderer objectRenderer;	
	
	
	public RadialMenu(Context mContext, ObjectRenderer objectRenderer) {
		this.mContext = mContext;
		this.objectRenderer = objectRenderer;
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
	
	public IconQuad[] getMenuItems() {
		return menuItems;
	}
	
	public void setStartStop(float startX, float startY, float startZ) {
		float currentAngle = 0.0f;
		final float[] startPos = new float[]{startX, startY, 1.0f};
		
		AnticipateOvershootInterpolator intp = new AnticipateOvershootInterpolator(1.5f);
		AccelerateDecelerateInterpolator li = new AccelerateDecelerateInterpolator();
		
		for(int a = 0; a < MENU_SIZE; a++) {		
			
			float targetX = (float)Math.sin(Math.toRadians(currentAngle));
			float targetY = (float)Math.cos(Math.toRadians(currentAngle));
			
			// Add out transition			
			final float[] stopPos = new float[]{targetX, targetY, 5.5f};
			
			Transition outTransition = new Transition(startPos, stopPos, 500, intp);
			menuItems[a].pushTransitionOntoQueue(outTransition);
			
			Transition inTransition = new Transition(stopPos, startPos, 500, li);			
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
		GLES20.glUseProgram(Shaders.defaultShader.program);
		for(int a = 0; a < MENU_SIZE; a++) {
			menuItems[a].applyTransition();
			objectRenderer.render(menuItems[a].x, menuItems[a].y, menuItems[a].z, menuItems[a].xRot, menuItems[a].yRot, menuItems[a].zRot, menuItems[a].textureId, Shaders.defaultShader, 1.0f, 1);
		}
	}
	
	public void drawSelection() {		
		GLES20.glUseProgram(Shaders.colorShader.program);
		for(int a = 0; a < MENU_SIZE; a++) {
			menuItems[a].applyTransition();
			GLES20.glUniform3fv(Shaders.colorShader.colorHandle, 1, menuItems[a].colorIndex, 0); //glUniform1f(Shaders.colorShader.colorHandle, menuItems[a].colorIndex);
			objectRenderer.renderSolidColorVBO(menuItems[a].x, menuItems[a].y, menuItems[a].z, menuItems[a].xRot, menuItems[a].yRot, menuItems[a].zRot, Shaders.colorShader, 1);
		}
	}
	
	private void loadTextures() {
		
		GLES20.glGenTextures(MENU_SIZE, ContactCardsRenderer.textureIDs, ContactCardsRenderer.currentTextureIndex);

		// Background highlight texture
//		GLES20.glBindTexture(GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[ContactCardsRenderer.currentTextureIndex]); // I.e. last icon texture + 1
//		initTextureParams();
//		loadTexture(R.drawable.jog_dial_dimple);
		
//		ContactCardsRenderer.currentTextureIndex++;
		ContactCardsRenderer.currentTextureIndex++;
		for(int a = 0;a < MENU_SIZE; a++) {
			GLES20.glBindTexture(GL_TEXTURE_2D, ContactCardsRenderer.textureIDs[ContactCardsRenderer.currentTextureIndex]);
			initTextureParams();
    
			int resourceId = getIconOfIndex(a);
			loadTexture(resourceId);
			menuItems[a].textureId = ContactCardsRenderer.textureIDs[ContactCardsRenderer.currentTextureIndex];
			
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
				return new IconQuad(new LaunchDial(mContext), UI_COLOR_IDX_OFFSET);
			
			case 1:
				return new IconQuad(new LaunchEditContact(mContext), UI_COLOR_IDX_OFFSET+16);				
		
			case 2:
				return new IconQuad(new MakeVisible(), UI_COLOR_IDX_OFFSET+32);
	
			case 3:
				return new IconQuad(new MakeInvisible(), UI_COLOR_IDX_OFFSET+48);
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
	
	
	
	
	
	
	public void execute(int iconIndex, Object...params) {
		if(iconIndex > -1 && menuItems != null && iconIndex < menuItems.length && menuItems[iconIndex] != null && menuItems[iconIndex].actionWhenClicked != null) {
			Log.i("RadialMenu", "Executing command for icon " + iconIndex);
			menuItems[iconIndex].actionWhenClicked.execute(params);
		} else {
			Log.i("RadialMenu", "Could not execute command for icon " + iconIndex + ", no action defined");
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
