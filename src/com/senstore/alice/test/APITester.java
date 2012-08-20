/**
 * 
 */
package com.senstore.alice.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.senstore.alice.test.R;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class APITester extends Activity {

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
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
