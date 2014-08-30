package de.darcade.scooterentdrossler;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainController extends Activity {

	protected static final int RECIEVE_MESSAGE = 0;
	BluetoothDevice device;
	BluetoothAdapter btAdapter;

	TextView devicelabel, btoutput;
	Button signalBtn, saveName;
	EditText devicename_field;

	BluetoothManager btManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_controller);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		device = btAdapter.getRemoteDevice(getIntent().getExtras().getString(
				"device-address"));

		// init();
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
			btManager.pauseSocket();
			startActivity(new Intent(this, SettingsMenu.class));
		return true;

	}

	private void init() {
		devicelabel = (TextView) findViewById(R.id.device_label);
		btoutput = (TextView) findViewById(R.id.btoutput_label);
		btoutput.setMovementMethod(new ScrollingMovementMethod());
		signalBtn = (Button) findViewById(R.id.signal_button);
		saveName = (Button) findViewById(R.id.save_device_name);
		devicename_field = (EditText) findViewById(R.id.device_name);

		devicelabel.setText(device.getName());
		devicename_field.setText(device.getName());

		btManager = new BluetoothManager(this);

		saveName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btManager.changeDeviceName(devicename_field.getText()
						.toString(), device);
				btManager.pauseSocket();
				Toast.makeText(MainController.this, getString(R.string.changing_name), Toast.LENGTH_LONG).show();
				SystemClock.sleep(5000);
				btManager.setupSocket(device);

			}
		});

		signalBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btManager.sendOn(device);

			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		init();
		btManager.setupSocket(device);
		btManager.getVersion(device);
	}

	@Override
	public void onPause() {
		super.onPause();
		 btManager.pauseSocket();

	}
}
