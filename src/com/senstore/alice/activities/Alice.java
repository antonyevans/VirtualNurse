package com.senstore.alice.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.senstore.alice.R;
import com.senstore.alice.api.HarvardGuide;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.AdapterDiagnosis;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.overlays.AliceItemizedOverlay;
import com.senstore.alice.services.BackgroundLogger;
import com.senstore.alice.tasks.DiagnosisAsyncTask;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

public class Alice extends Activity implements AsyncTasksListener {

	private ResponseReceiver receiver;

	private static final int DIAGNOSIS_DIALOG = 0;
	private ProgressDialog mProgressDialog;

	final AsyncTasksListener listener = this;

	private DiagnosisAsyncTask diagnosisTask;

	private View chatview;
	private ListView chatlist;
	private ViewFlipper flipper;

	private AliceChatAdapter chatAdapter;

	private View menuView;

	private LayoutInflater inflater;

	private AdapterDiagnosis mDiagnosis;

	private static SpeechKit _speechKit;
	private static final int LISTENING_DIALOG = 1;
	private Handler _handler = null;
	private final Recognizer.Listener _listener;
	private Recognizer _currentRecognizer;
	private ListeningDialog _listeningDialog;
	private boolean _destroyed;

	private Vocalizer _vocalizer;
	private Object _lastTtsContext = null;

	public Alice() {
		super();
		_listener = createListener();
		_currentRecognizer = null;
		_listeningDialog = null;
		_destroyed = true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.main);

		inflater = getLayoutInflater();

		// inflate flipper to switch between menu and chat screen
		flipper = (ViewFlipper) findViewById(R.id.alice_view_flipper);

		// inflate the view with the listview
		chatview = inflater.inflate(R.layout.alice_chat_list_layout, null);

		// load listview
		chatlist = (ListView) chatview.findViewById(R.id.alice_chat_list);
		chatlist.setFocusable(false);

		chatAdapter = new AliceChatAdapter(this);
		chatlist.setAdapter(chatAdapter);

		// Load the main menu
		createHarvardGuideWidget();

		// TODO Fix the on screen rotation changed
		if (savedInstanceState != null) {
			int flipperPosition = savedInstanceState.getInt("FLIPPER_POSITION");
			flipper.setDisplayedChild(flipperPosition);
		}

		flipper.addView(menuView);

		// Register the Background Logger Broadcast Receiver
		initLogBroadcastReceiver();

		// Check if the app has been run before.
		if (isFirstRun()) {
			Log.i(Constants.TAG, "isFirstRun()");
			// Start a background Task, to register the current user/device
			// doLog(Integer.toString(Constants.LOG_REGISTER));

		}// else proceed with the normal app flow

		// TODO Speech/Voice

		// set volume control to media
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Adjust the volume
		// AudioManager audio = (AudioManager)
		// getSystemService(Context.AUDIO_SERVICE);
		// int max_volume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		// audio.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);

		// If this Activity is being recreated due to a config change (e.g.
		// screen rotation), check for the saved SpeechKit instance.
		_speechKit = (SpeechKit) getLastNonConfigurationInstance();
		if (_speechKit == null) {
			_speechKit = SpeechKit.initialize(getApplication()
					.getApplicationContext(), AppInfo.SpeechKitAppId,
					AppInfo.SpeechKitServer, AppInfo.SpeechKitPort,
					AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
			_speechKit.connect();
			// TODO: Keep an eye out for audio prompts not working on the Droid
			// 2 or other 2.2 devices.
			Prompt beep = _speechKit.defineAudioPrompt(R.raw.beep);
			_speechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100),
					null, null);
		}
		_destroyed = false;

		// Initialize the listening dialog
		createListeningDialog();

		// Create Vocalizer listener
		Vocalizer.Listener vocalizerListener = new Vocalizer.Listener() {

			public void onSpeakingBegin(Vocalizer vocalizer, String text,
					Object context) {

				// TODO
				// updateCurrentText("Alice:  " + text, Color.GRAY, false);
			}

			public void onSpeakingDone(Vocalizer vocalizer, String text,
					SpeechError error, Object context) {
				// Use the context to detemine if this was the final TTS phrase
				if (context != _lastTtsContext) {
					// updateCurrentText("More phrases remaining", Color.YELLOW,
					// false);
				} else {
					// updateCurrentText("", Color.WHITE, false);
				}
			}
		};
		_vocalizer = Alice.getSpeechKit().createVocalizerWithLanguage("en_US",
				vocalizerListener, new Handler());
		_vocalizer.setVoice("Serena");

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
			_listeningDialog.setLevel(savedState.DialogLevel);
			_listeningDialog.setRecording(savedState.DialogRecording);
			_handler = savedState.Handler;

			if (savedState.DialogRecording) {
				// Simulate onRecordingBegin() to start animation
				_listener.onRecordingBegin(_currentRecognizer);
			}

			_currentRecognizer.setListener(_listener);

		}

		// speakReply("Welcome to the Pocket Doctor. I am Alice, how can I help you today? You can click on the microphone to talk to me.");

	}

	private void showAlert(String title, String message) {
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

	/**
	 * Loop through the Harvard Guide enum, to fetch the guides and their
	 * respective properties
	 * 
	 * @Mimano This is where you put in your buttons/list in a scroll view
	 */
	private void createHarvardGuideWidget() {
		// inflate main menu view
		menuView = inflater.inflate(R.layout.alice_first_row, null);
		// locate box for placing buttons
		LinearLayout lightbox = (LinearLayout) menuView
				.findViewById(R.id.lightbox_button_layout);

		for (HarvardGuide hg : HarvardGuide.values()) {
			final String name = hg.officialName();
			final String guide = hg.guideName();
			final String start_input = hg.startInput();

			Button b = new Button(this);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 10, 10, 10);
			params.gravity = Gravity.CENTER;
			// params.height = 35;
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);

			b.setPadding(10, 10, 10, 10);
			Drawable btnBg = getResources().getDrawable(R.drawable.btn_orange);

			b.setBackgroundDrawable(btnBg);

			b.setText(name);

			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doTouchDiagnosis(guide, Constants.VOICE_DEFAULT_LAST_QUERY,
							start_input);
				}
			});

			lightbox.addView(b);

			Log.i(Constants.TAG, hg.officialName() + " :: " + hg.guideName());
		}
	}

	private void initLogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter(Constants.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);
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
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setVoice(false);
		diagnosisTask.setListener(listener);
		diagnosisTask.setLast_query(last_query);
		diagnosisTask.setHealth_guide(health_guide);
		diagnosisTask.setInput_text(input_text);
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
	public void doVoiceDiagnosis(String health_guide,String last_query, String input_text) {
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setVoice(true);
		diagnosisTask.setListener(listener);
		diagnosisTask.setLast_query(last_query);
		diagnosisTask.setHealth_guide(health_guide);
		diagnosisTask.setInput_text(input_text);
		diagnosisTask.execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIAGNOSIS_DIALOG:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle(getString(R.string.app_name));
			mProgressDialog.setMessage("Working...");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		case LISTENING_DIALOG:
			return _listeningDialog;
		}

		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		int position = flipper.getDisplayedChild();
		savedInstanceState.putInt("FLIPPER_POSITION", position);
	}

	@Override
	public void onTaskPreExecute() {
		showDialog(DIAGNOSIS_DIALOG);
	}

	@Override
	public void onTaskProgress(CharSequence message) {
		mProgressDialog.setMessage(message);
	}

	@Override
	public void onTaskPostExecute(Object obj) {
		removeDialog(DIAGNOSIS_DIALOG);

		Diagnosis result = (Diagnosis) obj;

		if (result != null) {

			// In cases where the server cannot understand/decipher
			// input_text, we receive back a 'problem'. Test for the same
			// here
			if (result.getCurrent_query().equalsIgnoreCase("problem")) {

				// TODO Show Alert Dialog that the server could not
				// understand their request

				showAlert("Problem",
						result.getInput() + "\n" + result.getReply());

				Log.i(Constants.TAG, "Hapa Tu : " + result.getReply());

			} else {

				// add diagnosis object to adapter
				AdapterDiagnosis diag = new AdapterDiagnosis(result);
				chatAdapter.addItem(diag);

				// tell listeners that underlying data has changed. Refresh the
				// view
				chatAdapter.notifyDataSetChanged();

				// identify the view on display currently
				View currentView = flipper.getCurrentView();

				if (currentView.equals(menuView)) {
					// identify the number of children in the flipper
					int childCount = flipper.getChildCount();
					if (childCount > 1) {
						flipper.showNext();
					} else {
						flipper.addView(chatview);
						flipper.showNext();
					}

					// perhaps scroll to the last item if layout does not handle
					// this well

				} else if (currentView.equals(chatview)) {
					// TODO: Check if we really have to do nothing here
					// perhaps scroll to the last item if layout does not handle
					// this well

				}
			}
		}

	}

	@Override
	protected void onPause() {
		// mgr.cancel(pi);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// mgr.cancel(pi);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String text = intent.getStringExtra(Constants.LOG_SERVICE_OUT_MSG);

			Log.i(Constants.TAG, "onReceive under ResponseReceiver " + text);

			// Check if Log Type is register, and if so, mark is first run to
			// false
			if (text.equalsIgnoreCase("1")) {
				setNotFirstRun();
			}

		}
	}

	// custom adapter for the chat listview
	public class AliceChatAdapter extends BaseAdapter {
		ArrayList<AdapterDiagnosis> listitems;
		LayoutInflater inflater;
		Context context;

		public AliceChatAdapter(Context context) {
			this.context = context;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listitems = new ArrayList<AdapterDiagnosis>();

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listitems.size();
		}

		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			return listitems.get(index);
		}

		@Override
		public long getItemId(int index) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// retrieve currently selected item
			mDiagnosis = listitems.get(position);
			final int currPos =position;
			View row = null;
			
			Log.v(Constants.TAG, "THIS IS MY INPUT->"+mDiagnosis.getPrevText());
			

			// retrieve ID for discriminating the different views
			int diagnosisType = mDiagnosis.getResponse_type();

			switch (diagnosisType) {
			case 1:
				// TODO Response Type 1 - Show Confirm Dialog . This is
				// ignored for now
				break;
			case 2:

				// Response Type 2 - Show Options Dialog
				row = inflater.inflate(R.layout.diagnosis_options_chat, null);

				TextView optResp = (TextView) row
						.findViewById(R.id.options_txt_response);
				
				Button opt_close = (Button)row.findViewById(R.id.options_close);
				opt_close.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AliceChatAdapter.this.removeItem(currPos);
						if (currPos>1) {
							AliceChatAdapter.this.removeItem(currPos-1);
						}
						notifyDataSetChanged();
						
					}
				});
				

				optResp.setText(Html.fromHtml( mDiagnosis.getReply().toString()));

				LinearLayout optGroup = (LinearLayout) row
						.findViewById(R.id.options_response_options);

				HashMap<String, String> respOpts = mDiagnosis
						.getReply_options();
				
				int count = 0;

				for (Entry<String, String> entry : respOpts.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();
					
					Button bo = new Button(this.context);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					params.setMargins(10, 10, 10, 10);
					params.gravity = Gravity.CENTER;
					// params.height = 35;
					bo.setLayoutParams(params);
					bo.setGravity(Gravity.CENTER);

					bo.setPadding(10, 10, 10, 10);
					Drawable btnBg = getResources().getDrawable(R.drawable.radio_btn);

					bo.setBackgroundDrawable(btnBg);

					bo.setText(key);

					bo.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							AdapterDiagnosis diag = new AdapterDiagnosis(null);
							diag.setPrevText(key);
							listitems.add(diag);
							doTouchDiagnosis(mDiagnosis.getGuide(),
									mDiagnosis.getCurrent_query(), value);
						}
					});
					
					optGroup.addView(bo);
					count++;

					
				}

				break;
			case 3:
				// Response Type 3 - EMERGENCY - Map with nearest
				// hospital/doctor
				row = inflater.inflate(R.layout.diagnosis_map_chat, null);

				TextView mapResp = (TextView) row
						.findViewById(R.id.map_txt_response);
				Button map_close = (Button)row.findViewById(R.id.map_close);
				map_close.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AliceChatAdapter.this.removeItem(currPos);
						if (currPos>1) {
							AliceChatAdapter.this.removeItem(currPos-1);
						}
						notifyDataSetChanged();
						
					}
				});
				mapResp.setText(Html.fromHtml( mDiagnosis.getReply().toString()));

				MapView mapView = (MapView) row.findViewById(R.id.mapview);

				mapView.setBuiltInZoomControls(true);

				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = context.getResources().getDrawable(
						R.drawable.hospital);
				AliceItemizedOverlay itemizedoverlay = new AliceItemizedOverlay(
						drawable, context);
				GeoPoint point = new GeoPoint((int) -1.297322, (int) 36.792344);
				OverlayItem overlayitem = new OverlayItem(point,
						"Health Centre Location",
						"This is the nearest health centre");

				itemizedoverlay.addOverlay(overlayitem);

				mapOverlays.add(itemizedoverlay);

				MapController mapController = mapView.getController();

				mapController.animateTo(point); // attempt to center map

				mapController.setZoom(14); // this needs some
											// investigation
											// to realise best zoom
											// level

				break;
			case 4:
				// TODO Response Type 4 - CALL DOCTOR - Text with button
				// to
				// call
				// doctor.
				row = inflater.inflate(R.layout.diagnosis_calldoc_chat, null);

				TextView callResp = (TextView) row
						.findViewById(R.id.calldoc_txt_response);
				
				Button calldoc_close = (Button)row.findViewById(R.id.calldoc_close);
				calldoc_close.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AliceChatAdapter.this.removeItem(currPos);
						if (currPos>1) {
							AliceChatAdapter.this.removeItem(currPos-1);
						}
						notifyDataSetChanged();
						
					}
				});

				callResp.setText(Html.fromHtml( mDiagnosis.getReply().toString()));

				Button callBtn = (Button) row.findViewById(R.id.calldoc_btn);

				callBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(context, "Calling Doctor now",
								Toast.LENGTH_SHORT).show();

					}
				});

				break;
			case 5:
				// TODO Response Type 5 - INFORMATION - Text
				row = inflater.inflate(R.layout.diagnosis_information_chat,
						null);

				TextView infoResp = (TextView) row
						.findViewById(R.id.info_txt_response);
				
				Button info_close = (Button)row.findViewById(R.id.info_close);
				info_close.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AliceChatAdapter.this.removeItem(currPos);
						if (currPos>1) {
							AliceChatAdapter.this.removeItem(currPos-1);
						}
						
						notifyDataSetChanged();
						
					}
				});

				infoResp.setText(Html.fromHtml( mDiagnosis.getReply().toString()));

				break;

			case 6:
				// TODO Response Type 6 - EXIT THE CURRENT GUIDE - Text
				row = inflater.inflate(R.layout.diagnosis_information_chat,
						null);

				TextView infoResp2 = (TextView) row
						.findViewById(R.id.info_txt_response);

				infoResp2.setText(Html.fromHtml( mDiagnosis.getReply().toString()));

				break;
				
			case -2:
				// TODO Response Type 6 - EXIT THE CURRENT GUIDE - Text
				row = inflater.inflate(R.layout.diagnosis_input_chat,
						null);

				TextView inputQuery = (TextView) row
						.findViewById(R.id.input_text_query);
				inputQuery.setText(mDiagnosis.getPrevText());
				
				break;

			default:
				break;
			}

			return row;
		}

		private void resetAdapter() {
			listitems = new ArrayList<AdapterDiagnosis>();

		}

		private void addItem(AdapterDiagnosis diagnosis) {
			listitems.add(diagnosis);
		}

		private void removeItem(AdapterDiagnosis diagnosis) {
			listitems.remove(diagnosis);
		}
		
		private void removeItem(int position) {
			listitems.remove(position);
		}

	}

	// Start Voice Business

	public void startDictation(View view) {
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
			savedState.DialogLevel = _listeningDialog.getLevel();
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
		String DialogLevel;
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
							_listeningDialog.setLevel(Float
									.toString(_currentRecognizer
											.getAudioLevel()));
							_handler.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			public void onRecordingDone(Recognizer recognizer) {
				_listeningDialog.setText("Processing...");
				_listeningDialog.setLevel("");
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
				Log.i(Constants.TAG, detail + "\n" + suggestion);
				// updateCurrentText(detail + "\n" + suggestion, Color.GREEN,
				// false);
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
		// _arrayAdapter.clear();
		if (results.length > 0) {
			// setResult(results[0].getText());
			String t = results[0].getText();
			// String dialogue = "Me:  " + t;
			// TODO
			// updateCurrentText(dialogue, Color.WHITE, false);
			Log.i(Constants.TAG, t);
			// speakReply(askAlice(t));

			if (mDiagnosis != null) {
				doVoiceDiagnosis(mDiagnosis.getGuide(),
						mDiagnosis.getCurrent_query(), t);
			} else {
				doVoiceDiagnosis("null",Constants.VOICE_DEFAULT_LAST_QUERY, t);
			}

		} else {
			// setResult("");
		}
	}

	private void speakReply(String reply) {
		_lastTtsContext = new Object();
		_vocalizer.speakString(reply, _lastTtsContext);
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
					// the
					// animation stops working.
					Alice.this.removeDialog(LISTENING_DIALOG);
					createListeningDialog();
				}
			}
		});
	}

}