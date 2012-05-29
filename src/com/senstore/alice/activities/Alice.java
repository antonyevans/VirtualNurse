package com.senstore.alice.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.commonsware.cwac.locpoll.LocationReceiver;
import com.senstore.alice.R;
import com.senstore.alice.adapters.AliceChatAdapter;
import com.senstore.alice.api.HarvardGuide;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.services.BackgroundLogger;
import com.senstore.alice.tasks.DiagnosisAsyncTask;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;

public class Alice extends Activity implements AsyncTasksListener {

	// 2 minutes(120000)
	// 5 minutes(300000)
	// 10 minutes(600000)
	// 30 minutes(1800000)
	private static final int PERIOD = 120000; // 2 minutes
	private PendingIntent pi = null;
	private AlarmManager mgr = null;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Registry.instance().put(Constants.REGISTRY_CONTEXT,
				getApplicationContext());

		setContentView(R.layout.main);

		//
		SavedState savedState = (SavedState) getLastNonConfigurationInstance();
		if (savedState != null) {
			flipper = savedState.flipper;
		}

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

		flipper.addView(menuView);

		// Register the Background Logger Broadcast Receiver
		initLogBroadcastReceiver();

		initLocationService();

		// Check if the app has been run before.
		if (isFirstRun()) {
			Log.i(Constants.TAG, "isFirstRun()");
			// Start a background Task, to register the current user/device
			doLog(Integer.toString(Constants.LOG_REGISTER));

		}// else proceed with the normal app flow

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

			Drawable btnBg = getResources().getDrawable(R.drawable.buttonbgb);

			b.setBackgroundDrawable(btnBg);

			b.setText(name);

			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doDiagnosis(guide, start_input);
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

	private void initLocationService() {
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
	 * Creates an instance of {@link DiagnosisAsyncTask}, and sets the
	 * {@link AsyncTasksListener}
	 * 
	 * @param health_guide
	 *            and
	 * @param input_text
	 *            and then executes the request
	 */
	private void doDiagnosis(String health_guide, String input_text) {
		diagnosisTask = new DiagnosisAsyncTask();
		diagnosisTask.setListener(listener);
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
		}
		return null;
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

			// add diagnosis object to adapter
			chatAdapter.addItem(result);

			// tell listeners that underlying data has changed. Refrash the view
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

		if (result != null) {
			int responseType = Integer.parseInt(result.getResponse_type());

			Log.i(Constants.TAG, "Response Type = " + responseType);

			switch (responseType) {
			case 1:
				// TODO Response Type 1 - Show Confirm Dialog

				break;
			case 2:
				// TODO Response Type 2 - Show Options Dialog

				String sanitized = android.text.Html
						.fromHtml(result.getReply()).toString();

				Log.i(Constants.TAG, sanitized);

				break;
			case 3:
				// TODO Response Type 3 - EMERGENCY - Map with nearest
				// hospital/doctor
				break;
			case 4:
				// TODO Response Type 4 - CALL DOCTOR - Text with button to call
				// doctor.
				break;
			case 5:
				// TODO Response Type 5 - INFORMATION - Text
				break;

			default:
				break;
			}
		} else {
			Log.e(Constants.TAG, "onTaskPostExecute returned null");
		}

	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		if (flipper != null) {
			SavedState savedState = new SavedState();
			savedState.flipper = flipper;
		}

		return super.onRetainNonConfigurationInstance();
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
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private class SavedState {
		Object Context;
		ViewFlipper flipper;
		AsyncTasksListener listener;
		LayoutInflater inflater;
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

}