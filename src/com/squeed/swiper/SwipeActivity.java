package com.squeed.swiper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.squeed.swiper.helper.ContactLoader;
import com.squeed.swiper.shapes.ContactCard;

/**
 * Starts the Swiper activity.
 * 
 * @author Erik
 *
 */
public class SwipeActivity extends Activity {
	
	private TouchSurfaceView mGLSurfaceView;
	private ContactLoader contactLoader;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.contactLoader = new ContactLoader(this);
		
		// Load contacts from database in this thread..
		ContactCard[] contacts = new ContactLoader(this).loadContacts();
		
		mGLSurfaceView = new TouchSurfaceView(this, contacts);
		initalizeContactCards(contacts);
		
		setContentView(mGLSurfaceView);
		mGLSurfaceView.requestFocus();
		mGLSurfaceView.setFocusableInTouchMode(true);	
		
		if(contacts == null || contacts.length == 0) {
			setContentView(R.layout.main);
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					SwipeActivity.this.finish();
				}				
			});
			return;
		}
	}
	
	private void initalizeContactCards(ContactCard[] contacts) {
		
		int colorIndex = 0x000000;
		colorIndex = colorIndex + 16;
		for (int i = 0; i < contacts.length; i++) {
			if(contacts[i] != null) {
				contacts[i].x = i * 2.5f;
				contacts[i].y = 0.0f;
				contacts[i].z = -2.0f - (Math.abs(i * 2.5f) / 2);
				contacts[i].yRot = 0.0f;
				
				contacts[i].colorIndex[0] = ((colorIndex>>16)&0x0ff)/255.0f;
				contacts[i].colorIndex[1] = ((colorIndex>>8) &0x0ff)/255.0f;
				contacts[i].colorIndex[2] = ((colorIndex)    &0x0ff)/255.0f;

				colorIndex = colorIndex + 16;
				Log.i("Colors", "Set: " + contacts[i].colorIndex[0] + " " + contacts[i].colorIndex[1] + " " + contacts[i].colorIndex[2]);
			}
		}
	}

	public static final int REFLECTION = 0;
	public static final int BACKGROUND = 1;
	public static final int PULSE = 2;
	public static final int INC_REFL = 3;
	public static final int DEC_REFL = 4;
	public static final int SOLID = 6;
	public static final int ROTATE_TO_FRONT = 7;

	@Override
	protected void onResume() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
		ContactCard[] contacts = contactLoader.loadContacts();	
		
		if(contacts == null || contacts.length == 0) {
			setContentView(R.layout.main);
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setOnClickListener(new OnClickListener() {
			
				public void onClick(View v) {
					SwipeActivity.this.finish();
				}				
			});
			
			return;
		}
		
		mGLSurfaceView.refreshContacts(contacts);
		mGLSurfaceView.onResume();
	}


	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
		if(mGLSurfaceView != null)
			mGLSurfaceView.onPause();
	}


	public boolean onContextItemSelected(MenuItem item) {
		mGLSurfaceView.bringToFront();
		return super.onContextItemSelected(item);		
	}
	
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.swipebook_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.toggle_reflection:
	    	mGLSurfaceView.toggle(REFLECTION);
	        return true;
	    case R.id.toggle_background:
	    	mGLSurfaceView.toggle(BACKGROUND);
	        return true;
	    case R.id.toggle_pulse:
	    	mGLSurfaceView.toggle(PULSE);
	        return true;
	    case R.id.inc_refl:
	    	mGLSurfaceView.toggle(INC_REFL);
	        return true;
	    case R.id.dec_refl:
	    	mGLSurfaceView.toggle(DEC_REFL);
	        return true;	   
	    case R.id.solid:
	    	mGLSurfaceView.toggle(SOLID);
	        return true;
	    case R.id.rotateToFront:
	    	mGLSurfaceView.toggle(ROTATE_TO_FRONT);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}