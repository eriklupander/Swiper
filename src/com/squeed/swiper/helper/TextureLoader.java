package com.squeed.swiper.helper;

import static android.opengl.GLES10.GL_TEXTURE_2D;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import com.squeed.swiper.R;
import com.squeed.swiper.shapes.BgQuad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Creates texture images using input bitmap image + some non-fancy canvas drawing.
 * 
 * @author Erik
 *
 */
public class TextureLoader {
	
	private static int bgTexture[] = new int[1];
	
	public static BgQuad loadBackgroundTexture(GL10 gl, Context mContext) {

		GLES20.glGenTextures(1, bgTexture, 0);
		GLES20.glBindTexture(GL_TEXTURE_2D, bgTexture[0]);

		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
        
		InputStream is = mContext.getResources().openRawResource(
				R.drawable.default_wallpaper_pot);
		Bitmap tempBitmap = null;
		Bitmap resizedBitmap = null;
		try {
			tempBitmap = BitmapFactory.decodeStream(is);
			 int width = tempBitmap.getWidth();
             int height = tempBitmap.getHeight();
             int newWidth = 512;
             int newHeight = 512;

             // calculate the scale - in this case = 0.4f
             float scaleWidth = ((float) newWidth) / width;
             float scaleHeight = ((float) newHeight) / height;


             // createa matrix for the manipulation
             Matrix matrix = new Matrix();
             // resize the bit map
             matrix.postScale(scaleWidth, scaleHeight);

             // recreate the new Bitmap
             resizedBitmap = Bitmap.createBitmap(tempBitmap, 0, 0,
                               width, height, matrix, true);
		} finally {
			try {
				is.close();
			} catch (IOException e) {}
		}
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, resizedBitmap, 0);

		tempBitmap.recycle();
		resizedBitmap.recycle();
		return new BgQuad(bgTexture);
	}		
	
	/**
	 * This method rescales the specified bitmap into a width/height that are power-of-two.
	 * 
	 * This is because OpenGL generally dislikes non-uniform texture sizes.
	 * 
	 * @param bm
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap rescaleBitmapToPOT(Bitmap bm, int newWidth, int newHeight) {
		int x = bm.getWidth();
		int y = bm.getHeight();
		float scaleWidth = ((float) newWidth) / x;       
		float scaleHeight = ((float) newHeight) / y;
		
		
        // createa matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0,
                          x, y, matrix, true);
		
		
	}
	
	

	public static void drawContactCardBitmap(Bitmap bm, String string, boolean border) {
	
		float x = bm.getWidth();
		float y = bm.getHeight();
		Canvas c = null;
		if(bm.getConfig() == Bitmap.Config.RGB_565) {			
			bm = Bitmap.createBitmap((int)x, (int)y, Bitmap.Config.ARGB_8888);			
			int dest[] = new int[(int) (x*y)];
			bm.setPixels(dest, 0, (int)x, 0, 0, (int)x, (int)y);
			c = new Canvas(bm);			
		} else {
			c = new Canvas(bm);
		}

		Paint p2 = new Paint();
		p2.setColor(Color.WHITE);
		p2.setAntiAlias(true);
		p2.setAlpha(0x90);
		RectF rectF = new RectF(0, y - 25, x, y + 1);
		c.drawRect(rectF, p2); // RoundRect(rectF, 15.0f, 15.0f, p2);
//
		//if(!border) {
			Paint p3 = new Paint();
			p3.setColor(Color.WHITE);
			p3.setStrokeWidth(15);
			p3.setAntiAlias(true);

			c.drawLine(0, 0, x, 0, p3);
			c.drawLine(x, 0, x, y, p3);
			c.drawLine(x, y, 0, y, p3);
			c.drawLine(0, y, 0, 0, p3);
		//}
//		
		Paint p = new Paint();
		p.setAntiAlias(true);		

		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		p.setTextSize(12);
		p.setTextAlign(Paint.Align.CENTER);
		Paint.FontMetrics fm = p.getFontMetrics();
		c.drawText(string, x / 2, (y - fm.ascent - 10) / 1.12f , p);		
	}
	
	public static Bitmap drawContactCardBitmap2(Bitmap bm, String string, boolean border) {
		
		float x = bm.getWidth();
		float y = bm.getHeight();
		Canvas c = null;
		Bitmap tmpBitmap = null;
		
		if(bm.getConfig() == Bitmap.Config.RGB_565) {			
			tmpBitmap = Bitmap.createBitmap((int)x, (int)y, Bitmap.Config.ARGB_8888);
			c = new Canvas(tmpBitmap);
			c.drawBitmap(bm, 0, 0, null);			
		}

		Paint p2 = new Paint();
		p2.setColor(Color.WHITE);
		p2.setAntiAlias(true);
		p2.setAlpha(0x90);
		RectF rectF = new RectF(0, y - 15, x, y + 1);
		c.drawRoundRect(rectF, 15.0f, 15.0f, p2);
//
		if(!border) {
			Paint p3 = new Paint();
			p3.setColor(Color.WHITE);
			p3.setStrokeWidth(15);
			p3.setAntiAlias(true);

			c.drawLine(0, 0, x, 0, p3);
			c.drawLine(x, 0, x, y, p3);
			c.drawLine(x, y, 0, y, p3);
			c.drawLine(0, y, 0, 0, p3);
		}
//		
		Paint p = new Paint();
		p.setAntiAlias(true);

		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		p.setTextSize(12);
		p.setTextAlign(Paint.Align.CENTER);
		Paint.FontMetrics fm = p.getFontMetrics();
		c.drawText(string, x / 2, (y - fm.ascent) / 1.12f , p);
	
		return tmpBitmap;
	}

}
