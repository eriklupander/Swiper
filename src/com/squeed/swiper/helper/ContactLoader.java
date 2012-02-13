package com.squeed.swiper.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.squeed.swiper.model.Phone;
import com.squeed.swiper.shapes.ContactCard;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactLoader {

	static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
		ContactsContract.Contacts._ID, // 0
		ContactsContract.Contacts.DISPLAY_NAME, // 1
		ContactsContract.Contacts.STARRED, // 2
		ContactsContract.Contacts.TIMES_CONTACTED, // 3
		ContactsContract.Contacts.CONTACT_PRESENCE, // 4
		ContactsContract.Contacts.PHOTO_ID, // 5
		ContactsContract.Contacts.LOOKUP_KEY, // 6
		ContactsContract.Contacts.HAS_PHONE_NUMBER, // 7
	};

	static final int SUMMARY_ID_COLUMN_INDEX = 0;
	static final int SUMMARY_NAME_COLUMN_INDEX = 1;
	static final int SUMMARY_STARRED_COLUMN_INDEX = 2;
	static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3;
	static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4;
	static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 5;
	static final int SUMMARY_LOOKUP_KEY = 6;
	static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 7;
	
	private Activity context;

	static final int CARD_LIMIT = 50;
	
	public ContactLoader(Activity context) {
		super();
		this.context = context;
	}
	
	public ContactCard[] loadContacts() {
		String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND (" + ContactsContract.Contacts.HAS_PHONE_NUMBER + " == 1))";
		Cursor c = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				CONTACTS_SUMMARY_PROJECTION, 
				select, 
				null,
				ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
		context.startManagingCursor(c);

		//ContactCard[] contacts = new ContactCard[c.getCount()];
		
		ArrayList<ContactCard> cList = new ArrayList<ContactCard>();
		int a = 0;
		while (c.moveToNext() && a < CARD_LIMIT) {
			cList.add(new ContactCard(c.getString(0), c.getString(1),
					loadContactPhoto21(c)));
			a++;
		}
		c.close();
		return cList.toArray(new ContactCard[a]);
	}
	

	
//	private Bitmap loadContactPhoto16(Cursor cursor) {
//		Uri contactUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,
//				cursor.getInt(0));
//		InputStream is = Contacts.People.openContactPhotoInputStream(this.context.getContentResolver(), contactUri);
//		return BitmapFactory.decodeStream(is);	
//	}
//	
	private Bitmap loadContactPhoto21(Cursor cursor) {

		if (cursor.getString(5) != null) {
			Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
					cursor.getInt(0));
			InputStream is = null;
			try {
				is = ContactsContract.Contacts.openContactPhotoInputStream(
						context.getContentResolver(), contactUri);
				Log.i("", "Loaded image InputStream for " +  cursor.getString(1));
				return BitmapFactory.decodeStream(is);
//			} catch(OutOfMemoryError err) {
//				Log.i("", err.getMessage());
//				return null;
			} catch(Exception e) {
				Log.e("", "Error reading image: " + e.getMessage());
			} finally {
			
				try {
					if(is!=null)
						is.close();
				} catch (IOException e) {
					Log.e("", "Error closing InputStream: " + e.getMessage());
				}
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	public String[] loadPersonDetails(String id) {
		String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL AND " + "people." + ContactsContract.Contacts._ID + "=?))";
		Cursor c = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
				CONTACTS_SUMMARY_PROJECTION, select, new String[]{id},  
				ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
		context.startManagingCursor(c);
		
		String[] resp = null;
		if (c.moveToNext()) {
			resp = new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)};
		}
		
		c.close();
		
		return resp;
	}

//	public static String[] loadPhoneNumbers(String personId, Activity localContext) {
//		Uri personUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(personId));
//		Uri phonesUri = Uri.withAppendedPath(personUri, ContactsContract.PhoneLookup.Contacts.Contacts.Phones.CONTENT_DIRECTORY);
//		String[] proj = new String[] {Phones._ID, Phones.TYPE, Phones.NUMBER, Phones.LABEL};
//		Cursor c = localContext.getContentResolver().query(phonesUri, proj, null, null, null);
//
//		localContext.startManagingCursor(c);
//		
//		
//		ArrayList<String> l = new ArrayList<String>();
//		
//		while (c.moveToNext()) {
//			String str = new String(c.getString(2)); // + "(" + c.getString(3) + ")");
//			l.add(str);
//		}
//		
//		c.close();
//		String[] resp = new String[l.size()];
//		return l.toArray((resp));
//	}
	
	public static ArrayList<Phone> loadPhoneNumbers(String id, Activity localContext) {
 		ArrayList<Phone> phones = new ArrayList<Phone>();
 		
 		Cursor pCur = localContext.getContentResolver().query(
 				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 				null, 
 				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
 				new String[]{id}, null);
 		while (pCur.moveToNext()) {
 			String typeStrId = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
 			int typeLabelResource = ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(Integer.parseInt(typeStrId));
 			String typeLabelStr = localContext.getResources().getString(typeLabelResource);
 			Log.i("ContactLoader", "1: " + typeStrId + " 2: " + typeLabelResource + " 3: " + typeLabelStr);
 			phones.add(new Phone(
 					pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
 					typeLabelStr
 			));
 
 		} 
 		pCur.close();
 		return(phones);
 	}
 	
// 	public ArrayList<Email> getEmailAddresses(String id) {
// 		ArrayList<Email> emails = new ArrayList<Email>();
// 		
// 		Cursor emailCur = this.cr.query( 
// 				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
// 				null,
// 				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
// 				new String[]{id}, null); 
// 		while (emailCur.moveToNext()) { 
// 		    // This would allow you get several email addresses
// 			Email e = new Email(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
// 					,emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))  
// 					);
// 			emails.add(e);
// 		} 
// 		emailCur.close();
// 		return(emails);
// 	}

}
