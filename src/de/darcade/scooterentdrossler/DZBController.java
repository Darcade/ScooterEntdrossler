package de.darcade.scooterentdrossler;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DZBController extends Activity {

	// Message types sent from the BluetoothReadService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	protected static final int UPDATE = 0;

	private String deviceMac;

	/**
	 * Used to temporarily hold data received from the remote process. Allocated
	 * once and used permanently to minimize heap thrashing.
	 */
	private byte[] mReceiveBuffer;

	private boolean changingName = false;

	private ByteQueue mByteQueue;

	private BluetoothAdapter btAdapter = null;
	private BluetoothSerialService mSerialService = null;

	private Button saveButton;
	private Button entdrosselButton;
	private EditText deviceNameEditor;
	public TextView deviceOutput = null;
	private ProgressBar connectBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dzbcontroller);

		mByteQueue = new ByteQueue(4 * 1024);
		mReceiveBuffer = new byte[4 * 1024];

		entdrosselButton = (Button) findViewById(R.id.entdrosslerButton);
		saveButton = (Button) findViewById(R.id.save_button);
		connectBar = (ProgressBar) findViewById(R.id.progressBar1);

		deviceNameEditor = (EditText) findViewById(R.id.deviceName_TextField);
		deviceOutput = (TextView) findViewById(R.id.deviceOutput);

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		deviceMac = getIntent().getExtras().getString(
				MainActivity.DEVICE_MAC_KEY);

		String deviceName = btAdapter.getRemoteDevice(deviceMac).getName();
		setTitle(deviceName);
		deviceNameEditor.setText(deviceName);

		mSerialService = new BluetoothSerialService(this, mHandlerBT, this);

		// Toast.makeText(this, deviceName , Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mSerialService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
				// Start the Bluetooth chat services
				// mSerialService.start();
				mSerialService.connect(btAdapter.getRemoteDevice(deviceMac));
				entdrosselButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mSerialService.write("1".getBytes());

					}
				});
				saveButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						changingName = true;
						deviceOutput.setText(deviceOutput.getText().toString()
								+ '\n' + getString(R.string.changing_name)
								+ '\n');
						connectBar.setVisibility(View.VISIBLE);
						mSerialService.write(new String("AT+NAME"
								+ deviceNameEditor.getText() + '\n').getBytes());
						mSerialService.stop();

						SystemClock.sleep(5000);
						mSerialService.start();
						mSerialService.connect(btAdapter
								.getRemoteDevice(deviceMac));
						connectBar.setVisibility(View.INVISIBLE);
					}
				});
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mSerialService != null)
			mSerialService.stop();
	}

	public void write(byte[] buffer, int length) {
		try {
			mByteQueue.write(buffer, 0, length);
		} catch (InterruptedException e) {
		}
		mHandler.sendMessage(mHandler.obtainMessage(UPDATE));
	}

	/*
	 * public void write(String message) {
	 * deviceOutput.setText(deviceOutput.getText() + message); }
	 */

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandlerBT = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				// if (DEBUG)
				// Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothSerialService.STATE_CONNECTED:
					/*
					 * if (mMenuItemConnect != null) {
					 * mMenuItemConnect.setIcon(android
					 * .R.drawable.ic_menu_close_clear_cancel);
					 * mMenuItemConnect.setTitle(R.string.disconnect); }
					 * mInputManager.showSoftInput(mEmulatorView,
					 * InputMethodManager.SHOW_IMPLICIT);
					 * mTitle.setText(R.string.title_connected_to);
					 * mTitle.append(" " + mConnectedDeviceName);
					 */

					connectBar.setVisibility(View.INVISIBLE);
					// setTitle(mConnectedDeviceName +
					// getString(R.string.connected));
					// Toast.makeText(DZBController.this,
					// msg.getData().getString(DEVICE_NAME),
					// Toast.LENGTH_LONG).show();
					break;
				case BluetoothSerialService.STATE_CONNECTING:
					setTitle(msg.getData().getString(DEVICE_NAME) + " (" + getString(R.string.connecting)
							+ ")");
					// mTitle.setText(R.string.title_connecting);
					connectBar.setVisibility(View.VISIBLE);
					break;
				case BluetoothSerialService.STATE_LISTEN:
				case BluetoothSerialService.STATE_NONE:
					/*
					 * if (mMenuItemConnect != null) { mMenuItemConnect
					 * .setIcon(android.R.drawable.ic_menu_search);
					 * mMenuItemConnect.setTitle(R.string.connect); }
					 * mInputManager.hideSoftInputFromWindow(
					 * mEmulatorView.getWindowToken(), 0);
					 * mTitle.setText(R.string.title_not_connected);
					 */
					break;
				}
				break;
			case MESSAGE_WRITE:
				/*
				 * if (mLocalEcho) { byte[] writeBuf = (byte[]) msg.obj;
				 * this.write(writeBuf, 0); }
				 */
				break;

			/*
			 * case MESSAGE_READ: byte[] readBuf = (byte[]) msg.obj; try {
			 * DZBController.this.write(new String(readBuf, "UTF-8")); } catch
			 * (UnsupportedEncodingException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); }
			 * 
			 * break;
			 */

			case MESSAGE_DEVICE_NAME:

				// save the connected device's name
				String mConnectedDeviceName = msg.getData().getString(
						DEVICE_NAME);
				// Toast.makeText(getApplicationContext(), mConnectedDeviceName,
				// Toast.LENGTH_SHORT) .show();
				setTitle(mConnectedDeviceName + " ("
						+ getString(R.string.connected) + ")");

				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				if (!changingName
						&& msg.getData()
								.getString(TOAST)
								.equals(getString(R.string.toast_unable_to_connect)))
					finish();
				break;
			}
		}
	};

	/**
	 * Our message handler class. Implements a periodic callback.
	 */
	private final Handler mHandler = new Handler() {
		/**
		 * Handle the callback message. Call our enclosing class's update
		 * method.
		 * 
		 * @param msg
		 *            The callback message.
		 */
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE) {
				update();
			}
		}
	};

	/**
	 * Look for new input from the ptty, send it to the terminal emulator.
	 */
	private void update() {
		int bytesAvailable = mByteQueue.getBytesAvailable();
		int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
		try {
			int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
			String stringRead = new String(mReceiveBuffer, 0, bytesRead);
			deviceOutput.setText(deviceOutput.getText() + stringRead);
		} catch (InterruptedException e) {
		}
	}

}

/**
 * A multi-thread-safe produce-consumer byte array. Only allows one producer and
 * one consumer.
 */
class ByteQueue {
	public ByteQueue(int size) {
		mBuffer = new byte[size];
	}

	public int getBytesAvailable() {
		synchronized (this) {
			return mStoredBytes;
		}
	}

	public int read(byte[] buffer, int offset, int length)
			throws InterruptedException {
		if (length + offset > buffer.length) {
			throw new IllegalArgumentException(
					"length + offset > buffer.length");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		if (length == 0) {
			return 0;
		}
		synchronized (this) {
			while (mStoredBytes == 0) {
				wait();
			}
			int totalRead = 0;
			int bufferLength = mBuffer.length;
			boolean wasFull = bufferLength == mStoredBytes;
			while (length > 0 && mStoredBytes > 0) {
				int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
				int bytesToCopy = Math.min(length, oneRun);
				System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
				mHead += bytesToCopy;
				if (mHead >= bufferLength) {
					mHead = 0;
				}
				mStoredBytes -= bytesToCopy;
				length -= bytesToCopy;
				offset += bytesToCopy;
				totalRead += bytesToCopy;
			}
			if (wasFull) {
				notify();
			}
			return totalRead;
		}
	}

	public void write(byte[] buffer, int offset, int length)
			throws InterruptedException {
		if (length + offset > buffer.length) {
			throw new IllegalArgumentException(
					"length + offset > buffer.length");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length < 0");
		}
		if (length == 0) {
			return;
		}
		synchronized (this) {
			int bufferLength = mBuffer.length;
			boolean wasEmpty = mStoredBytes == 0;
			while (length > 0) {
				while (bufferLength == mStoredBytes) {
					wait();
				}
				int tail = mHead + mStoredBytes;
				int oneRun;
				if (tail >= bufferLength) {
					tail = tail - bufferLength;
					oneRun = mHead - tail;
				} else {
					oneRun = bufferLength - tail;
				}
				int bytesToCopy = Math.min(oneRun, length);
				System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
				offset += bytesToCopy;
				mStoredBytes += bytesToCopy;
				length -= bytesToCopy;
			}
			if (wasEmpty) {
				notify();
			}
		}
	}

	private byte[] mBuffer;
	private int mHead;
	private int mStoredBytes;
}
