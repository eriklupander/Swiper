package com.squeed.swiper;

import com.squeed.swiper.shapes.ContactCard;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

public class TouchSurfaceView extends GLSurfaceView {
	
	private ContactCardsRenderer mRenderer;
	private float mPreviousX;

	public TouchSurfaceView(Activity context, ContactCard[] contacts) {
		super(context);
		
		if (detectOpenGLES20(context)) {
            this.setEGLContextClientVersion(2);
            mRenderer = new ContactCardsRenderer(context, contacts, this);
        } else {
        	throw new RuntimeException("SwipeBook is only runnable on devices capable of OpenGL ES 2.0");
        }
					
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		setInitialPositionsForCards(contacts);
	}
	
	private boolean detectOpenGLES20(Activity context) {
        ActivityManager am =
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

	public void refreshContacts(ContactCard[] contacts) {
		mRenderer.refreshContacts(contacts);
	}

	private void setInitialPositionsForCards(ContactCard[] contacts) {
		
		int colorIndex = 0x000000;
		colorIndex = colorIndex + 16;
		for (int i = 0; i < contacts.length; i++) {
			if(contacts[i] != null) {
				contacts[i].x = i * 2.5f;
				contacts[i].y = 0.0f;
				contacts[i].z = -2.0f - (Math.abs(i * 2.5f) / 2);
				contacts[i].yRot = 0.0f;
				//contacts[i].colorIndex = (0x000000 + colorIdx);
				contacts[i].colorIndex[0] = ((colorIndex>>16)&0x0ff)/255.0f;
				contacts[i].colorIndex[1] = ((colorIndex>>8) &0x0ff)/255.0f;
				contacts[i].colorIndex[2] = ((colorIndex)    &0x0ff)/255.0f;
				colorIndex = colorIndex + 16;
				Log.i("Colors", "Set: " + contacts[i].colorIndex[0] + " " + contacts[i].colorIndex[1] + " " + contacts[i].colorIndex[2]);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		ContactCardsRenderer.inSelectionMode = false;
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		dumpEvent(e);
		final float x = e.getX();
		final float y = e.getY();
		
		if(ContactCardsRenderer.inSelectionMode) {
			return handleTouchEventInSelectionMode(e, x, y);
		}
		else {		
			return handleTouchEventInSwipeMode(e, x, y);
		}
	}
	
	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event) {
	   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
	      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
	   StringBuilder sb = new StringBuilder();
	   int action = event.getAction();
	   int actionCode = action & MotionEvent.ACTION_MASK;
	   sb.append("event ACTION_" ).append(names[actionCode]);
	   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
	         || actionCode == MotionEvent.ACTION_POINTER_UP) {
	      sb.append("(pid " ).append(
	      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
	      sb.append(")" );
	   }
	   sb.append("[" );
	   for (int i = 0; i < event.getPointerCount(); i++) {
	      sb.append("#" ).append(i);
	      sb.append("(pid " ).append(event.getPointerId(i));
	      sb.append(")=" ).append((int) event.getX(i));
	      sb.append("," ).append((int) event.getY(i));
	      if (i + 1 < event.getPointerCount())
	         sb.append(";" );
	   }
	   sb.append("]" );
	   Log.d("", sb.toString());
	}
	
	private boolean handleTouchEventInSelectionMode(MotionEvent e, final float x, final float y) {
		dumpEvent(e);
		switch (e.getAction()) {		
			case MotionEvent.ACTION_DOWN:
				
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
				
				queueEvent(new Runnable() {
					public void run() {
						mRenderer.testSelect((int)x, (int)y);
					}
				});
				return true;
		}
		return true;
	}
	
	private int clickIndex = 0;
	private boolean isLongPress = false;
	private boolean lastClickWasLongClick = false;
	private static float distance = 0.0f;
	private static float lastDistance = 0.0f;

	private boolean handleTouchEventInSwipeMode(final MotionEvent e, final float x,
			final float y) {
		boolean pinchZooming = false;
		switch (e.getAction()) {
		
		case MotionEvent.ACTION_POINTER_2_DOWN:
			Log.i("Test", "ACTION_POINTER_2_DOWN at x/y: " + x +"/" +y);
			float xDiff = x -  e.getX(1);
			float yDiff = y - e.getY(1);
			
			//Log.i("Test", "ACTION_POINTER_2_DOWN, X-diff: " + xDiff + " Y-diff: " + yDiff);
			lastDistance = (float) FloatMath.sqrt(xDiff*xDiff + yDiff*yDiff); //FloatMath.sqrt(x * x + y * y);
			//Log.i("Test", "ACTION_POINTER_2_DOWN,distance: " + distance);
			
			break;

		

		case MotionEvent.ACTION_DOWN:
			clickIndex++;
			isLongPress = true;
			if(clickIndex - 1 == Integer.MAX_VALUE) {
				clickIndex = 0;
			}
			Log.i("Test", "X/Y clicked: " + x + " / " + y);
			mPreviousX = x;			
			LongClick t = new LongClick(clickIndex, (int) x, (int)y);			
			t.start();
			
			return true;
		case MotionEvent.ACTION_UP:
			isLongPress = false;
			
			Log.i("Test", "X/Y when UP: " + x + " / " + y);
			
			// When we release the touch, check if we have moved since DOWN event.			
			if(!closeEnough(x, mPreviousX)) {
				Log.i("Test", "Slowdown");
				// If so, do the funny slow-down thing.
				queueEvent(new Runnable() {
					public void run() {
						mRenderer.handleActionDown(2.5f);						
					}
				});
			} else if(!lastClickWasLongClick){
				
				Log.i("Test", "Select, not longclcik");
				
				// Not moved? Ahhh - a selection test!				
				queueEvent(new Runnable() {
					public void run() {
						mRenderer.testSelect((int)x, (int)y);
					}
				});
			}
			
			
			
		case MotionEvent.ACTION_MOVE:
			
			if (e.getPointerCount() == 1 && mPreviousX - x > 1 || mPreviousX - x < -1) {
				isLongPress = false;
				// Alternate way of sending an action to the rendering thread, this way, we reuse
				// the same Runnable, just updating a field on it.
				// However, this might be a bad idea if the event processing takes a long time
				// and the next Move event manages to change x prior to completion of the queued event.
				handleActionMove.x = x;
				queueEvent(handleActionMove);
			}
			if (e.getPointerCount() == 2) {
				pinchZooming = true;
				//dumpEvent(e);
				setDistance(e);

				final float diff = lastDistance - distance;			
					queueEvent(new Runnable() {
						public void run() {
							 mRenderer.pinchZoom(diff);
						}
					});
				
				lastDistance = distance;
			}
			if(!pinchZooming) { try {	Thread.sleep(80);	} catch (InterruptedException e1) {} }
			return true;

		}
		return true;
	}

	private void setDistance(final MotionEvent e) {
		float xDiff = e.getX(0) -  e.getX(1);
		float yDiff = e.getY(0) - e.getY(1);
		distance = (float) FloatMath.sqrt(xDiff*xDiff + yDiff*yDiff); //FloatMath.sqrt(x * x + y * y);
	}
		
	private static final float MARGIN = 20.0f;
	
	private boolean closeEnough(float coord, float prevCoord) {
		if(coord > prevCoord-MARGIN && coord < prevCoord+MARGIN) {
			return true;
		} else {
			return false;
		}
	}

	private HandleActionMove handleActionMove = new HandleActionMove();
	
	class HandleActionMove implements Runnable {
		
		public float x;
		
		public void run() {
			mRenderer.handleActionMove(1.0f,
					(mPreviousX - x));
		}
	}
	
	
	
	class LongClick extends Thread {
		private int startClickIndex;
		private int x;
		private int y;
		
		LongClick(int startClickIndex, int x, int y) {
			this.startClickIndex = startClickIndex;
			this.x = x;
			this.y = y;
		}
		
		public void run() {
			try {
				lastClickWasLongClick = false;
				Thread.sleep(500);
				if(startClickIndex == clickIndex && isLongPress) {
					Log.i("ACTION_DOWN", "Long click found!!");
					lastClickWasLongClick = true;
					mRenderer.testSelectLongClick((int)x, (int)y);
				}
			} catch (InterruptedException e) {}
		}
	}

	public void setContext(SwipeActivity swipeActivity) {
		
	}

	public void setContacts(ContactCard[] contacts) {
		
	}

	public void toggle(final int identifier) {
		queueEvent(new Runnable() {
			public void run() {
				 mRenderer.toggle(identifier);
			}
		});
	}
}