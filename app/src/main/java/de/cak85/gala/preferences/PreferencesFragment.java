package de.cak85.gala.preferences;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import de.cak85.gala.R;

public class PreferencesFragment extends PreferenceFragment
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final int REQUEST_ENABLE_BT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		final GalaMultiSelectListPreference multiSelectListPreference =
				(GalaMultiSelectListPreference) findPreference(getActivity().getString(
						R.string.pref_key_selected_bluetooth_devices));
		if (((CheckBoxPreference) findPreference(getActivity()
				.getString(R.string.pref_key_bluetooth_detection_service))).isChecked()) {
			/*Intent serviceIntent = new Intent(getActivity(), BluetoothDetectionService.class);
			serviceIntent.putStringArrayListExtra("devicesFilter",
					new ArrayList<String>(multiSelectListPreference.getValues()));
			getActivity().startService(serviceIntent);*/
			multiSelectListPreference.setEnabled(true);
			if (!multiSelectListPreference.getValues().isEmpty()) {
				getDetailedSummary(multiSelectListPreference);
			}
			multiSelectListPreference.setOnClickListner(
					new GalaMultiSelectListPreference.OnClickListener() {
						@Override
						public boolean onClick() {
							populateBluetoothDevicesList();
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							return multiSelectListPreference.getEntries().length <= 0;
						}
					});
		}
	}

	private void getDetailedSummary(GalaMultiSelectListPreference multiSelectListPreference) {
		StringBuilder sb = new StringBuilder();

		for (String s : multiSelectListPreference.getValues()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(/*pairedDevices.get(*/s/*)*/);
		}
		final int i = sb.lastIndexOf(", ");
		if (i > -1) {
			sb.replace(i, i+1, " or");
		}

		multiSelectListPreference.setSummary(getActivity().getString(
				R.string.pref_selected_bluetooth_devices_summ_concrete, sb,
				multiSelectListPreference.getValues().size() > 1 ?
						getActivity().getString(R.string.is_plural_auxiliary_verb) :
						getActivity().getString(R.string.is_singular_auxiliary_verb)));
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener
				(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener
				(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//Intent serviceIntent = new Intent(getActivity(), BluetoothDetectionService.class);
		if (key.equals(getActivity().getString(R.string.pref_key_bluetooth_detection_service))) {
			if (((CheckBoxPreference) findPreference(key)).isChecked()) {
				findPreference(getActivity().getString(
						R.string.pref_key_selected_bluetooth_devices)).setEnabled(true);
			} else {
				//getActivity().stopService(serviceIntent);
				findPreference(getActivity().getString(
						R.string.pref_key_selected_bluetooth_devices)).setEnabled(false);
			}
		} else if (key.equals(getActivity().getString(R.string.pref_key_selected_bluetooth_devices))) {
			GalaMultiSelectListPreference p = (GalaMultiSelectListPreference) findPreference(key);
			if (!p.getValues().isEmpty()) {
				getDetailedSummary(p);
			} else {
				p.setSummary(getActivity().getString(
						R.string.pref_selected_bluetooth_devices_summ));
			}
		} else if (key.equals(getActivity().getString(R.string.pref_key_user_interface_num_columns))) {
			ListPreference l = (ListPreference) findPreference(key);
			l.setSummary(getActivity().getString(
					R.string.pref_user_interface_num_columns_sum, l.getEntry()));
		} else if (key.equals(getActivity().getString(R.string.pref_key_user_interface_spacing))) {
			ListPreference l = (ListPreference) findPreference(key);
			l.setSummary(getActivity().getString(
					R.string.pref_user_interface_spacing_sum, l.getEntry()));
		} else if (key.equals(getActivity().getString(R.string.pref_key_user_interface_shadow))) {
			CheckBoxPreference c = (CheckBoxPreference) findPreference(key);
			if (c.isChecked()) {
				c.setSummary(getActivity().getString(
						R.string.pref_user_interface_shadow_sum_true));
			} else {
				c.setSummary(getActivity().getString(
						R.string.pref_user_interface_shadow_sum_false));
			}
		}
	}

	private void populateBluetoothDevicesList() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		MultiSelectListPreference bluetoothDevicesPref =
				(MultiSelectListPreference)
						findPreference(getActivity().getString(
								R.string.pref_key_selected_bluetooth_devices));
		bluetoothDevicesPref.setEntries(new String[]{});
		bluetoothDevicesPref.setEntryValues(new String[]{});
		if (checkBluetoothState(bluetoothAdapter)) {
			List<CharSequence> pairedDevicesNames = new ArrayList<>();
			List<CharSequence> pairedDevicesAddresses = new ArrayList<>();
			for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
				pairedDevicesNames.add(bluetoothDevice.getName());
				pairedDevicesAddresses.add(bluetoothDevice.getAddress());
			}
			bluetoothDevicesPref.setEntries(pairedDevicesNames.toArray(
					new String[pairedDevicesNames.size()]));
			bluetoothDevicesPref.setEntryValues(pairedDevicesAddresses.toArray(
					new String[pairedDevicesAddresses.size()]));
		}
	}

	private boolean checkBluetoothState(BluetoothAdapter bluetoothAdapter) {
		if (bluetoothAdapter == null) {
			showErrorDialog(getActivity().getString(R.string.bluetooth_not_supported));
			(findPreference(getActivity().getString(R.string.pref_key_bluetooth_detection_service)))
					.setEnabled(false);
			findPreference(getActivity().getString(
					R.string.pref_key_selected_bluetooth_devices)).setEnabled(false);
		} else {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				populateBluetoothDevicesList();
				getPreferenceScreen().onItemClick(null, null, findPreference(getActivity().getString(
						R.string.pref_key_selected_bluetooth_devices)).getOrder(), 0);
			} else {
				showErrorDialog(getActivity().getString(R.string.bluetooth_enable_error));
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void showErrorDialog(String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle(getActivity().getString(R.string.error));
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}
}
