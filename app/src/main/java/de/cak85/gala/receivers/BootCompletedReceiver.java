package de.cak85.gala.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.cak85.gala.R;
import de.cak85.gala.services.BluetoothDetectionService;

/**
 * Created by ckuster on 19.01.2016.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPref.getBoolean(context.getString(R.string
				.pref_key_bluetooth_detection_service), false)) {
			Intent startServiceIntent = new Intent(context, BluetoothDetectionService.class);
			context.startService(startServiceIntent);
		}
	}
}