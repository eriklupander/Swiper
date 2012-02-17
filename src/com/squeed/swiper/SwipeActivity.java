package com.squeed.swiper;

import javax.microedition.khronos.opengles.GL;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;

import com.squeed.swiper.helper.ContactLoader;
import com.squeed.swiper.shapes.ContactCard;
import com.squeed.swiper.util.MatrixTrackingGL;

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
		mGLSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper() {			
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }});
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

	public static final int REFLECTION = 0;
	public static final int BACKGROUND = 1;
	public static final int PULSE = 2;
	public static final int INC_REFL = 3;
	public static final int DEC_REFL = 4;
	public static final int ROTATE = 5;
	public static final int SOLID = 6;

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

//	@Override
//	public void onBackPressed() {
//		super.onBackPressed();
//		finish();
//		
//	}

	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
		if(mGLSurfaceView != null)
			mGLSurfaceView.onPause();
	}
		
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 1, 0, "Edit");
		menu.add(0, 2, 0, "Delete");
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case 1:
				Log.i("CONTEXT-MENU", "(1)Info-ID");				
				break;
			case 2:
				Log.i("CONTEXT-MENU", "(2)Info-ID");
				break;
		}
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
	    case R.id.rotate:
	    	mGLSurfaceView.toggle(ROTATE);
	        return true;
	    case R.id.solid:
	    	mGLSurfaceView.toggle(SOLID);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}