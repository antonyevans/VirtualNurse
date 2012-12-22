/**
 * 
 */
package com.senstore.alice.activities;

import java.util.Timer;
import java.util.TimerTask;

import com.flurry.android.FlurryAgent;
import com.senstore.alice.harvard.R;
import com.senstore.alice.services.MyPrefsBackupAgent;
import com.senstore.alice.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Mimano Muthondu gmimano@bityarn.co.ke
 *
 */
public class SplashActivity extends Activity {
	private boolean agreeTCs = false;
	
	private BackupManager mBackupManager;
	
	private long splashDelay = 1500; //1.5 seconds
	
	private void ask_accept_TCs() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Terms & Conditions");
		builder.setMessage("By clicking accept you agree to the Terms and Conditions and our Privacy Policy")
		       .setCancelable(false)
		       .setNegativeButton("Agree", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("Agreed to T&Cs");
		        	   agreeTCs = true;
		        	   
		        	   //save the preferences
		        	   SharedPreferences preferences = getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE);
		        	   SharedPreferences.Editor editor = preferences.edit();
		       		   editor.putBoolean("agreeTCs", agreeTCs); // value to store
		       		   editor.commit();
		       		   
		       		   //backup the changes
		       		   mBackupManager.dataChanged();
		       		   
		       		   Intent mainIntent = new Intent(SplashActivity.this, Alice.class);
		       		   startActivity(mainIntent);
		           }
		       })
		       .setPositiveButton("Disagree (quit)", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   agreeTCs = false;
		        	   FlurryAgent.logEvent("Rejected the T&Cs");
		        	   finish();
		           }
		       });
		AlertDialog askTCsDialog = builder.create();
		askTCsDialog.show(); 
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//load preferences file
		mBackupManager = new BackupManager(this);
		
		//the next lines are not required
		/*mBackupManager.requestRestore( new RestoreObserver()
		{
			@Override
			public void restoreStarting( int numPackages )
			{
				Log.d( Constants.TAG, "restoreStarting: " + numPackages );
				super.restoreStarting( numPackages );
			}

			@Override
			public void restoreFinished( int error )
			{
				Log.d( Constants.TAG, "restoreFinished: " + error );
				super.restoreFinished( error );
			}

			@Override
			public void onUpdate( int nowBeingRestored, String currentPackage )
			{
				Log.d( Constants.TAG, "onUpdate: " + currentPackage );
				super.onUpdate( nowBeingRestored, currentPackage );
			}
		} );*/
		
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
		
		//load the sharedpreferences
		SharedPreferences preferences = getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE);
		agreeTCs = preferences.getBoolean("agreeTCs", false);
		
		if (agreeTCs) {
			Timer timer = new Timer();
			timer.schedule(task, this.splashDelay);
		} else {
			ask_accept_TCs();
		};
		
		
	}

}
