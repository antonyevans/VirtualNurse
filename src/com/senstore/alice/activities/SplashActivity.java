/**
 * 
 */
package com.senstore.alice.activities;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

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
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

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
		
		String msg = "By clicking 'Accept' you agree to our <b><a href=\"http://www.senstore.com/tcs\">Terms and Conditions</a></b> and <b><a href=\"http://www.senstore.com/privacy\">Privacy Policy</a></b>";
		
		builder.setMessage(Html.fromHtml(msg))
		       .setCancelable(false)
		       .setNegativeButton("Accept", new DialogInterface.OnClickListener() {
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
		
		// Make the textview clickable. 
	    ((TextView)askTCsDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//load preferences file
		mBackupManager = new BackupManager(this);
		
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
