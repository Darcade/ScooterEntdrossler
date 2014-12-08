package de.darcade.scooterentdrossler;

import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final String DEVICE_MAC_KEY = "device_mac";
	private static final int REQUEST_ENABLE_BT = 1;

	private boolean mEnablingBT = false;
	private boolean mAutoconnected = false;

	private BluetoothAdapter mBluetoothAdapter = null;

	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mAvailableDevicesArrayAdapter;

	// Used for checking wether the autoconnect device is available
	private ArrayAdapter<String> mAllAvailableDevices;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		mAvailableDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		mAllAvailableDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.pairedDevices_list);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mPairedDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.availableDevices_list);
		newDevicesListView.setAdapter(mAvailableDevicesArrayAdapter);
		newDevicesListView
				.setOnItemClickListener(mAvailableDeviceClickListener);

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
			System.out.println("DOING LOOP");
			mEnablingBT = true;
			if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {

				if (!checkBlueAutoToggle(mBluetoothAdapter)) {
					mEnablingBT = true;
					Intent btIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(btIntent, REQUEST_ENABLE_BT);
					//btIntent.
					
				}

				System.out.println("DOING LOOP_END");
				
			}
		}

		Button reloadButton = (Button) findViewById(R.id.reload_button);

		reloadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doDiscovery();
			}
		});
		doDiscovery();
		checkAutoconnect(mBluetoothAdapter);

	}

	private void showDevices() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {

			for (BluetoothDevice device : pairedDevices) {
				if (!checkDevice(device.getName() + "\n" + device.getAddress(),
						mPairedDevicesArrayAdapter))
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

			restartApp();

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks wether there is a device to autoconnect to, if yes than it
	 * connects to the default device.
	 * 
	 * @param btAdapter
	 * @return
	 */

	private boolean checkAutoconnect(BluetoothAdapter btAdapter) {
		SettingsManager settings = new SettingsManager(this);
		boolean autoconnect = settings.getAutoconnect();

		String deviceMac = settings.getDeviceAddress();

		if (autoconnect && !mAutoconnected) {
			mAutoconnected = true;
			startActivity(new Intent(MainActivity.this, DZBController.class)
					.putExtra(DEVICE_MAC_KEY, deviceMac)); // .putExtra(autoconnect,
															// true));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	private void restartApp() {
		Intent mStartActivity = new Intent(this, MainActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(this,
				mPendingIntentId, mStartActivity,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
				mPendingIntent);
		System.exit(0);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings)
			startActivity(new Intent(this, SettingsMenu.class));
		return true;

	}

	private OnItemClickListener mPairedDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			String selectedBox = mPairedDevicesArrayAdapter.getItem(arg2);
			String selectedMacAddress = selectedBox.substring(
					selectedBox.lastIndexOf("\n")).replaceFirst("\n", "");
			// Toast.makeText(MainActivity.this, selectedMacAddress,
			// Toast.LENGTH_SHORT).show();
			startActivity(new Intent(MainActivity.this, DZBController.class)
					.putExtra(DEVICE_MAC_KEY, selectedMacAddress));
		}
	};

	private OnItemClickListener mAvailableDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			if (!mAvailableDevicesArrayAdapter.getItem(arg2).equals(
					getString(R.string.none_found))) {
				// OPEN PAIR REQUEST:
				String selectedBox = mAvailableDevicesArrayAdapter
						.getItem(arg2);
				String selectedMacAddress = selectedBox.substring(
						selectedBox.lastIndexOf("\n")).replaceFirst("\n", "");
				// Toast.makeText(MainActivity.this, selectedMacAddress,
				// Toast.LENGTH_LONG).show();

				// BluetoothDevice selectedDevice =
				// mBluetoothAdapter.getRemoteDevice(selectedMacAddress);

				startActivity(new Intent(MainActivity.this, DZBController.class)
						.putExtra(DEVICE_MAC_KEY, selectedMacAddress));

				// startActivity(new Intent(MainActivity.this,
				// DZBController.class).putExtra(DEVICE_MAC_KEY,));
			}
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

				// Put the device into the list for all available devices
				if (!checkDevice(device.getAddress(), mAllAvailableDevices))
					mAllAvailableDevices.add(device.getAddress());
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (!checkDevice(
							device.getName() + "\n" + device.getAddress(),
							mAvailableDevicesArrayAdapter)) {
						if (mAvailableDevicesArrayAdapter.getItem(0).toString()
								.equals(getString(R.string.none_found)))
							mAvailableDevicesArrayAdapter
									.remove(getString(R.string.none_found));
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
	private boolean checkDevice(String device,
			ArrayAdapter<String> deviceListAdapter) {
		boolean listed = false;
		for (int i = 0; i < deviceListAdapter.getCount(); i++) {
			if (deviceListAdapter.getItem(i).equals(device)) {
				listed = true;
			}
		}

		return listed;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_ENABLE_BT) {
			restartApp();
		}

	}
}