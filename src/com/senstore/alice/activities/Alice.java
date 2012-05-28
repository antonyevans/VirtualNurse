package com.senstore.alice.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.commonsware.cwac.locpoll.LocationReceiver;
import com.senstore.alice.R;
import com.senstore.alice.services.BackgroundLogger;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

public class Alice extends Activity {

	// 2 minutes(120000)
	// 5 minutes(300000)
	// 10 minutes(600000)
	// 30 minutes(1800000)
	private static final int PERIOD = 120000; // 2 minutes
	private PendingIntent pi = null;
	private AlarmManager mgr = null;
	ResponseReceiver receiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.main);

		// Register the Background Logger Broadcast Receiver
		initLogBroadcastReceiver();

		initLocationService();

		// Check if the app has been run before.
		if (isFirstRun()) {
			Log.i(Constants.TAG, "isFirstRun()");
			// Start a background Task, to register the current user/device
			doLog(Integer.toString(Constants.LOG_REGISTER));

		}// else proceed with the normal app flow

	}

	private void initLogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter(Constants.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);
	}

	private void initLocationService() {
		mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, LocationPoller.class);
		i.putExtra(LocationPoller.EXTRA_INTENT, new Intent(this,
				LocationReceiver.class));
		i.putExtra(LocationPoller.EXTRA_PROVIDER, LocationManager.GPS_PROVIDER);
		pi = PendingIntent.getBroadcast(this, 0, i, 0);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), PERIOD, pi);

		Log.i(Constants.TAG, "Location polling every 2 minutes begun");
	}

	private void setNotFirstRun() {
		// Save the state with shared preferences
		getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
				.putBoolean("firstRun", false).commit();
	}

	private boolean isFirstRun() {
		return getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean(
				"firstRun", true);
	}

	/**
	 * Creates an {@link Intent} that identifies the {@link BackgroundLogger}
	 * {@link Service} and puts a string extra on it.
	 * 
	 * @param log_type
	 */
	private void doLog(String log_type) {
		Intent msgIntent = new Intent(this, BackgroundLogger.class);
		msgIntent.putExtra(Constants.LOG_SERVICE_IN_MSG, log_type);
		startService(msgIntent);

	}

	@Override
	protected void onPause() {
		mgr.cancel(pi);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mgr.cancel(pi);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String text = intent.getStringExtra(Constants.LOG_SERVICE_OUT_MSG);

			Log.i(Constants.TAG, "onReceive under ResponseReceiver " + text);
			
			//Check if Log Type is register, and if so, mark is first run to false
			if(text.equalsIgnoreCase("1")){
				setNotFirstRun();
			}
			
		}
	}
}