package com.senstore.alice.activities;

import android.app.Activity;
import android.os.Bundle;

import com.senstore.alice.R;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

public class Main extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.main);
	}
}