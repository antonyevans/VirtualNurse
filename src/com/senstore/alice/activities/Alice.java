package com.senstore.alice.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.senstore.alice.harvard.R;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.location.AliceLocation;
import com.senstore.alice.location.AliceLocation.LocationResult;
import com.senstore.alice.menus.MenuGuide;
import com.senstore.alice.menus.CategoryGuide;
import com.senstore.alice.menus.BodyGuide;
import com.senstore.alice.menus.DemographicGuide;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.models.Guide;
import com.senstore.alice.services.BackgroundLogger;
import com.senstore.alice.services.MyPrefsBackupAgent;
import com.senstore.alice.services.BillingService.RequestPurchase;
import com.senstore.alice.tasks.DiagnosisAsyncTask;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;
import com.senstore.alice.utils.Utils;
import com.senstore.alice.views.ChatListView;
import com.senstore.alice.services.BillingService;
import com.senstore.alice.billing.PurchaseObserver;
import com.senstore.alice.utils.Constants.PurchaseState;
import com.senstore.alice.utils.Constants.ResponseCode;
import com.senstore.alice.billing.ResponseHandler;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.flurry.android.FlurryAgent;

public class Alice extends Activity implements AsyncTasksListener,
		TextToSpeech.OnInitListener {

	private BackupManager mBackupManager;
	
	//Variables for licensing
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private boolean bRetry = true;
	
	private boolean canCallDoctor = false;
	private TextToSpeech mTts;
	private boolean isTTSReady = false;

	private ResponseReceiver receiver;
	private String chatQuery = null;

	private String prevCurQuery = null;

	private static final int DIAGNOSIS_DIALOG = 0;
	private static final int LISTENING_DIALOG = 1;
	private static final int BILLING_NOT_WORKING_DIALOG = 2;
	private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 3;	
	private static final int BILLING_WORKING_DIALOG = 4;
	
	//constants for the orange buttons
	private static final int GUIDE = 0;
	private static final int MENU = 1;
	private static final int CATEGORY = 2;
	private static final int BODY = 3;
	private static final int DEMOGRAPHIC = 4;
	private static final int OWNED = 5;
	private static final int ALL = 6;
	private static final int HOME = 7;

	final AsyncTasksListener listener = this;

	private DiagnosisAsyncTask diagnosisTask;

	private View chatView;
	private ChatListView chatlist;
	private ViewFlipper flipper;

	private AliceChatAdapter chatAdapter;

	private View menuView;
	private View categoryView;
	private View guideView;
	//private View aboutView;
	private View partnerView;
	private View infoView;
	private View textinputView;

	private ListView list;
    private List<String> List_file;
	
	private LayoutInflater inflater;

	private Diagnosis mDiagnosis;

	private static SpeechKit _speechKit;
	private Handler _handler = null;
	private final Recognizer.Listener _listener;
	private Recognizer _currentRecognizer;
	private ListeningDialog _listeningDialog;
	private DiagnosisDialog _diagnosisDialog;
	private boolean _destroyed;

	private Vocalizer _vocalizer;
	private Object _lastTtsContext = null;

	private Drawable mic_start;
	private Drawable mic_stop;
	private ImageButton mic_action;

	//preferences variables
	private boolean canTalk = false;
	private boolean everMute = false;
	private int usageCount = 0;

	private String talkResp = "";

	private boolean isFirstTime = true;
	
	private BillingService mBillingService;
	private AlicePurchaseObserver mAlicePurchaseObserver;
	private Handler mHandler;

	public Alice() {
		super();
		_listener = createListener();
		_currentRecognizer = null;
		_listeningDialog = null;
		_diagnosisDialog = null;
		_destroyed = true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(Constants.TAG,"Oncreate");
		//create backupmanager
		mBackupManager = new BackupManager(this);
		
		//setup licensing
		
		
		// Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(AppInfo.SALT, getPackageName(), Utils.getUserID(this))),
            AppInfo.BASE64_PUBLIC_KEY);
        doLicenseCheck(); 
		
		// Set Full Screen Since we have a Tittle Bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		initAliceLocation();
		
		mBillingService = new BillingService();
        mBillingService.setContext(this);
        mHandler = new Handler();
        mAlicePurchaseObserver = new AlicePurchaseObserver(mHandler);
        ResponseHandler.register(mAlicePurchaseObserver);

		setContentView(R.layout.main);

		mic_action = (ImageButton) findViewById(R.id.action_mic);
		inflater = getLayoutInflater();

		inflater = getLayoutInflater();

		mic_start = getResources().getDrawable(R.drawable.mic_start);
		mic_stop = getResources().getDrawable(R.drawable.mic_stop);

		// inflate flipper to switch between menu and chat screen
		flipper = (ViewFlipper) findViewById(R.id.alice_view_flipper);

		if (savedInstanceState != null) {
			int flipperPosition = savedInstanceState.getInt("FLIPPER_POSITION");
			flipper.setDisplayedChild(flipperPosition);
		}

		// inflate the view with the listview
		chatView = inflater.inflate(R.layout.alice_chat_list_layout, null);

		// load listview
		chatlist = (ChatListView) chatView.findViewById(R.id.alice_chat_list);
		chatlist.setFocusable(false);

		chatAdapter = new AliceChatAdapter(this);

		// List refresh code
		chatlist.destroyDrawingCache();
		chatlist.setVisibility(ListView.INVISIBLE);
		chatlist.setVisibility(ListView.VISIBLE);

		chatlist.setAdapter(chatAdapter);

		// Load the main menu
		createMenuWidget(HOME, null, "","");
		
		// Restore preferences
        SharedPreferences settings = getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE);
        canTalk = settings.getBoolean("canTalk", false);
        everMute = settings.getBoolean("everMute", false);
        setTalkSettings();
        usageCount = settings.getInt("usageCount", 0);
	       
		// Register the Background Logger Broadcast Receiver
		initLogBroadcastReceiver();

		// Initialize the listening dialog
		createDiagnosisDialog();

		// Check if the app has been run before.
		if (isFirstRun()) {
			//Log.i(Constants.TAG, "isFirstRun()");
			// Start a background Task, to register the current user/device
			doLog(Integer.toString(Constants.LOG_REGISTER));

		}// else proceed with the normal app flow

		initAndroidTTS();

		// set volume control to media
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// If this Activity is being recreated due to a config change (e.g.
		// screen rotation), check for the saved SpeechKit instance.
		_speechKit = (SpeechKit) getLastNonConfigurationInstance();
		if (_speechKit == null) {
			_speechKit = SpeechKit.initialize(getApplication()
					.getApplicationContext(), AppInfo.SpeechKitAppId,
					AppInfo.SpeechKitServer, AppInfo.SpeechKitPort,
					AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
			_speechKit.connect();
			// Keep an eye out for audio prompts not working on the Droid
			// 2 or other 2.2 devices.
			Prompt beep = _speechKit.defineAudioPrompt(R.raw.beep);
			_speechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100),
					null, null);
		}
		_destroyed = false;

		// Initialize the listening dialog
		createListeningDialog();

		// Create Vocalizer listener
		initVocalizer();

		SavedState savedState = (SavedState) getLastNonConfigurationInstance();
		if (savedState == null) {
			// Initialize the handler, for access to this application's message
			// queue
			_handler = new Handler();
		} else {
			// There was a recognition in progress when the OS destroyed/
			// recreated this activity, so restore the existing recognition
			_currentRecognizer = savedState.Recognizer;
			_listeningDialog.setText(savedState.DialogText);
			_listeningDialog.setRecording(savedState.DialogRecording);
			_handler = savedState.Handler;

			if (savedState.DialogRecording) {
				// Simulate onRecordingBegin() to start animation
				_listener.onRecordingBegin(_currentRecognizer);
			}

			_currentRecognizer.setListener(_listener);

		}
		
		
		//show rate it dialog on the RATEIT time someone uses the app
		if (usageCount == 0) {
			FlurryAgent.logEvent("Show How to dialog");			
			showHowTo();
		} else if (usageCount == Constants.RATE_IT) {
			FlurryAgent.logEvent("Show Rate it!");
			rateItDialog();
		} else if (usageCount == Constants.SHARE_IT) {
			FlurryAgent.logEvent("Ask Share it!");
			askShareApp();
		}
		

	}

	private void doLicenseCheck() {
        
        setProgressBarIndeterminateVisibility(true);
        mChecker.checkAccess(mLicenseCheckerCallback);
    }
	
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            FlurryAgent.logEvent("License Allowed");
            //Toast.makeText(getApplicationContext(), "License Permission Granted", Toast.LENGTH_SHORT).show();
        }

        public void dontAllow(int policyReason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
            FlurryAgent.logEvent("License Not Allowed");
            //Toast.makeText(getApplicationContext(), "License Not Allowed", Toast.LENGTH_LONG).show();
            showNotLicensedDialog();
        }

        public void applicationError(int errorCode) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
            //Toast.makeText(getApplicationContext(), "License Error", Toast.LENGTH_LONG).show();
            FlurryAgent.onError("Licensing Error","Licensing Application Error",Integer.toString(errorCode));
        }
    }

    private void showNotLicensedDialog() {
    	AlertDialog.Builder notAllowedBuilder = new AlertDialog.Builder(this);
        notAllowedBuilder.setTitle("Unlicensed Application")
            .setCancelable(false)
        	.setMessage(bRetry ? "Error with application license, would you like to retry?" : "Please download a new version of the app from Google Play")
            .setNegativeButton(bRetry ? "Retry License" : "Download App", new DialogInterface.OnClickListener() {
                boolean mRetry = bRetry;
                public void onClick(DialogInterface dialog, int which) {
                    if ( mRetry ) {
                    	FlurryAgent.logEvent("Retry License");
                    	bRetry = false;
                        doLicenseCheck();
                    } else {
                    	FlurryAgent.logEvent("License Failed - going to Play");
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://play.google.com/store/apps/details?id=" + getPackageName()));
                            startActivity(marketIntent);                        
                    }
                }
            })
            .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	FlurryAgent.logEvent("License Failed - Quit");
                	finish();
                }
            }).create();
        notAllowedBuilder.show();
    }
	
	private void initAliceLocation() {
		AliceLocation myLocation = new AliceLocation();
		myLocation.getLocation(this, locationResult);
	}

	private void initAndroidTTS() {
		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		mTts = new TextToSpeech(this, this // TextToSpeech.OnInitListener
		);

	}

	public void initVocalizer() {
		Vocalizer.Listener vocalizerListener = new Vocalizer.Listener() {

			public void onSpeakingBegin(Vocalizer vocalizer, String text,
					Object context) {

			}

			public void onSpeakingDone(Vocalizer vocalizer, String text,
					SpeechError error, Object context) {
				// Use the context to detemine if this was the final TTS phrase
				if (context != _lastTtsContext) {

				} else {

				}
			}
		};
		_vocalizer = Alice.getSpeechKit().createVocalizerWithLanguage("en_US",
				vocalizerListener, new Handler());
		_vocalizer.setVoice("Serena");
	}

	public void setTalkSettings() {
		if (canTalk) {
			mTts = new TextToSpeech(this, this // TextToSpeech.OnInitListener
			);
			mic_action.setImageDrawable(mic_start);

		} else {

			if (mTts != null) {
				mTts.stop();
				mTts.shutdown();
			}			
			mTts = null;
			mic_action.setImageDrawable(mic_stop);

		}
	}
	
	public void toggleTalk(View view) {
		//usage tracking
		everMute = true;
		String talkStatus = null;
		if (canTalk) {
			talkStatus = "Mute";
		} else {
			talkStatus = "Enable";
		}
		Map<String, String> flurryParams = new HashMap<String, String>();
        	flurryParams.put("Talk status", talkStatus); // Capture status
        
		FlurryAgent.logEvent("toggleTalk", flurryParams);
		
		// do toggling
		canTalk = !canTalk;
		setTalkSettings();
		
	}

	public void onHome(View view) {
		//usage tracking
		FlurryAgent.logEvent("onHome");
		
		// identify the view on display currently
		View currentView = flipper.getCurrentView();

		while (!currentView.equals(menuView)) {

			if (currentView.equals(chatView)) {
				// moving to Home View. Clean the Chat list and remove the chat view
				chatAdapter.resetAdapter();
				flipper.removeView(currentView);
			} else {
				flipper.removeView(currentView);
			}
			currentView = flipper.getCurrentView();
		} 
		
		stopTTS();
		
		int chcount = flipper.getChildCount();
		
		if (chcount==1) {
			prevCurQuery = Constants.DIAGNOSIS_DEFAULT_LAST_QUERY;
		}
		
		// Speak after returning to Home View
		// Removed for now

	}

	public void onInfo(View view) {
		//usage tracking
		FlurryAgent.logEvent("onInfo");
        
		View currentView = flipper.getCurrentView();
		if (!currentView.equals(infoView)) {
			
			infoView = inflater.inflate(R.layout.info_screen, null);
			flipper.addView(infoView);
			flipper.showNext();
			
			List_file = new ArrayList<String>();
	        list = (ListView)findViewById(R.id.infolist);
	        
	        List_file.add(getString(R.string.info_howto));
	        List_file.add(getString(R.string.info_about));
	        List_file.add(getString(R.string.info_partners));
	        List_file.add(getString(R.string.info_TCs));
	        List_file.add(getString(R.string.info_privacy));
	        List_file.add(getString(R.string.info_share));
	        List_file.add(getString(R.string.info_rate));
	        List_file.add(getString(R.string.info_contact));
	           
	        list.setAdapter(new ArrayAdapter<String>(this, R.layout.info_item, List_file));
	        
	        list.setOnItemClickListener(new OnItemClickListener() {	       
				@Override
				public void onItemClick(AdapterView<?> arg0, View view,
						int arg2, long arg3) {
					// selected item
		            String product = ((TextView) view).getText().toString();
		            infoClickResponse(product);
				};
	        });
	        
			
		}
		stopTTS();
		
/*		Button shareBtnAbout = (Button) aboutView.findViewById(R.id.share_app);
		
		shareBtnAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//event tracking
				    		
	    		FlurryAgent.logEvent("Share App Btn pressed");
	    		shareApp();
	    		
			}

		});*/

	}
	
	public void infoClickResponse(String selection) {
		if (selection == getString(R.string.info_about) ) {
			FlurryAgent.logEvent("Info: About");
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(textinputView)) {
				textinputView = inflater.inflate(R.layout.text_screen, null);
				flipper.addView(textinputView);
				flipper.showNext();
			}
			TextView text_inputTxt = (TextView)findViewById(R.id.text_input);
	        text_inputTxt.setText(Html.fromHtml(readTxt("About")));
			Linkify.addLinks(text_inputTxt, 15); //all become links
			
		} else if (selection == getString(R.string.info_howto) ) {
			FlurryAgent.logEvent("Info: How to");
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(textinputView)) {
				textinputView = inflater.inflate(R.layout.text_screen, null);
				flipper.addView(textinputView);
				flipper.showNext();
			}
			TextView text_inputTxt = (TextView)findViewById(R.id.text_input);
	        text_inputTxt.setText(Html.fromHtml(readTxt("HowTo")));
			Linkify.addLinks(text_inputTxt, 15); //all become links
			
		} else if (selection == getString(R.string.info_partners)) {
			FlurryAgent.logEvent("Info: Partners");
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(partnerView)) {
				partnerView = inflater.inflate(R.layout.partner_screen, null);
				flipper.addView(partnerView);
				flipper.showNext();
			}
		} else if (selection == getString(R.string.info_TCs)) {
			FlurryAgent.logEvent("Info: T&Cs");
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(textinputView)) {
				textinputView = inflater.inflate(R.layout.text_screen, null);
				flipper.addView(textinputView);
				flipper.showNext();
			}
			TextView text_inputTxt = (TextView)findViewById(R.id.text_input);
	        text_inputTxt.setText(Html.fromHtml(readTxt("TCs")));
			
		} else if (selection == getString(R.string.info_privacy)) {
			FlurryAgent.logEvent("Info: Privacy");
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(textinputView)) {
				textinputView = inflater.inflate(R.layout.text_screen, null);
				flipper.addView(textinputView);
				flipper.showNext();
			}
			TextView text_inputTxt = (TextView)findViewById(R.id.text_input);
	        text_inputTxt.setText(Html.fromHtml(readTxt("Privacy")));
			
		} else if (selection == getString(R.string.info_share)) {
			FlurryAgent.logEvent("Info: Share");
			shareApp("Info: Share");
		} else if (selection == getString(R.string.info_rate)) {
			FlurryAgent.logEvent("Info: Rate");
			rateIt();
		} else if (selection == getString(R.string.info_contact)) {
			FlurryAgent.logEvent("Info: Contact"); 
			
			View currentView = flipper.getCurrentView();
			if (!currentView.equals(textinputView)) {
				textinputView = inflater.inflate(R.layout.text_screen, null);
				flipper.addView(textinputView);
				flipper.showNext();
			}
			TextView text_inputTxt = (TextView)findViewById(R.id.text_input);
	        text_inputTxt.setText(Html.fromHtml(readTxt("Contact")));
			Linkify.addLinks(text_inputTxt, 15); //all become links
		}
	}
	
	private String readTxt(String textFile)	{

     InputStream inputStream;
     
     if (textFile == "TCs") {
    	 inputStream = getResources().openRawResource(R.raw.tcs);
     } else if (textFile == "Privacy") {
    	 inputStream = getResources().openRawResource(R.raw.privacy);
     } else if (textFile == "Contact") {
        	 inputStream = getResources().openRawResource(R.raw.contact);    
     } else if (textFile == "Hello") {
    	 inputStream = getResources().openRawResource(R.raw.hello);
     } else if (textFile == "About") {
    	 inputStream = getResources().openRawResource(R.raw.about);
     } else if (textFile == "HowTo") {
    	 inputStream = getResources().openRawResource(R.raw.howto);
     } else {
    	 return "TextFile not found";
     }
     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
     
     int i;
	  try {
	   i = inputStream.read();
	   while (i != -1)
	      {
	       byteArrayOutputStream.write(i);
	       i = inputStream.read();
	      }
	      inputStream.close();
	  } catch (IOException e) {
	   
	   e.printStackTrace();
	  }
	  
	  return byteArrayOutputStream.toString();
	}
	

	private void shareApp(String shareParam) {	
		
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = getString(R.string.share_body);
		String shareSubject = getString(R.string.share_subject);
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		
		// gets the list of intents that can be loaded.
		String typeFace = "faceb";
		String typeTwit	= "twit";
		Boolean foundFace = false;
		Boolean foundTwit = false;
	    List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(sharingIntent, 0);
	    if (!resInfo.isEmpty()){
	        for (ResolveInfo info : resInfo) {
	            if (info.activityInfo.packageName.toLowerCase().contains(typeFace) || 
	                    info.activityInfo.name.toLowerCase().contains(typeFace) ) {
	                sharingIntent.setPackage(info.activityInfo.packageName);
	                foundFace = true;
	                Map<String, String> flurryParams = new HashMap<String, String>(); 
	    				flurryParams.put("Share Type", shareParam);
	    				flurryParams.put("Share Intent", "Found Fbook");
	    				flurryParams.put("Share Package", info.activityInfo.packageName.toString());
	    			FlurryAgent.logEvent("Share App", flurryParams);
	                break;
	            }
	        }
	        if (!foundFace) {
	        	//check for twitter
	        	for (ResolveInfo info : resInfo) {
		            if (info.activityInfo.packageName.toLowerCase().contains(typeTwit) || 
		                    info.activityInfo.name.toLowerCase().contains(typeTwit) ) {
		                sharingIntent.setPackage(info.activityInfo.packageName);
		                foundTwit = true;
		                Map<String, String> flurryParams = new HashMap<String, String>(); 
		    				flurryParams.put("Share Type", shareParam);
		    				flurryParams.put("Share Intent", "Found Twit");
		    				flurryParams.put("Share Package", info.activityInfo.packageName.toString());
	    				FlurryAgent.logEvent("Share App", flurryParams);
		                break;
		            }
		        }
	        }
	        //otherwise bring up default chooser
	        if (!foundFace && !foundTwit) {
	        	Map<String, String> flurryParams = new HashMap<String, String>(); 
					flurryParams.put("Share Type", shareParam);
					flurryParams.put("Share Intent", "Present Chooser");
					flurryParams.put("Share Package", "No package");
				FlurryAgent.logEvent("Share App", flurryParams);
	        }
	    }
	    startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
	
	public void onAlphabet(View view) {
		//usage tracking
		FlurryAgent.logEvent("onAlphabet");
		getGuideList("All", "");
	}

	public void onKeyboard(View view) {
		//usage tracking
		FlurryAgent.logEvent("onKeyboard");
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Text Entry");
		alert.setMessage("Please enter your symptoms");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);
		final View crntView = view;

		alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (mDiagnosis != null) {
					//instance we have a previous query
					doVoiceDiagnosis(mDiagnosis.getGuide(), prevCurQuery, value);

				} else {
					//instance we are starting the selection process
					onHome(crntView);
					doVoiceDiagnosis("null",
							Constants.DIAGNOSIS_DEFAULT_LAST_QUERY, value);
				}
					
				
			}
		});

		alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
		 public void onClick(DialogInterface dialog, int whichButton) {
		     // Canceled.
		 }
		});

		
		input.requestFocus();
        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(input, 0);
            }
        },200);
		
		alert.show();
		
		
	}
	
	@Override
	public void onBackPressed() {
		//usage tracking
		FlurryAgent.logEvent("onBackPressed");
		Log.d(Constants.TAG,"Back Pressed");
		
		View currentView = flipper.getCurrentView();
		if (currentView.equals(menuView)) {
			// close the app
			Log.i("Alice", "backpressed"); 
			onStop();
			moveTaskToBack(true);
			//finish(); 
		} else if (currentView.equals(chatView)) {

			// moving to Home View. Clean the Chat list and remove the chat view
			chatAdapter.resetAdapter();
			flipper.removeView(currentView);
			

		} else  {

			// stepback one
			Log.i("Alice","Removed view");
			flipper.removeView(currentView);
		}
		stopTTS();
		
		int chcount = flipper.getChildCount();
		
		if (chcount==1) {
			prevCurQuery = Constants.DIAGNOSIS_DEFAULT_LAST_QUERY;
		}
	}
	
	private void showInfoAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alert.show();
	}
	


	private void showCallAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);

		alert.setButton(getString(R.string.call_doctor_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doLog(Constants.LOG_CALL_DOCTOR_REJECT);
						return;
					}
				});
		alert.setButton2(getString(R.string.call_doctor_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						doLog(Constants.LOG_CALL_DOCTOR_ACCEPT);

						// TODO call actual doctor

						try {
							Intent callIntent = new Intent(Intent.ACTION_CALL);
							callIntent.setData(Uri.parse("tel:"
									+ getString(R.string.call_doctor_number)));
							startActivity(callIntent);
						} catch (ActivityNotFoundException activityException) {
							Log.e(Constants.TAG, "Call failed",
									activityException);
						}
					}
				});
		alert.show();
	}

	
	/** This creates the orange button used a in number of the views
	 * 
	 */
	
	private Button createOrangeBtn(final String name, final int type, final String details, final String subDetails) {
		Button b = new Button(this);
		
		// Create Layout parameters object to dynamically
		// style the button before plugging it to the view
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		params.setMargins(15, 5, 0, 5);
		params.gravity = Gravity.CENTER;
		b.setLayoutParams(params);
		b.setGravity(Gravity.CENTER);

		b.setPadding(10, 10, 10, 10);

		Drawable btnBg = getResources().getDrawable(R.drawable.btn_orange);

		b.setBackgroundDrawable(btnBg);

		b.setText(name);

		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopTTS();		        	
				switch (type) {
				case MENU:
					if (details == "BodyPart") {
						createMenuWidget(BODY, null,"","");
					} else if (details == "Category") {
						createMenuWidget(CATEGORY, null,"","");
					} else if (details == "Demographic") {
						createMenuWidget(DEMOGRAPHIC, null,"","");
					} else if (details == "Owned") {
						createMenuWidget(OWNED, null,"","");
					} else  {
						createMenuWidget(ALL, null,"","");
					};
					break;
				case BODY:
					getGuideList("Body", details);
					break;
				case CATEGORY:
					getGuideList("Category", details);
					break;
				case DEMOGRAPHIC:
					getGuideList("Demographic", details);
					break;
				case GUIDE:
					chatQuery = name;
					doTouchDiagnosis(details,
							Constants.DIAGNOSIS_DEFAULT_LAST_QUERY, subDetails);
					break;
				}
				
			}
		});
		
		return b;
	}
	
	/**
	 * Handler to manage all the different menu displays
	 * 
	 */
	private void createMenuWidget(int layer, List <Guide> guides, String user_input, String reply) {
		LinearLayout lightbox;
		TextView text_inputTxt;
		TextView text_aliceTxt;
		
		switch (layer) {
		case HOME:
			// inflate main menu view
			FlurryAgent.logEvent("Show Home");
			menuView = inflater.inflate(R.layout.alice_first_row, null);	
			flipper.addView(menuView);
			flipper.showNext();
			
			//load description text into menuView
			text_inputTxt = (TextView) menuView.findViewById(R.id.description);
	        text_inputTxt.setText(Html.fromHtml(readTxt("Hello")));
	        
			// locate box for placing buttons
			lightbox = (LinearLayout) menuView
					.findViewById(R.id.lightbox_button_layout);
			
			for (MenuGuide menu : MenuGuide.values()) {

				//Button b = createOrangeBtn(name,GUIDE, guide, start_input);
				Button b = createOrangeBtn(menu.userFriendlyName(),MENU, menu.result(), "");
				// add each button to the layout
				lightbox.addView(b);

			}
			break;
		case BODY:
		case CATEGORY:
		case DEMOGRAPHIC:
					
			categoryView = inflater.inflate(R.layout.alice_first_row, null);
			flipper.addView(categoryView);
			flipper.showNext();
			
			// locate box for placing buttons
			lightbox = (LinearLayout) categoryView
								.findViewById(R.id.lightbox_button_layout);
			
			//load description text into menuView
			text_inputTxt = (TextView) categoryView.findViewById(R.id.description);
	        
			if (layer== BODY) {
				FlurryAgent.logEvent("Show Body");
				text_inputTxt.setText(Html.fromHtml(getString(R.string.menu_body)));
				
				for (BodyGuide body : BodyGuide.values()) {

					//Button b = createOrangeBtn(name,GUIDE, guide, start_input);
					Button b = createOrangeBtn(body.userFriendlyName(),layer, body.result(), "");
					// add each button to the layout
					lightbox.addView(b);

				}
			} else if (layer == CATEGORY) {
				FlurryAgent.logEvent("Show Category");
				text_inputTxt.setText(Html.fromHtml(getString(R.string.menu_category)));
				
				for (CategoryGuide body : CategoryGuide.values()) {

					//Button b = createOrangeBtn(name,GUIDE, guide, start_input);
					Button b = createOrangeBtn(body.userFriendlyName(),layer, body.result(), "");
					// add each button to the layout
					lightbox.addView(b);

				}
				
			} else if (layer == DEMOGRAPHIC) {
				FlurryAgent.logEvent("Show Demographic");
				text_inputTxt.setText(Html.fromHtml(getString(R.string.menu_demographic)));
				
				for (DemographicGuide body : DemographicGuide.values()) {

					//Button b = createOrangeBtn(name,GUIDE, guide, start_input);
					Button b = createOrangeBtn(body.userFriendlyName(),layer, body.result(), "");
					// add each button to the layout
					lightbox.addView(b);

				}
			};
			
			break;
			
		case OWNED:
			//TODO: update this to search for owned guides, requires back end work
			FlurryAgent.logEvent("Show Owned");
			getGuideList("Owned", "");
			break;
			
		case ALL:
	    	FlurryAgent.logEvent("Show All");
			getGuideList("All", "");
			break;
		
		case GUIDE:
			
			FlurryAgent.logEvent("Show Guides");
			View currentView = flipper.getCurrentView();
			if (currentView.equals(guideView)) {
				//don't need to do anything
			} else {
				//load the guideView
				guideView = inflater.inflate(R.layout.guide_list, null);
				flipper.addView(guideView);
				flipper.showNext();
			}
			
			//load description text into View
			text_inputTxt = (TextView) guideView.findViewById(R.id.options_text_query);
	        text_inputTxt.setText(user_input);
			
			text_aliceTxt = (TextView) guideView.findViewById(R.id.description);
	        text_aliceTxt.setText(reply);
	        
	        
			
			// locate box for placing buttons
			lightbox = (LinearLayout) guideView
					.findViewById(R.id.lightbox_button_layout);
			
			for(int i = 0; i < guides.size(); i++) {
				Guide guide = guides.get(i);
				//Button b = createOrangeBtn(name,GUIDE, guide, start_input);
				Button b = createOrangeBtn(guide.getSimpleName(),GUIDE, guide.getFileName(), guide.getStartOption());
				// add each button to the layout
				lightbox.addView(b);

			}
			break;
		}
		
		
		Button shareBtnMain = (Button) menuView.findViewById(R.id.share_app);
		
		shareBtnMain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//event tracking
				    		
	    		FlurryAgent.logEvent("Share App Btn pressed");
	    		shareApp("Share App Btn pressed");
	    		
			}

		});
		
	}

	private void initLogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter(Constants.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);
	}

	private void setNotFirstRun() {
		// Save the state with shared preferences
		getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE).edit()
				.putBoolean("firstRun", false).commit();
	}

	private boolean isFirstRun() {
		return getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE).getBoolean(
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
		msgIntent.putExtra(Constants.LOG_USER_ID, Utils.getUserID(this));
		msgIntent.putExtra(Constants.LOG_USER_TEL, Utils.getPhoneNumber(this));
		startService(msgIntent);

	}

	public void getGuideList(String type, String selection) {
		Map<String, String> flurryParams = new HashMap<String, String>(); 
			flurryParams.put("Type", type);
			flurryParams.put("Selection", selection);
		
		FlurryAgent.logEvent("Get guides", flurryParams);
		
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setType("Guide_list");
		diagnosisTask.setListener(listener);
		diagnosisTask.setLast_query(selection);
		diagnosisTask.setInput_text(type);
		diagnosisTask.setUser_id(Utils.getUserID(this));
		diagnosisTask.execute();
	}
	
	/**
	 * Touch Diagnosis - Creates an instance of {@link DiagnosisAsyncTask}, and
	 * sets the {@link AsyncTasksListener}
	 * 
	 * @param health_guide
	 *            and
	 * @param input_text
	 *            and then executes the request
	 */
	public void doTouchDiagnosis(String health_guide, String last_query,
			String input_text) {
		//event tracking code
		Map<String, String> flurryParams = new HashMap<String, String>(); 
    		flurryParams.put("Guide", health_guide);
    		flurryParams.put("Last Query", last_query);
    		flurryParams.put("Input Text", input_text);
    		flurryParams.put("Diagnosis Type", "Touch");
    		
		View currentView = flipper.getCurrentView();
		
		if (currentView.equals(chatView)) {
			//don't need to do anything
			FlurryAgent.logEvent("Provide response", flurryParams);
		} else {
			//we are starting new diagnosis
			FlurryAgent.logEvent("Start Guide", flurryParams);
		}
    	
		if (!everMute) {
			canTalk = false;
			setTalkSettings();
		}
		
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setType("Touch");
		diagnosisTask.setListener(listener);
		diagnosisTask.setLast_query(last_query);
		diagnosisTask.setHealth_guide(health_guide);
		diagnosisTask.setInput_text(input_text);
		diagnosisTask.setUser_id(Utils.getUserID(this));
		diagnosisTask.execute();
	}

	/**
	 * Voice Diagnosis - Creates an instance of {@link DiagnosisAsyncTask}, and
	 * sets the {@link AsyncTasksListener}
	 * 
	 * @param last_query
	 *            and
	 * @param input_text
	 *            and then executes the request
	 */
	public void doVoiceDiagnosis(String health_guide, String last_query,
			String input_text) {
		//event tracking code
		Map<String, String> flurryParams = new HashMap<String, String>(); 
    		flurryParams.put("Guide", health_guide);
    		flurryParams.put("Last Query", last_query);
    		flurryParams.put("Input Text", input_text);
    		flurryParams.put("Diagnosis Type", "Voice");
    		
		if (!everMute) {
			canTalk = true;
			setTalkSettings();
		}
    	
    	View currentView = flipper.getCurrentView();
		
		if (currentView.equals(chatView)) {
			//don't need to do anything
			FlurryAgent.logEvent("Provide response", flurryParams);
		} else {
			//we are starting new diagnosis
			FlurryAgent.logEvent("Start Guide", flurryParams);
		}
		    	
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setType("Voice");
		diagnosisTask.setListener(listener);
		diagnosisTask.setLast_query(last_query);
		diagnosisTask.setHealth_guide(health_guide);
		diagnosisTask.setInput_text(input_text);
		diagnosisTask.setUser_id(Utils.getUserID(this));
		diagnosisTask.execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIAGNOSIS_DIALOG:
			return _diagnosisDialog;
		case LISTENING_DIALOG:
			return _listeningDialog;
		case BILLING_NOT_WORKING_DIALOG:
			return simpleMessage("Problem with billing");
		case BILLING_WORKING_DIALOG:
			return simpleMessage("In-app billing supported");
		}

		return null;
	} 

	public void showHowTo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("How to use this app");
		builder.setMessage(Html.fromHtml(readTxt("HowTo")))
		       .setCancelable(true)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog howToDialog = builder.create();
		howToDialog.show(); 
		
	}
	
	public void askUpgrade() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Please upgrade the app");
		builder.setMessage("To access this content you need to update to the most recent version of the app.  Please press 'upload' to be taken to the Google Play store")
		       .setCancelable(true)
		       .setNegativeButton("Upgrade", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("Upgrade app");
		        	   rateIt();
		           }
		       })
		       .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("No, Don't Upgrade");
		        	   dialog.cancel();
		           }
		       });
		AlertDialog upgradeDialog = builder.create();
		upgradeDialog.show(); 
		
	}
	
	public void rateItDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Rate it!");
		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.rate_it, (ViewGroup) getCurrentFocus());
		builder.setView(dialoglayout);
		builder.setMessage("Would you like to rate this app?")
		       .setCancelable(true)
		       .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("Yes, Rate it!");
		        	   rateIt();
		           }
		       })
		       .setPositiveButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("No, Don't Rate it!");
		        	   dialog.cancel();
		           }
		       });
		AlertDialog rateDialog = builder.create();
		rateDialog.show(); 
		
	}
	
	public void askShareApp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Tell your friends?");
		builder.setMessage("Would you like to tell your friends about this app?")
		       .setCancelable(true)
		       .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("Yes Share App!");
		        	   shareApp("Yes Share App!");
		           }
		       })
		       .setPositiveButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   FlurryAgent.logEvent("No, don't Share App!");
		        	   dialog.cancel();
		           }
		       });
		AlertDialog rateDialog = builder.create();
		rateDialog.show(); 
		
	}
	
	public void rateIt() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
 	   	intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
 	   	startActivity(intent);
	}
	
	public AlertDialog simpleMessage(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog dialog = builder.create();
		return dialog;		
	}
	
	@Override
	public void onTaskPreExecute() {
		showDialog(DIAGNOSIS_DIALOG);
	}

	@Override
	public void onTaskProgress(CharSequence message) {
		_diagnosisDialog.setText(message.toString());
	}

	@Override
	public void onTaskPostExecute(Object obj) {
		removeDialog(DIAGNOSIS_DIALOG);

		Diagnosis result = (Diagnosis) obj;
		
		if (result == null) {
			// This shouldn't really happen unless we have a bad server connection
			FlurryAgent.logEvent("Diagnosis returned null");
			Toast.makeText(getApplicationContext(), "Weak internet connection, please retry your query", Toast.LENGTH_SHORT).show();
			
			
			return;
		}
		
		if (result.getHas_guides()) {
			//This means that our asynctask has returned a list of guides
			//we need to display a guide menu
			createMenuWidget(GUIDE,result.getGuides(),result.getInput(),result.getReply());
			
			return;
		}
		
		if (result.getPurchasedState()) {

			// In cases where the server cannot understand/decipher
			// input_text, we receive back a 'problem'. Test for the same
			// here
			if (result.getCurrent_query().equalsIgnoreCase("problem")) {

				// TODO Show Alert Dialog that the server could not
				// understand their request

				showInfoAlert(getString(R.string.alert_dialog_title),
						result.getReply() + "\n" + "(" + result.getInput()
								+ ")");

			} else  {

				String last_query = result.getLast_query();
				String select_type = result.getSelect_type();

				// This handles the selection of a guide via voice, from the
				// first screen
				if (last_query
						.equalsIgnoreCase(Constants.DIAGNOSIS_DEFAULT_LAST_QUERY)
						&& select_type
								.equalsIgnoreCase(Constants.DIAGNOSIS_VOICE)) {

					chatQuery = result.getGuide();
				}

				// Check if server has returned an unclear response(Unclear
				// response, please try again.), and send the user a message.
				// Also stop TTS

				if (result.getReply().contains(
						getString(R.string.unknown_response_phrase))) {

					showInfoAlert(getString(R.string.app_name),
							getString(R.string.unknown_response_alert));
				} else {

					// add diagnosis object to adapter
					// The list will now be able to render the view based on the
					// data model provided
					chatAdapter.addItem(result);

					chatlist.clearFocus();

					// After list is drawn, initiate a task to scroll to te item
					// just added
					chatlist.post(new Runnable() {

						@Override
						public void run() {
							// 1);

							scrollToLastItem();

						}
					});

					// identify the view on display currently
					View currentView = flipper.getCurrentView();
					
					if (currentView.equals(chatView)) {
						//don't need to do anything
					} else {
						//load the chatView
						try {
							flipper.addView(chatView);
							flipper.showNext();
						} catch (IllegalStateException e) {
							FlurryAgent.logEvent("IllegalStateException showing response");
							int i = 0;
							while ((!currentView.equals(chatView)) && (i < 20 )) {
								flipper.removeView(currentView);
								currentView = flipper.getCurrentView();
								i = i + 1;
							} 
						} catch (Exception e){
							FlurryAgent.onError("Error showing response", "Error: " + e, "");
						}
						
					}
					/*if (currentView.equals(menuView)) {
						// identify the number of children in the flipper
						int childCount = flipper.getChildCount();
						if (childCount > 1) {
							flipper.showNext();
						} else {
							flipper.addView(chatView);
							flipper.showNext();
						}

						// handle

					} else if (currentView.equals(chatView)) {

						// Incidentally we do not have a case that makes use of
						// this logic space
						// handle

					}*/
				}
			} 
		}  else  {
			//this means they haven't purchased guide yet
			askUpgrade();
			
			//old code to direct to purchase guides
			/*if (!mBillingService.requestPurchase("womanssexualhealth", Constants.ITEM_TYPE_INAPP, "developerPayload")) {
                showDialog(BILLING_NOT_WORKING_DIALOG);
                FlurryAgent.onError("Billing Error", "App purchase failure" + System.currentTimeMillis(), "");
            } else {
            	Log.i(Constants.TAG,"Billing request sent");
            }*/
		}

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		int position = flipper.getDisplayedChild();
		savedInstanceState.putInt("FLIPPER_POSITION", position);
	}

	public void stopTTS() {
		if (mTts != null) {
			mTts.stop();

		}
	}

	public void killTTS() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
	}

	public void scrollToLastItem() {

		// Identify the last index in the data model
		int lastPosition = chatAdapter.getCount() - 1;

		// move to that Item
		chatlist.setSelection(lastPosition);

		speakText(talkResp);

	}

	/**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
     * messages to this application so that we can update the UI.
     */
    private class AlicePurchaseObserver extends PurchaseObserver {
        public AlicePurchaseObserver(Handler handler) {
            super(Alice.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
            if (Constants.DEBUG) {
                Log.i("Purchases", "supported: " + supported);
            }
            if (type == null || type.equals(Constants.ITEM_TYPE_INAPP)) {
                if (supported) {
                  //  restoreDatabase();
                  //  mBuyButton.setEnabled(true);
                  //  mEditPayloadButton.setEnabled(true);
                } else {
                    showDialog(BILLING_NOT_WORKING_DIALOG);
                }
            } else if (type.equals(Constants.ITEM_TYPE_SUBSCRIPTION)) {
               // mCatalogAdapter.setSubscriptionsSupported(supported);
            } else {
                showDialog(BILLING_NOT_WORKING_DIALOG);
            }
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
                int quantity, long purchaseTime, String developerPayload) {
            if (Constants.DEBUG) {
                Log.i("Purchases", "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            }

            if (developerPayload == null) {
                Log.i("Purchases",itemId + purchaseState.toString());
            } else {
            	Log.i("Purchases",itemId + purchaseState + "\n\t" + developerPayload);
            }
            
            if (purchaseState == PurchaseState.PURCHASED) {
                //mOwnedItems.add(itemId);
                
            }
            //mCatalogAdapter.setOwnedItems(mOwnedItems);
            //mOwnedItemsCursor.requery();
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request,
                ResponseCode responseCode) {
        	
        	if (Constants.DEBUG) {
                Log.d("Purchases", request.mProductId + ": " + responseCode);
            }
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Constants.DEBUG) {
                    Log.i("Purchases", "purchase was successfully sent to server");
                }
                Log.i("Purchases",request.mProductId + "sending purchase request");
                Map<String, String> flurryParams = new HashMap<String, String>(); 
	    			flurryParams.put("Purchase Response Type", "Result OK");
	    			flurryParams.put("Purchase Product ID", request.mProductId);
	    			flurryParams.put("Purchase Developer Payload", request.mDeveloperPayload);
	    			flurryParams.put("Purchase Product Type", request.mProductType);
	    			
	    		FlurryAgent.logEvent("Purchase Reponse", flurryParams);
            } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                if (Constants.DEBUG) {
                    Log.i("Purchases", "user canceled purchase");
                }
                Map<String, String> flurryParams = new HashMap<String, String>(); 
    				flurryParams.put("Purchase Response Type", "User Cancelled");
    				flurryParams.put("Purchase Product ID", request.mProductId);
	    			flurryParams.put("Purchase Developer Payload", request.mDeveloperPayload);
	    			flurryParams.put("Purchase Product Type", request.mProductType);
    			FlurryAgent.logEvent("Purchase Reponse", flurryParams);	
            } else if (responseCode == ResponseCode.RESULT_SERVICE_UNAVAILABLE) {
                if (Constants.DEBUG) {
                    Log.i("Purchases", "result service unavailable");
                }
                Map<String, String> flurryParams = new HashMap<String, String>(); 
    				flurryParams.put("Purchase Response Type", "Result service unavailable");
    				flurryParams.put("Purchase Product ID", request.mProductId);
	    			flurryParams.put("Purchase Developer Payload", request.mDeveloperPayload);
	    			flurryParams.put("Purchase Product Type", request.mProductType);
    			FlurryAgent.logEvent("Purchase Reponse", flurryParams);
            } else {
                if (Constants.DEBUG) {
                    Log.i("Purchases", "purchase failed");
                }
                Map<String, String> flurryParams = new HashMap<String, String>(); 
    				flurryParams.put("Purchase Response Type", "Purchase failed");
    				flurryParams.put("Purchase Product ID", request.mProductId);
	    			flurryParams.put("Purchase Developer Payload", request.mDeveloperPayload);
	    			flurryParams.put("Purchase Product Type", request.mProductType);
    			FlurryAgent.logEvent("Purchase Reponse", flurryParams);
            }
        }

    }
	
    @Override
    protected void onStart()  {
    	super.onStart();
    	String app_ver = "";
    	try {
    	    app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
    	}
    	catch (NameNotFoundException e)  {
    	    Log.v("Alice", e.getMessage());
    	}
    	
    	//start flurry agent
    	FlurryAgent.onStartSession(this, Constants.FLURRY_API);
    	FlurryAgent.setUserId(Utils.getUserID(this));
    	FlurryAgent.setReportLocation(true);   	
    	FlurryAgent.setVersionName(app_ver);
    	FlurryAgent.setLogEnabled(true);
    	FlurryAgent.setLogEvents(true);
    	
    }
    
    @Override
	protected void onPause() {
		stopTTS();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

    @Override
    protected void onStop(){
      super.onStop();

      //stop the flurry agent
      FlurryAgent.onEndSession(this);
       
      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(MyPrefsBackupAgent.PREFS, MODE_PRIVATE);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("canTalk", canTalk);
      editor.putBoolean("everMute", everMute);
      editor.putInt("usageCount", usageCount + 1);

      // Commit the edits!
      editor.commit();
      
      //And backup 
	  mBackupManager.dataChanged();
	  Log.i("Alice","onstop completed");
    }
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
        mBillingService.unbind();
        ResponseHandler.unregister(mAlicePurchaseObserver); 
		killTTS();
		mChecker.onDestroy();
		super.onDestroy();
	}

	public class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {
				String text = intent
						.getStringExtra(Constants.LOG_SERVICE_OUT_MSG);

				// Check if Log Type is register, and if so,
				// mark is first run to false
				if (text != null && text.equalsIgnoreCase("1")) {
					setNotFirstRun();
				}
				if (text != null && text.equalsIgnoreCase("2")) {

					canCallDoctor = Boolean.parseBoolean(Registry.instance()
							.get(Constants.REGISTRY_CALL).toString());
				}

				//Log.i(Constants.TAG, "Successfully logged type " + text);

			}
		}

	}

	private void removeDiagnosisView(View view) {
		stopTTS();
		flipper.removeView(view);
		prevCurQuery = Constants.DIAGNOSIS_DEFAULT_LAST_QUERY;
	}

	private void showMap() {

		Object loc = Registry.instance().get(Constants.REGISTRY_LOCATION);
		if (loc != null) {

			String uri = "geo:"
					+ Registry.instance().get(Constants.REGISTRY_LOCATION)
							.toString() + "?q=Emergency+Room";
			startActivity(new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse(uri)));
		} else {
			// Location is not available. Opt to show alert dialog or ignore
			Toast.makeText(this, "Location currently unavailable",
					Toast.LENGTH_LONG);
		}

	}

	// custom adapter for the ChatListview
	public class AliceChatAdapter extends BaseAdapter {
		private List<Diagnosis> listitems;
		private LayoutInflater inflater;
		private Context context;

		public AliceChatAdapter(Context context) {
			this.context = context;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// init the data model
			listitems = new ArrayList<Diagnosis>();

		}

		@Override
		public int getCount() {
			return listitems.size();
		}

		public void resetAdapter() {
			listitems = new ArrayList<Diagnosis>();
		}

		@Override
		public Object getItem(int index) {
			return listitems.get(index);
		}

		@Override
		public long getItemId(int index) {
			return 0;
		}

		// This function provides rendering logic for the different
		// items in the ChatList based on the data model
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// retrieve currently selected item
			Alice.this.mDiagnosis = listitems.get(position);
			final int mposition = position;
			View row = null;

			TextView queryTxt = null;
			TextView responseTxt = null;

			// retrieve ID for discriminating the different views
			final String type = mDiagnosis.getResponse_type();
			
			
			//event tracking code
			final String guide = mDiagnosis.getGuide();
			final String select_type = mDiagnosis.getSelect_type();
			final String input = mDiagnosis.getInput();
			final String reply = mDiagnosis.getReply();
			
			Map<String, String> flurryParams = new HashMap<String, String>(); 
	    		flurryParams.put("Guide", guide);
	    		flurryParams.put("Input", input);
	    		flurryParams.put("Input Text", input);
	    		flurryParams.put("Diagnosis Type", select_type);
	    		flurryParams.put("Reply", reply);
	    		flurryParams.put("Response Type", type);
	    		
	    	FlurryAgent.logEvent("Response", flurryParams);
			
			int diagnosisType = Integer.parseInt(type);

			switch (diagnosisType) {
			case 1:
				// Response Type 1 - Show Confirm Dialog . This is
				// ignored for now
				break;
			case 2:
				// Response Type 2 - Show Options Dialog
				row = inflater.inflate(R.layout.diagnosis_options_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.options_txt_response);

				Button opt_close = (Button) row
						.findViewById(R.id.options_close);
				opt_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking code
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", "X");
			    		
				    	FlurryAgent.logEvent("Remove response", flurryParams);
						
						removeItem(mposition);

						if (listitems.size() == 0) {
							removeDiagnosisView(flipper.getCurrentView());
						} else {
							notifyDataSetChanged();

							chatlist.setSelectionFromTop(
									chatAdapter.getCount() - 1, 10);
						}

					}
				});

				LinearLayout optGroup = (LinearLayout) row
						.findViewById(R.id.options_response_options);

				HashMap<String, String> respOpts = mDiagnosis
						.getReply_options();

				for (Entry<String, String> entry : respOpts.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();

					Button bo = new Button(this.context);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					params.setMargins(15, 5, 0, 5);
					params.gravity = Gravity.CENTER;
					bo.setLayoutParams(params);
					bo.setGravity(Gravity.CENTER);

					bo.setPadding(10, 10, 10, 10);
					Drawable btnBg = getResources().getDrawable(
							R.drawable.btn_orange);

					bo.setBackgroundDrawable(btnBg);

					bo.setText(key);

					bo.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							Alice.this.chatQuery = key;

							stopTTS();

							doTouchDiagnosis(mDiagnosis.getGuide(),
									prevCurQuery, value);
						}
					});

					optGroup.addView(bo);

				}
				break;

			case 3:
				// Response Type 3 - EMERGENCY - Load Map with search results of
				// nearest
				// hospital/doctor
				flurryParams.put("Recommendation", "Emergency");
	    		
	    		FlurryAgent.logEvent("Recommendation", flurryParams);
				
				row = inflater.inflate(R.layout.diagnosis_map_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.map_txt_response);
				Button map_close = (Button) row.findViewById(R.id.map_close);
				map_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", "X");
		    		
			    		FlurryAgent.logEvent("Remove response", flurryParams);
						
			    		removeItem(mposition);

						if (listitems.size() == 0) {
							removeDiagnosisView(flipper.getCurrentView());
						} else {
							notifyDataSetChanged();

							chatlist.setSelectionFromTop(
									chatAdapter.getCount() - 1, 10);
						}

					}
				});

				Button mapBtn = (Button) row.findViewById(R.id.emergency_btn);
				mapBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", type);
			    		
			    		FlurryAgent.logEvent("Show Map", flurryParams);
			    		
						showMap();

					}
				});

				break;
			case 4:
				// Response Type 4 - CALL DOCTOR - Text with button
				// to call doctor.
				flurryParams.put("Recommendation", "Call Doctor");
    		
	    		FlurryAgent.logEvent("Recommendation", flurryParams);
				
				
				row = inflater.inflate(R.layout.diagnosis_calldoc_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.calldoc_txt_response);

				Button calldoc_close = (Button) row
						.findViewById(R.id.calldoc_close);
				calldoc_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Map<String, String> flurryParams = new HashMap<String, String>(); 
			    			flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", "X");
		    		
			    		FlurryAgent.logEvent("Remove response", flurryParams);
						
						removeItem(mposition);

						if (listitems.size() == 0) {
							removeDiagnosisView(flipper.getCurrentView());
						} else {
							notifyDataSetChanged();

							chatlist.setSelectionFromTop(
									chatAdapter.getCount() - 1, 10);
						}

					}
				});

				Button callBtn = (Button) row.findViewById(R.id.calldoc_btn);

				callBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", type);
			    		
			    		FlurryAgent.logEvent("Find Doctor", flurryParams);
			    		
						stopTTS();
						
						// Build the intent
						Uri webpage = Uri.parse("http://www.google.com/search?q=doctor");
						Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);

						// Verify it resolves
						PackageManager packageManager = getPackageManager();
						List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
						boolean isIntentSafe = activities.size() > 0;
						  
						// Start an activity if it's safe
						if (isIntentSafe) {
						    startActivity(webIntent);
						}

						/*if (canCallDoctor) {
							doLog(Integer.toString(Constants.LOG_CALL_DOCTOR));
							showCallAlert(getString(R.string.app_name),
									getString(R.string.call_doctor_text));

						} else {

							showInfoAlert(getString(R.string.app_name),
									getString(R.string.call_doctor_unavailable));
						}*/

					}
				});

				break;
			case 5:
				// Response Type 5 - INFORMATION - Text
				flurryParams.put("Recommendation", "Information/pharmacy");
	    		
	    		FlurryAgent.logEvent("Recommendation", flurryParams);
				
				row = inflater.inflate(R.layout.diagnosis_information_chat,
						null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.info_txt_response);

				Button info_close = (Button) row.findViewById(R.id.info_close);
				info_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//flurry tracking
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", "X");
		    		
			    		FlurryAgent.logEvent("Remove response", flurryParams);
						
						removeItem(mposition);

						if (listitems.size() == 0) {
							removeDiagnosisView(flipper.getCurrentView());
						} else {
							notifyDataSetChanged();

							chatlist.setSelectionFromTop(
									chatAdapter.getCount() - 1, 10);
						}

					}
				});
				
				Button shareBtnInfo = (Button) row.findViewById(R.id.share_app);
				
				shareBtnInfo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking
						    		
			    		FlurryAgent.logEvent("Share App Btn pressed");
			    		shareApp("Share App Btn pressed");
			    		
					}

				});
				
				break;

			case 6:

				// Response Type 6 - Exit current guide and open new one. In the
				// options array you should have just one option, which the link
				// name will contain the name of the guide to link to. You
				// should then respond with the start of that guide (you have
				// already the codes to do this).
				flurryParams.put("Recommendation", "Change Guide");
	    		
	    		FlurryAgent.logEvent("Change Guide", flurryParams);
	    		
				row = inflater.inflate(R.layout.diagnosis_options_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.options_txt_response);

				Button opt6_close = (Button) row
						.findViewById(R.id.options_close);
				opt6_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//event tracking
						Map<String, String> flurryParams = new HashMap<String, String>(); 
				    		flurryParams.put("Guide", guide);
				    		flurryParams.put("Input", input);
				    		flurryParams.put("Input Text", input);
				    		flurryParams.put("Diagnosis Type", select_type);
				    		flurryParams.put("Reply", reply);
				    		flurryParams.put("Response Type", "X");
		    		
			    		FlurryAgent.logEvent("Remove response", flurryParams);
			    		
						removeItem(mposition);

						if (listitems.size() == 0) {
							removeDiagnosisView(flipper.getCurrentView());
						} else {
							notifyDataSetChanged();

							chatlist.setSelectionFromTop(
									chatAdapter.getCount() - 1, 10);
						}

					}
				});

				LinearLayout _optGroup = (LinearLayout) row
						.findViewById(R.id.options_response_options);

				HashMap<String, String> _respOpts = mDiagnosis
						.getReply_options();

				for (Entry<String, String> entry : _respOpts.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();

					Button bo = new Button(this.context);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					params.setMargins(15, 5, 0, 5);
					params.gravity = Gravity.CENTER;
					bo.setLayoutParams(params);
					bo.setGravity(Gravity.CENTER);

					bo.setPadding(10, 10, 10, 10);
					Drawable btnBg = getResources().getDrawable(
							R.drawable.btn_orange);

					bo.setBackgroundDrawable(btnBg);

					bo.setText(key);

					bo.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							Alice.this.chatQuery = key;

							stopTTS();

							chatAdapter.resetAdapter();

							doTouchDiagnosis(value,
									Constants.DIAGNOSIS_DEFAULT_LAST_QUERY,
									value);
						}
					});

					_optGroup.addView(bo);

				}
				break;
			default:
				break;
			}

			// TODO Set both the query and response Strings to the TextViews

			if (queryTxt != null && responseTxt != null) {
				
				Log.i("Alice info", mDiagnosis.getQuery_string());
				queryTxt.setText(mDiagnosis.getQuery_string());
				String toTrim = mDiagnosis.getReply().trim();
				responseTxt.setText(Html.fromHtml(toTrim));

			}

			return row;
		}

		public void addItem(Diagnosis diagnosis) {

			diagnosis.setQuery_string(chatQuery);
			prevCurQuery = diagnosis.getCurrent_query();

			talkResp = diagnosis.getReply().replaceAll("<(.|\n)*?>", "");
			listitems.add(diagnosis);

			// tell listeners that underlying data has changed. Refresh the
			// view
			chatAdapter.notifyDataSetChanged();

		}

		public void removeItem(int position) {

			stopTTS();

			if (position > 0) {
				Diagnosis holder = listitems.get(position - 1);
				prevCurQuery = holder.getCurrent_query();
				talkResp = holder.getReply().replaceAll("<(.|\n)*?>", "");
			}

			listitems = listitems.subList(0, position);
			notifyDataSetChanged();
		}

	}

	// Start Voice Business
	public void startDictation(View view) {
		stopTTS();

		_listeningDialog.setText("Initializing...");
		showDialog(LISTENING_DIALOG);
		_listeningDialog.setStoppable(false);
		setResults(new Recognition.Result[0]);

		_currentRecognizer = Alice.getSpeechKit().createRecognizer(
				Recognizer.RecognizerType.Dictation,
				Recognizer.EndOfSpeechDetection.Long, "en_US", _listener,
				_handler);
		_currentRecognizer.start();
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch (id) {
		case LISTENING_DIALOG:
			_listeningDialog.prepare(new Button.OnClickListener() {

				public void onClick(View v) {
					if (_currentRecognizer != null) {
						_currentRecognizer.stopRecording();
					}
				}
			});
			break;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (_listeningDialog.isShowing() && _currentRecognizer != null) {
			// If a recognition is in progress, save it, because the activity
			// is about to be destroyed and recreated
			SavedState savedState = new SavedState();
			savedState.Recognizer = _currentRecognizer;
			savedState.DialogText = _listeningDialog.getText();
			savedState.DialogRecording = _listeningDialog.isRecording();
			savedState.Handler = _handler;

			_currentRecognizer = null; // Prevent onDestroy() from canceling

			// Save the Vocalizer state, because we know the Activity will be
			// immediately recreated.
			savedState.Vocalizer = _vocalizer;
			savedState.Context = _lastTtsContext;

			_vocalizer = null; // Prevent onDestroy() from canceling

			return savedState;
		}
		return null;
	}

	// Allow other activities to access the SpeechKit instance.
	static SpeechKit getSpeechKit() {
		return _speechKit;
	}

	private class SavedState {
		String DialogText;
		boolean DialogRecording;
		Recognizer Recognizer;
		Handler Handler;
		Vocalizer Vocalizer;
		Object Context;
	}

	private Recognizer.Listener createListener() {
		return new Recognizer.Listener() {

			public void onRecordingBegin(Recognizer recognizer) {
				_listeningDialog.setText("Recording...");
				_listeningDialog.setStoppable(true);
				_listeningDialog.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable() {
					public void run() {
						if (_listeningDialog != null
								&& _listeningDialog.isRecording()
								&& _currentRecognizer != null) {
							// _listeningDialog.setLevel(Float
							// .toString(_currentRecognizer
							// .getAudioLevel()));
							_handler.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			public void onRecordingDone(Recognizer recognizer) {
				_listeningDialog.setText("Processing...");
				_listeningDialog.setRecording(false);
				_listeningDialog.setStoppable(false);
			}

			public void onError(Recognizer recognizer, SpeechError error) {
				if (recognizer != _currentRecognizer)
					return;
				if (_listeningDialog.isShowing())
					dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);

				// Display the error + suggestion in the edit box
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();

				if (suggestion == null)
					suggestion = "";
				// TODO
				//Log.i(Constants.TAG, detail + " - " + suggestion);

				showInfoAlert(getString(R.string.app_name), suggestion);

			}

			public void onResults(Recognizer recognizer, Recognition results) {
				if (_listeningDialog.isShowing())
					dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result[] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++) {
					rs[i] = results.getResult(i);
				}

				setResults(rs);
			}
		};
	}

	private void setResults(Recognition.Result[] results) {
		if (results.length > 0) {
			String t = results[0].getText();

			//Log.i(Constants.TAG, "Voice Results : " + t);

			if (mDiagnosis != null) {
				// Set the voice input as the query string in the Diagnosis
				// object
				// Voice response should match with the selected guide
				// option, rather than the input text (sometimes the input text
				// looks wrong or is badly spelled)

				chatQuery = t;

				doVoiceDiagnosis(mDiagnosis.getGuide(), prevCurQuery, t);

			} else {
				doVoiceDiagnosis("null",
						Constants.DIAGNOSIS_DEFAULT_LAST_QUERY, t);
			}

		} else {

		}
	}

	private void speakText(String text) {
		if (!canTalk) {
			return;
		}

		if (isTTSReady) {
			mTts.stop();
			mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
		} else {
			// TTS not ready so Cannot speak :-(
			//Log.i(Constants.TAG, "Cannot speak. TTS Engine not ready");
		}

	}

	private void createListeningDialog() {
		_listeningDialog = new ListeningDialog(this);
		_listeningDialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer != null) // Cancel the current recognizer
				{
					_currentRecognizer.cancel();
					_currentRecognizer = null;
				}

				if (!_destroyed) {
					// Remove the dialog so that it will be recreated next time.
					// This is necessary to avoid a bug in Android >= 1.6 where
					// the animation stops working.
					Alice.this.removeDialog(LISTENING_DIALOG);
					createListeningDialog();
				}
			}
		});
	}

	private void createDiagnosisDialog() {
		_diagnosisDialog = new DiagnosisDialog(this);
		_diagnosisDialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {

			}
		});
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if ((status == TextToSpeech.SUCCESS) && (mTts != null)) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			// int result = mTts.setLanguage(Locale.US);

			int result = mTts.setLanguage(Locale.ENGLISH);

			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Language data is missing or the language is not supported.
				isTTSReady = false;
				Log.e(Constants.TAG, "Language is not available.");
			} else {
				// Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.

				// The TTS engine has been successfully initialized.

				// It is ok to proceed with normal app flow

				isTTSReady = true;

				if (isFirstTime) {
					isFirstTime = false;
					// Speak the welcome text
					
					//speakText(Html.fromHtml(readTxt("Hello")).toString());
					

				}
			}
		} else {
			// Initialization failed.
			isTTSReady = false;
			Log.e(Constants.TAG, "Could not initialize TextToSpeech.");
		}
	}

	LocationResult locationResult = new LocationResult() {
		@Override
		public void gotLocation(Location location) {
			// Got the location!
			if (location != null) {
				String loc = location.getLatitude() + ","
						+ location.getLongitude();
				//Log.i(Constants.TAG, "Logging location " + loc);
				Registry.instance().put(Constants.REGISTRY_LOCATION, loc);

				doLog(Integer.toString(Constants.LOG_LOCATION));

			}

		}
	};

}