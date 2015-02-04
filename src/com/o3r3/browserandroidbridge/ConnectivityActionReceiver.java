package com.o3r3.browserandroidbridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

public class ConnectivityActionReceiver extends BroadcastReceiver {

	public static final String WIFI_STATE = "WIFI_STATE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) { 
//            Log.d("WifiReceiver", "Have Wifi Connection");
            sendIntent(context);
        }
        else {
//            Log.d("WifiReceiver", "Don't have Wifi Connection");
            sendIntent(context);
        }		
	}
	
	private void sendIntent (Context context) {
		Intent intento = new Intent(WIFI_STATE);
		intento.putExtra("datos", "esto es lo que hay");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intento);
	}
	
}
