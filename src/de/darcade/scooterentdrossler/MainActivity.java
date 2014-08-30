package de.darcade.scooterentdrossler;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends Activity implements OnItemClickListener {

	ArrayAdapter<String> listAdapter;
	ListView listView;
	Button reloadButton;
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	public static final UUID MY_UUID = UUID
			.fromString("f9c25060-d307-11e3-9c1a-0800200c9a66");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	IntentFilter filter;
	BroadcastReceiver receiver;
	String tag = "debugging";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		reloadButton = (Button) findViewById(R.id.reload_button);
		init();
		
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No bluetooth detected",
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			if (!btAdapter.isEnabled()) {
				turnOnBT();
			}
			reloadButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					removeItems();
					unregisterReceiver(receiver);
					init();
					getPairedDevices();
					startDiscovery();
					
				}
			});
			getPairedDevices();
			startDiscovery();
		}
		checkautoconnect();

		

	}

	private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();

	}

	private void checkautoconnect() {
		SettingsManager settings = new SettingsManager(this);

		if (settings.getAutoconnect()) {
			startActivity(new Intent(this, MainController.class).putExtra(
					"device-address", settings.getDeviceAddress()));

		}
	}

	private void turnOnBT() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}

	private void getPairedDevices() {
		// TODO Auto-generated method stub
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size() > 0) {
			for (BluetoothDevice device : devicesArray) {
				pairedDevices.add(device.getName());

			}
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 0);
		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for (int a = 0; a < pairedDevices.size(); a++) {
						if (device.getName().equals(pairedDevices.get(a))) {
							// append
							s = "(Paired)";
							break;
						}
					}

					listAdapter.add(device.getName() + " " + s + " " + "\n"
							+ device.getAddress());
				}

				else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {
					// run some code
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {
					// run some code

				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					if (btAdapter.getState() == btAdapter.STATE_OFF) {
						turnOnBT();
					}
				}

			}
		};

		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth must be enabled to continue", Toast.LENGTH_SHORT)
					.show();
			finish();
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		if (listAdapter.getItem(arg2).contains("Paired")) {

			BluetoothDevice selectedDevice = devices.get(arg2);
			Toast.makeText(getApplicationContext(),
					"Device " + selectedDevice.getName() + " selected",
					Toast.LENGTH_SHORT).show();

			startActivity(new Intent(this, MainController.class).putExtra(
					"device-address", selectedDevice.getAddress()));

			Log.i(tag, "in click listener");
		} else {
			Toast.makeText(getApplicationContext(), "device is not paired",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void removeItems(){
		listView.setAdapter(null);		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings)
			startActivity(new Intent(this, SettingsMenu.class));
		return true;

	}
	
}