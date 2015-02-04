package com.o3r3.browserandroidbridge;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * Service to share content
 * 
 * @author carvilsi
 *
 */

public class ShareWithMe extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		 // Get the intent that started this activity
		String data = new String();
		Intent ir=new Intent(this, ShareWithMe.class); 
		ir.putExtra("data", data); 
		this.startService(ir); 
		data=(String) ir.getExtras().get("data"); 
	}

}
