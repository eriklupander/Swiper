package com.squeed.swiper.actions;

import java.util.ArrayList;

import com.squeed.swiper.helper.ContactLoader;
import com.squeed.swiper.model.Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

/**
 * Loads phone numbers for a specified contact and presents these
 * on a 2D-overlay.
 * 
 * Selecting a phone number launches the Android Dialer.
 * 
 * @author Erik
 *
 */
public class LaunchDial implements Command {
	
	
//	private final static String DIAL_CONTACT_21_URI = "content://com.android.contacts/raw_contacts/";
//	private final static String DIAL_CONTACT_16_URI = "content://contacts/people/";

	private Context context;	
	
	public LaunchDial(Context context) {
		this.context = context;		
	}

	public void execute(final Object...params) {
		validateParameters(params);
		((Activity)context).runOnUiThread(new Runnable() {

			public void run() {
				
				final ArrayList<Phone> phoneNumbers = ContactLoader.loadPhoneNumbers(
						(String)params[0],
						(Activity)context
				);
				
				String[] phoneNumbersArr = new String[phoneNumbers.size()];
				int a = 0;
				for(Phone phone : phoneNumbers) {
					phoneNumbersArr[a++] = new String(phone.getNumber() + " (" +phone.getType()+")");
				}
				
				AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Select phone number")
                .setItems(phoneNumbersArr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumbers.get(which).getNumber()))); 
                    }
                })
                .create();
				dialog.show();
			}
			 
		 });
	}

	private void validateParameters(Object... params) {
		if(params == null || params.length !=1 || !(params[0] instanceof String)) {
			throw new IllegalArgumentException("Could not launch Intent.ACTION_CALL. Invalid parameter. Expected 1 String parameter");
		}
	}

}
