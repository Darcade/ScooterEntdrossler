package de.darcade.scooterentdrossler;

import java.util.Set;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceChooser extends Activity {

	protected static final String DEVICE_MAC_KEY = "device_mac";

	private boolean mEnablingBT = false;

	private BluetoothAdapter mBluetoothAdapter = null;

	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mAvailableDevicesArrayAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devicechooser_activity);
	
		
		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		mAvailableDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.pairedDevices_list);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mPairedDeviceClickListener);


		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		
		
		
		
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, getString(R.string.device_has_no_bluetooth),
					Toast.LENGTH_LONG).show();
			finish();
		}

		while (!mEnablingBT) { // If we are turning on the BT we cannot check if
			// it's enable
			//System.out.println("DOING LOOP");
			mEnablingBT = true;
			if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {

				if (!checkBlueAutoToggle(mBluetoothAdapter)) {
					Intent btIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(btIntent, 1);
				}

			
			//System.out.println("DOING LOOP_END");
			Intent mStartActivity = new Intent(this, MainActivity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
			}
		}


		Button reloadButton = (Button) findViewById(R.id.reload_button);

		reloadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doDiscovery();
			}
		});





	}

	private void showDevices() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {

			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			String noDevices = getResources().getString(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mEnablingBT = false;
		
		doDiscovery();

		showDevices();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	/**
	 * Returns wether the Bluetooth has been toggled automaticly or not.
	 * 
	 * @param btAdapter
	 * @return
	 */

	private boolean checkBlueAutoToggle(BluetoothAdapter btAdapter) {
		SettingsManager settings = new SettingsManager(this);
		boolean bluetoothautotoggle = settings.getBlueAutotoggle();

		if (bluetoothautotoggle && !btAdapter.isEnabled()) {
			btAdapter.enable();
			Toast.makeText(this,
					getString(R.string.bluetooth_automaticly_enabled),
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO DO SOMETHING
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure we're not doing discovery anymore
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		// setTitle(R.string.scanning);
		// Turn on sub-title for new devices
		// findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}



	private OnItemClickListener mPairedDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			String selectedBox = mPairedDevicesArrayAdapter.getItem(arg2);
			String selectedMacAddress = selectedBox.substring(
					selectedBox.lastIndexOf("\n")).replaceFirst("\n", "");
			// Toast.makeText(MainActivity.this, selectedMacAddress,
			// Toast.LENGTH_SHORT).show();
			
			SettingsManager settings = new SettingsManager(DeviceChooser.this);
			settings.setDevice(selectedMacAddress, mBluetoothAdapter.getRemoteDevice(selectedMacAddress).getName());
			finish();
		}
	};


	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (!checkDevice(device.getName() + "\n"
							+ device.getAddress())) {
						mAvailableDevicesArrayAdapter.add(device.getName()
								+ "\n" + device.getAddress());
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mAvailableDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getString(
							R.string.none_found).toString();
					mAvailableDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	// checks wether the device already has been listed or not
	private boolean checkDevice(String device) {
		boolean listed = false;
		for (int i = 0; i < mAvailableDevicesArrayAdapter.getCount(); i++) {
			if (mAvailableDevicesArrayAdapter.getItem(i).equals(device)) {
				listed = true;
			}
		}

		return listed;
	}
}