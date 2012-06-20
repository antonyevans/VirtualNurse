/**
 * 
 */
package com.senstore.alice.test;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.senstore.alice.R;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class APITester extends Activity {
	// 2 minutes(120000)
	// 5 minutes(300000)
	// 10 minutes(600000)
	// 30 minutes(1800000)
	private static final int PERIOD = 120000; // 2 minutes
	private PendingIntent pi = null;
	private AlarmManager mgr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.api_test_main);
		
		
		((Button) findViewById(R.id.button_logger))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(APITester.this,
								LoggingAPITester.class));
					}
				});
		((Button) findViewById(R.id.button_diagnosis))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(APITester.this,
								DiagnosisAPITester.class));
					}
				});

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
