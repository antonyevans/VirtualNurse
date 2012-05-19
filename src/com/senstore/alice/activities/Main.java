package com.senstore.alice.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.senstore.alice.R;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

public class Main extends Activity {

	//2 minutes(120000)
	//5 minutes(300000)
	//10 minutes(600000)
	//30 minutes(1800000)
	private static final int PERIOD = 120000; // 2 minutes
	private PendingIntent pi = null;
	private AlarmManager mgr = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.main);

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
		super.onDestroy();
	}
}