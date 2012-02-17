package com.squeed.swiper.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Launches the ACTION_EDIT intent.
 * @author Erik
 *
 */
public class LaunchEditContact implements Command {
	
	private final static String EDIT_CONTACT_21_URI = "content://com.android.contacts/raw_contacts/";
	private final static String EDIT_CONTACT_16_URI = "content://contacts/people/";
	
	private Context context;	
	
	public LaunchEditContact(Context context) {
		this.context = context;
	}

	public void execute(Object... params) {
		validateParameters(params);
		this.context.startActivity(new Intent(Intent.ACTION_EDIT, Uri.parse(EDIT_CONTACT_16_URI + params[0])));
	}
	
	private void validateParameters(Object... params) {
		if(params == null || params.length !=1 || !(params[0] instanceof String)) {
			throw new IllegalArgumentException("Could not launch Intent.ACTION_EDIT. Invalid parameter. Expected 1 String parameter");
		}
	}

}
