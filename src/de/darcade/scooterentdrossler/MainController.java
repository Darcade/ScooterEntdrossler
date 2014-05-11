package de.darcade.scooterentdrossler;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainController extends Activity {

	protected static final int RECIEVE_MESSAGE = 0;
	BluetoothDevice device;
	BluetoothAdapter btAdapter;

	private StringBuilder sb = new StringBuilder();
	
	TextView devicelabel;
	Button signalBtn;
	EditText devicename_field;
	
	BluetoothManager btManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_controller);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		device = btAdapter.getRemoteDevice(getIntent().getExtras().getString(
				"device-address"));

		//init();
	}

	private void init() {
		devicelabel = (TextView) findViewById(R.id.device_label);
		signalBtn = (Button) findViewById(R.id.signal_button);
		devicename_field = (EditText) findViewById(R.id.device_name);
		
		devicelabel.setText(device.getName());
		devicename_field.setText(device.getName());

		Handler h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE: // if receive massage
					/*byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1); // create
																		// string
																		// from
																		// bytes
																		// array
					sb.append(strIncom); // append string
					int endOfLineIndex = sb.indexOf("\r\n"); // determine the
																// end-of-line
					if (endOfLineIndex > 0) { // if end-of-line,
						String sbprint = sb.substring(0, endOfLineIndex); // extract
																			// string
						sb.delete(0, sb.length()); // and clear
						txtArduino.setText("Data from Arduino: " + sbprint); // update
																				// TextView
						btnOff.setEnabled(true);
						btnOn.setEnabled(true);
					}
					// Log.d(TAG, "...String:"+ sb.toString() + "Byte:" +
					// msg.arg1 + "...");*/
					break;
				}
			};
		};

		btManager = new BluetoothManager(h);
		
		
		signalBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btManager.sendOn();
		         
			}
		});
		
		
		
	}
	
	
	
	
	  @Override
	  public void onResume() {
	    super.onResume();
	    init();
	    btManager.setupSocket(device);
	  }
	  
	  @Override
	  public void onPause() {
	    super.onPause();
	  //btManager.pauseSocket();

	  }
}
