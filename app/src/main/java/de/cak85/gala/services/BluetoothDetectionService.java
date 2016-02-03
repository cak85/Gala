package de.cak85.gala.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BluetoothDetectionService extends Service {

	private Set<String> devicesFilter = new HashSet<>();

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			/*String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				final BluetoothDevice bluetoothDevice =
						intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Toast.makeText(BluetoothDetectionService.this,
						"Device " + bluetoothDevice.toString() + " connected.",
						Toast.LENGTH_SHORT).show();
				if (devicesFilter.contains(bluetoothDevice.getAddress())) {
					Intent galaIntent = new Intent(context, GameListActivity.class);
					galaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(galaIntent);
				}
			}*/
		}
	};

	public BluetoothDetectionService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			this.devicesFilter = new HashSet<>(intent.getStringArrayListExtra("devicesFilter"));
		} else {
			Toast.makeText(BluetoothDetectionService.this,
					"filters: "+ Arrays.toString(devicesFilter.toArray()),
					Toast.LENGTH_SHORT).show();
		}
		return super.onStartCommand(intent, flags, startId);
	}
}
