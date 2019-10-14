package de.cak85.gala.preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.cak85.gala.R;

public class PreferencesFragment extends PreferenceFragment
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final int REQUEST_ENABLE_BT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		initBluetoothSettings();
	}

	private String getDetailedSummary(GalaMultiSelectListPreference multiSelectListPreference) {
		StringBuilder sb = new StringBuilder();

		for (String s : multiSelectListPreference.getValues()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			// For each value retrieve index
			int index = multiSelectListPreference.findIndexOfValue(s);
			// Retrieve entry from index
			CharSequence mEntry = index >= 0
					&& multiSelectListPreference.getEntries() != null ? multiSelectListPreference
					.getEntries()[index] : null;
			if (mEntry != null) {
				sb.append(mEntry).append(" (").append(s).append(")");
			}
		}
		final int i = sb.lastIndexOf(",");
		if (i > -1) {
			sb.replace(i, i+1, " " + getActivity().getString(R.string.or));
		}

		return getActivity().getString(
				R.string.pref_selected_bluetooth_devices_summ_concrete, sb,
				multiSelectListPreference.getValues().size() > 1 ?
						getActivity().getString(R.string.is_plural_auxiliary_verb) :
						getActivity().getString(R.string.is_singular_auxiliary_verb));
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
				String summary = getDetailedSummary(p);
				p.setSummary(summary);
			} else {
				p.setSummary(getActivity().getString(
						R.string.pref_selected_bluetooth_devices_summ));
			}
		} else if (key.equals(getActivity().getString(R.string.pref_key_user_interface_orientation))) {
			ListPreference l = (ListPreference) findPreference(key);
			String entry = getCurrentLocale().getLanguage().equals("en")
					? l.getEntry().toString().toLowerCase() : l.getEntry().toString();
			//noinspection WrongConstant
			getActivity().setRequestedOrientation(Integer.parseInt(l.getValue()));
			l.setSummary(getActivity().getString(
					R.string.pref_user_interface_orientation_sum, entry));
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
		} else if (key.equals(getActivity().getString(
				R.string.pref_key_user_interface_show_downloaded_images))) {
			CheckBoxPreference c = (CheckBoxPreference) findPreference(key);
			if (c.isChecked()) {
				c.setSummary(getActivity().getString(
						R.string.pref_user_interface_show_downloaded_images_sum_true));
			} else {
				c.setSummary(getActivity().getString(
						R.string.pref_user_interface_show_downloaded_images_sum_false));
			}
		} else if (key.equals(getActivity().getString(R.string.pref_key_user_interface_height))) {
			ListPreference l = (ListPreference) findPreference(key);
			l.setSummary(getActivity().getString(
					R.string.pref_user_interface_height_sum, l.getEntry()));
		}
	}

	@TargetApi(Build.VERSION_CODES.N)
	public Locale getCurrentLocale(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			return getResources().getConfiguration().getLocales().get(0);
		} else{
			//noinspection deprecation
			return getResources().getConfiguration().locale;
		}
	}

	private void populateBluetoothDevicesList() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		GalaMultiSelectListPreference bluetoothDevicesPref =
				(GalaMultiSelectListPreference)
						findPreference(getActivity().getString(
								R.string.pref_key_selected_bluetooth_devices));
		bluetoothDevicesPref.setEntries(new String[]{});
		bluetoothDevicesPref.setEntryValues(new String[]{});
		if (bluetoothAdapter.isEnabled()) {
			List<CharSequence> pairedDevicesNames = new ArrayList<>();
			List<CharSequence> pairedDevicesAddresses = new ArrayList<>();
			for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
				pairedDevicesNames.add(bluetoothDevice.getName());
				pairedDevicesAddresses.add(bluetoothDevice.getAddress());
			}
			bluetoothDevicesPref.setEntries(pairedDevicesNames.toArray(
					new CharSequence[pairedDevicesNames.size()]));
			bluetoothDevicesPref.setEntryValues(pairedDevicesAddresses.toArray(
					new CharSequence[pairedDevicesAddresses.size()]));
			bluetoothDevicesPref.setSummary(getDetailedSummary(bluetoothDevicesPref));
		} else {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private void initBluetoothSettings() {
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			Preference bluetoothPreferences = getPreferenceScreen()
					.findPreference(getActivity().getString(R.string.pref_cat_bluetooth));
			getPreferenceScreen().removePreference(bluetoothPreferences);
		} else if (((CheckBoxPreference) findPreference(getActivity()
					.getString(R.string.pref_key_bluetooth_detection_service))).isChecked()) {
			final GalaMultiSelectListPreference multiSelectListPreference =
					(GalaMultiSelectListPreference) findPreference(getActivity().getString(
							R.string.pref_key_selected_bluetooth_devices));
			multiSelectListPreference.setEnabled(true);
			populateBluetoothDevicesList();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				populateBluetoothDevicesList();
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
