package com.squeed.swiper.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Test class, create a bitmap with the specified text, in white.
 * 
 * @author Erik
 *
 */
public class TextCreator {
	public static Bitmap createText(String text) {
		Canvas c = null;
		
		Bitmap tmpBitmap = Bitmap.createBitmap(text.length()*13, 24, Bitmap.Config.ARGB_8888);
		c = new Canvas(tmpBitmap);
		
		

		Paint p = new Paint();
		p.setColor(Color.WHITE);
		p.setAntiAlias(true);
		
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		p.setTextSize(24);
		p.setTextAlign(Paint.Align.LEFT);
		Paint.FontMetrics fm = p.getFontMetrics();
		c.drawText(text, 0, 18 , p);
	
		return tmpBitmap;
	}
}
