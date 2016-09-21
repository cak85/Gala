package de.cak85.gala.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import de.cak85.gala.R;
import de.cak85.gala.launcher.GameListActivity;

/**
 * Created by ckuster on 27.01.2016.
 */
public class BluetoothDetectionReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPref.getBoolean(context.getString(R.string
				.pref_key_bluetooth_detection_service), false)) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				final BluetoothDevice bluetoothDevice =
						intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Set<String> devicesFilter = PreferenceManager.getDefaultSharedPreferences(context)
						.getStringSet(
								context.getString(
										R.string.pref_key_selected_bluetooth_devices),
								new HashSet<String>()
						);
				if (!devicesFilter.isEmpty()) {
					if (devicesFilter.contains(bluetoothDevice.getAddress())) {
						Intent galaIntent = new Intent(context, GameListActivity.class);
						galaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(galaIntent);
					}
				}
			}
		}
	}
}