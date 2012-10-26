/**
 * 
 */
package com.senstore.alice.activities;

import java.util.Timer;
import java.util.TimerTask;

import com.senstore.alice.harvard.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Mimano Muthondu gmimano@bityarn.co.ke
 *
 */
public class SplashActivity extends Activity {
	
	private long splashDelay = 1500; //1.5 seconds
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// Set Full Screen Since we have a Tittle Bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
				
		
		//add layout for the splash screen
		setContentView(R.layout.splash_screen);
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				finish();
				Intent mainIntent = new Intent(SplashActivity.this, Alice.class);
				startActivity(mainIntent);
				
				
			}
		};
		
		Timer timer = new Timer();
		
		timer.schedule(task, this.splashDelay);
		
		
	}

}
