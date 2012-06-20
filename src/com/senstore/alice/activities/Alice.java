package com.senstore.alice.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.senstore.alice.R;
import com.senstore.alice.api.HarvardGuide;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.location.AliceLocation;
import com.senstore.alice.location.AliceLocation.LocationResult;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.services.BackgroundLogger;
import com.senstore.alice.tasks.DiagnosisAsyncTask;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;
import com.senstore.alice.views.ChatListView;

public class Alice extends Activity implements AsyncTasksListener,
		TextToSpeech.OnInitListener {

	private boolean canCallDoctor = false;
	private TextToSpeech mTts;
	private boolean isTTSReady = false;

	private ResponseReceiver receiver;
	private String prevQuery = null;

	private static final int DIAGNOSIS_DIALOG = 0;

	final AsyncTasksListener listener = this;

	private DiagnosisAsyncTask diagnosisTask;

	private View chatview;
	private ChatListView chatlist;
	private ViewFlipper flipper;

	private AliceChatAdapter chatAdapter;

	private View menuView;

	private LayoutInflater inflater;

	private Diagnosis mDiagnosis;

	private static SpeechKit _speechKit;
	private static final int LISTENING_DIALOG = 1;
	private Handler _handler = null;
	private final Recognizer.Listener _listener;
	private Recognizer _currentRecognizer;
	private ListeningDialog _listeningDialog;
	private DiagnosisDialog _diagnosisDialog;
	private boolean _destroyed;

	private Vocalizer _vocalizer;
	private Object _lastTtsContext = null;

	private String talkResp = "";

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

		// Set Full Screen Since we have a Tittle Bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		initAliceLocation();

		setContentView(R.layout.main);
		inflater = getLayoutInflater();

		inflater = getLayoutInflater();

		// inflate flipper to switch between menu and chat screen
		flipper = (ViewFlipper) findViewById(R.id.alice_view_flipper);

		if (savedInstanceState != null) {
			int flipperPosition = savedInstanceState.getInt("FLIPPER_POSITION");
			flipper.setDisplayedChild(flipperPosition);
		}

		// inflate the view with the listview
		chatview = inflater.inflate(R.layout.alice_chat_list_layout, null);

		// load listview
		chatlist = (ChatListView) chatview.findViewById(R.id.alice_chat_list);
		chatlist.setFocusable(false);

		chatAdapter = new AliceChatAdapter(this);

		// Testing refresh
		chatlist.destroyDrawingCache();
		chatlist.setVisibility(ListView.INVISIBLE);
		chatlist.setVisibility(ListView.VISIBLE);

		chatlist.setAdapter(chatAdapter);

		// Load the main menu
		createHarvardGuideWidget();

		flipper.addView(menuView);

		// Register the Background Logger Broadcast Receiver
		initLogBroadcastReceiver();

		// Initialize the listening dialog
		createDiagnosisDialog();

		// Check if the app has been run before.
		if (isFirstRun()) {
			Log.i(Constants.TAG, "isFirstRun()");
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

	public void onHome(View view) {
		// identify the view on display currently
		View currentView = flipper.getCurrentView();

		if (currentView.equals(menuView)) {

			// perhaps scroll to the last item if layout does not handle
			// this well

		} else if (currentView.equals(chatview)) {
			// TODO: Check if we really have to do nothing here
			// perhaps scroll to the last item if layout does not handle
			// this well
			chatAdapter.resetAdapter();
			flipper.removeView(currentView);
			//

		}
		stopTTS();

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
			// params.setMargins(0, 5, 0, 5);
			params.setMargins(15, 5, 0, 5);
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

					// firstQuery = name;

					stopTTS();

					prevQuery = name;

					doTouchDiagnosis(guide,
							Constants.DIAGNOSIS_DEFAULT_LAST_QUERY, start_input);
				}
			});

			lightbox.addView(b);

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
	public void doVoiceDiagnosis(String health_guide, String last_query,
			String input_text) {
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
			return _diagnosisDialog;

		case LISTENING_DIALOG:
			return _listeningDialog;
		}

		return null;
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

		if (result != null) {

			// In cases where the server cannot understand/decipher
			// input_text, we receive back a 'problem'. Test for the same
			// here
			if (result.getCurrent_query().equalsIgnoreCase("problem")) {

				// TODO Show Alert Dialog that the server could not
				// understand their request

				showInfoAlert(getString(R.string.alert_dialog_title),
						result.getReply() + "\n" + "(" + result.getInput()
								+ ")");

			} else {

				String last_query = result.getLast_query();
				String select_type = result.getSelect_type();

				if (last_query
						.equalsIgnoreCase(Constants.DIAGNOSIS_DEFAULT_LAST_QUERY)
						&& select_type
								.equalsIgnoreCase(Constants.DIAGNOSIS_VOICE)) {

					prevQuery = result.getGuide();
				}

				// add diagnosis object to adapter
				chatAdapter.addItem(result);

				// tell listeners that underlying data has changed. Refresh the
				// view
				chatAdapter.notifyDataSetChanged();

				// chatlist.setSelectionFromTop(chatAdapter.getCount(), 10);
				chatlist.clearFocus();
				chatlist.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// chatlist.setSelection(chatAdapter.getCount() - 1);

						scrollToLastItem();

					}
				});

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

		int lastPosition = chatAdapter.getCount() - 1;

		chatlist.setSelection(lastPosition);
		speakText(talkResp);

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
	protected void onDestroy() {
		unregisterReceiver(receiver);
		killTTS();
		super.onDestroy();
	}

	public class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {
				String text = intent
						.getStringExtra(Constants.LOG_SERVICE_OUT_MSG);

				// Check if Log Type is register, and if so, mark is first run
				// to
				// false
				if (text != null && text.equalsIgnoreCase("1")) {
					setNotFirstRun();
				}
				if (text != null && text.equalsIgnoreCase("2")) {
					canCallDoctor = Boolean.getBoolean(Registry.instance()
							.get(Constants.REGISTRY_CALL).toString());
				}

				Log.i(Constants.TAG, "Successfully logged type " + text);

			}
		}

	}

	private void removeDiagnosisView(View view) {
		stopTTS();
		flipper.removeView(view);
	}

	// custom adapter for the chat listview
	public class AliceChatAdapter extends BaseAdapter {
		private List<Diagnosis> listitems;
		private LayoutInflater inflater;
		private Context context;

		public AliceChatAdapter(Context context) {
			this.context = context;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// retrieve currently selected item
			Alice.this.mDiagnosis = listitems.get(position);
			final int mposition = position;
			View row = null;

			TextView queryTxt = null;
			TextView responseTxt = null;

			// retrieve ID for discriminating the different views
			String type = mDiagnosis.getResponse_type();
			int diagnosisType = Integer.parseInt(type);

			switch (diagnosisType) {
			case 1:
				// TODO Response Type 1 - Show Confirm Dialog . This is
				// ignored for now
				break;
			case 2:

				Log.i(Constants.TAG,
						"mDiagnosis (case 2:) : " + mDiagnosis.getId());

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

							Alice.this.prevQuery = key;

							stopTTS();

							doTouchDiagnosis(mDiagnosis.getGuide(),
									mDiagnosis.getCurrent_query(), value);
						}
					});

					optGroup.addView(bo);

				}
				break;

			case 3:
				// Response Type 3 - EMERGENCY - Map with nearest
				// hospital/doctor
				row = inflater.inflate(R.layout.diagnosis_map_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.map_txt_response);
				Button map_close = (Button) row.findViewById(R.id.map_close);
				map_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
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

				Button mapBtn = (Button)row.findViewById(R.id.emergency_btn);
				mapBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					}
				});

				break;
			case 4:
				// TODO Response Type 4 - CALL DOCTOR - Text with button
				// to call doctor.
				row = inflater.inflate(R.layout.diagnosis_calldoc_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.calldoc_txt_response);

				Button calldoc_close = (Button) row
						.findViewById(R.id.calldoc_close);
				calldoc_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
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

						stopTTS();

						doLog(Integer.toString(Constants.LOG_CALL_DOCTOR));

						if (canCallDoctor) {
							showCallAlert(getString(R.string.app_name),
									getString(R.string.call_doctor_text));

						} else {
							showInfoAlert(getString(R.string.app_name),
									getString(R.string.call_doctor_unavailable));
						}

					}
				});

				break;
			case 5:
				// TODO Response Type 5 - INFORMATION - Text
				row = inflater.inflate(R.layout.diagnosis_information_chat,
						null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.info_txt_response);

				Button info_close = (Button) row.findViewById(R.id.info_close);
				info_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
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

				break;

			case 6:

				// Response Type 6 - Exit current guide and open new one. In the
				// options array you should have just one option, which the link
				// name will contain the name of the guide to link to. You
				// should then respond with the start of that guide (you have
				// already the codes to do this).
				row = inflater.inflate(R.layout.diagnosis_options_chat, null);

				queryTxt = (TextView) row.findViewById(R.id.options_text_query);

				responseTxt = (TextView) row
						.findViewById(R.id.options_txt_response);

				Button opt6_close = (Button) row
						.findViewById(R.id.options_close);
				opt6_close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

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

							Alice.this.prevQuery = key;

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

				queryTxt.setText(mDiagnosis.getQuery_string());
				String toTrim = mDiagnosis.getReply().trim();
				responseTxt.setText(Html.fromHtml(toTrim));

			}

			return row;
		}

		public void addItem(Diagnosis diagnosis) {

			diagnosis.setQuery_string(prevQuery);

			talkResp = diagnosis.getReply().replaceAll("<(.|\n)*?>", "");
			listitems.add(diagnosis);
		}

		public void removeItem(int position) {
			stopTTS();
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
				Log.i(Constants.TAG, detail + " - " + suggestion);

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

			Log.i(Constants.TAG, "Voice Results : " + t);

			if (mDiagnosis != null) {
				// Set the voice input as the query string in the Diagnosis
				// object
				// TODO Voice response should match with the selected guide
				// option, rather than the input text (sometimes the input text
				// looks wrong or is badly spelled)

				prevQuery = mDiagnosis.getGuide();

				doVoiceDiagnosis(mDiagnosis.getGuide(),
						mDiagnosis.getCurrent_query(), t);

			} else {
				doVoiceDiagnosis("null",
						Constants.DIAGNOSIS_DEFAULT_LAST_QUERY, t);
			}

		} else {

		}
	}

	private void speakText(String text) {

		if (isTTSReady) {
			mTts.stop();
			mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
		} else {
			// TODO Cannot speak
			Log.i(Constants.TAG, "Cannot speak. TTS Engine not ready");
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
		if (status == TextToSpeech.SUCCESS) {
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

				// Speak the welcome text
				speakText(getString(R.string.hello));
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
				Log.i(Constants.TAG, "Logging location " + loc);
				Registry.instance().put(Constants.REGISTRY_LOCATION, loc);

				doLog(Integer.toString(Constants.LOG_LOCATION));

			}

		}
	};

}