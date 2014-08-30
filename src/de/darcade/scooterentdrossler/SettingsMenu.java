package de.darcade.scooterentdrossler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsMenu extends Activity {

	TextView device_label;
	CheckBox autostart_box;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_menu);

		device_label = (TextView) findViewById(R.id.autoconnect_device_label);
		autostart_box = (CheckBox) findViewById(R.id.autoconnect_box);

		SettingsManager settings = new SettingsManager(this);
		String device_address = settings.getDeviceAddress();
		String device_name = settings.getDeviceName();
		boolean autostart = settings.getAutoconnect();

		if (autostart) {
			autostart_box.setChecked(autostart);
		}

		if (device_address != "") {
			device_label.setText(device_name);
		}

		device_label.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingsMenu.this, DeviceChooser.class));
			}
		});

		autostart_box
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						SettingsManager settings = new SettingsManager(
								SettingsMenu.this);
						if (settings.getDeviceAddress() != "") {
							settings.setAutoconnect(isChecked);
						} else {
							Toast.makeText(SettingsMenu.this ,getString(R.string.first_set_device) , Toast.LENGTH_LONG).show();
						}

					}
				});

	}
	
	public void onResume(){
		super.onResume();
		
		SettingsManager settings = new SettingsManager(this);
		String device_address = settings.getDeviceAddress();
		String device_name = settings.getDeviceName();
		boolean autostart = settings.getAutoconnect();

		if (autostart) {
			autostart_box.setChecked(autostart);
		}

		if (device_address != "") {
			device_label.setText(device_name);
		}
		
	}
}
