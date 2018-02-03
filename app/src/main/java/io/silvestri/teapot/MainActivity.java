package io.silvestri.teapot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

	SeekBar seek_temp;
	SeekBar seek_strength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* *** init views *** */
		seek_temp = findViewById(R.id.seek_temperature);
		seek_strength = findViewById(R.id.seek_strength);

		/* *** intent extras *** */
		if (getIntent().getExtras() != null) {
			String str_temp = getIntent().getStringExtra("settings_temperature");
			int set_temp = Math.min(90, Math.max(30, Integer.valueOf(str_temp))) - 30;
			String str_strength = getIntent().getStringExtra("settings_concentration");
			int set_strength = Math.min(4, Math.max(0, Math.round(Float.valueOf(str_strength) * 4)));

			seek_temp.setProgress(set_temp);
			seek_strength.setProgress(set_strength);
		}

		/* *** event listeners *** */
		findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				makeTea();
			}
		});
		findViewById(R.id.buttonSchedule).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				scheduleTeaClicked();
			}
		});
		SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				changeSettings();
			}
		};
		// TODO show value on thumb
		seek_temp.setOnSeekBarChangeListener(seekBarChangeListener);
		seek_strength.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	@Override
	public void onBackPressed() {
		getWindow().setSharedElementReturnTransition(null);
		getWindow().setSharedElementReenterTransition(null);
		findViewById(R.id.image_tea).setTransitionName(null);

		super.onBackPressed();
	}

	private BroadcastReceiver teaBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra("state");
			if (!state.equals("ready")) {
				Intent newIntent = new Intent(MainActivity.this, ProgressActivity.class);
				newIntent.putExtras(intent);
				startActivity(newIntent);
			}
		}
	};

	private BroadcastReceiver errorBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String message = intent.getStringExtra("error");
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		// subscribe to broadcasts
		LocalBroadcastManager.getInstance(this).registerReceiver(teaBroadcastReceiver, new IntentFilter("tea"));
		LocalBroadcastManager.getInstance(this).registerReceiver(errorBroadcastReceiver, new IntentFilter("error"));
	}

	void changeSettings() {
		try {
			TeapotAPI.makeRequest("update_settings", makeSettingsJson(seek_temp.getProgress(), seek_strength.getProgress()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	void makeTea() {
		int temp = seek_temp.getProgress();
		int strength = seek_strength.getProgress();
		try {
			TeapotAPI.makeRequest("make_tea", makeSettingsJson(temp, strength));
		} catch (JSONException e) {
			Toast.makeText(MainActivity.this, "JSON error", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	static String makeSettingsJson(int temp, int strength) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("temperature", temp + 30);
		obj.put("concentration", strength / 4.0f);
		return obj.toString();
	}

	void scheduleTeaClicked() {
		// TODO implement scheduling tea
	}
}
