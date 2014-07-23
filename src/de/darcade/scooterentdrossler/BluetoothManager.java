package de.darcade.scooterentdrossler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothManager {
	private static final String TAG = "BluetoothManager";

	protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
	
    
    MainController controller;
	TextView txtArduino;
	String tag = "debugging";
	Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch(msg.what){
            case SUCCESS_CONNECT:
                // DO something
                ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                Toast.makeText(controller.getApplicationContext(), "CONNECT", 0).show();
                String s = "successfully connected";
                connectedThread.write(s);
                Log.i(tag, "connected");
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[])msg.obj;
                String string = new String(readBuf);
                controller.btoutput.setMovementMethod(new ScrollingMovementMethod()); 
                controller.btoutput.setText(controller.btoutput.getText() + string + "\n");
                scrollToBottom();
                //Toast.makeText(controller.getApplicationContext(), string, 0).show();
                break;
            }
        }
    };
	
	private static char STX=2,ETX=3;

	final int RECIEVE_MESSAGE = 1; // Status for Handler
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;

	private ConnectedThread mConnectedThread;

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public BluetoothManager(MainController controller) {
		this.controller = controller;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	public void sendOn() {
		mConnectedThread.write("1");
	}

	public void sendOff() {
		mConnectedThread.write("0");
	}
	
	public void getVersion() {
		mConnectedThread.write("?");
	}
	
	public void changeDeviceName(String newdevicename){
		mConnectedThread.write(STX+"AT+NAME"+newdevicename+ETX);
	}

	public void setupSocket(BluetoothDevice device) {

		// Log.d(TAG, "...onResume - try connect...");

		// Set up a pointer to the remote node using it's address.
		// BluetoothDevice device =
		// btAdapter.getRemoteDevice(device.getAddress());

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			// errorExit("Fatal Error",
			// "In onResume() and socket create failed: " + e.getMessage() +
			// ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				// errorExit("Fatal Error",
				// "In onResume() and unable to close socket during connection failure"
				// + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();
	}

	public void pauseSocket() {
		// Log.d(TAG, "...In onPause()...");

		try {
			btSocket.close();
		} catch (IOException e2) {
			// errorExit("Fatal Error",
			// "In onPause() and failed to close socket." + e2.getMessage() +
			// ".");
		}
	}

	

	private void scrollToBottom()
	{
		final TextView mTextStatus = (TextView) controller.findViewById(R.id.btoutput_label);
		final ScrollView mScrollView = (ScrollView) controller.findViewById(R.id.SCROLLER_ID);
	    mScrollView.post(new Runnable()
	    { 
	        public void run()
	        { 
	            mScrollView.smoothScrollTo(0, mTextStatus.getBottom());
	        } 
	    });
	}
	
	
	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer); // Get number of bytes and
														// message in "buffer"
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget(); // Send to message queue Handler
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {
			Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
			}
		}
	}
	
	
}