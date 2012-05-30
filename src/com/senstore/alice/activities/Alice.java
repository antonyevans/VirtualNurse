package com.senstore.alice.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.commonsware.cwac.locpoll.LocationReceiver;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.senstore.alice.R;
import com.senstore.alice.api.HarvardGuide;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.overlays.AliceItemizedOverlay;
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

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 10, 10, 10);
			params.gravity = Gravity.CENTER;
			// params.height = 35;
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);

			b.setPadding(10, 10, 10, 10);
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
	public void doDiagnosis(String health_guide, String input_text) {
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
		ArrayList<Diagnosis> listitems;
		LayoutInflater inflater;
		Context context;

		public AliceChatAdapter(Context context) {
			this.context = context;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listitems = new ArrayList<Diagnosis>();

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
			final Diagnosis diagnosis = listitems.get(position);
			String type = diagnosis.getResponse_type();
			View row = null;

			if (type != null) {
				// retrive ID for discriminating the different views
				int diagnosisType = Integer.parseInt(type);

				switch (diagnosisType) {
				case 1:
					// TODO Response Type 1 - Show Confirm Dialog . This is
					// ignored for now
					break;
				case 2:

					// Response Type 2 - Show Options Dialog
					row = inflater.inflate(R.layout.diagnosis_options_chat,
							null);

					TextView optQuery = (TextView) row
							.findViewById(R.id.options_txt_query);
					TextView optResp = (TextView) row
							.findViewById(R.id.options_txt_response);

					optQuery.setText(diagnosis.getCurrent_query().toString());
					optResp.setText(diagnosis.getReply().toString());

					RadioGroup optGroup = (RadioGroup) row
							.findViewById(R.id.options_query_options);

					HashMap<String, String> respOpts = diagnosis
							.getReply_options();

					for (Entry<String, String> entry : respOpts.entrySet()) {
						final String key = entry.getKey();
						final String value = entry.getValue();

						RadioButton rbtn = new RadioButton(this.context);
						rbtn.setText(key);
						rbtn.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								doDiagnosis(diagnosis.getGuide(), value);
							}
						});
						optGroup.addView(rbtn);
					}

					break;
				case 3:
					// Response Type 3 - EMERGENCY - Map with nearest
					// hospital/doctor
					row = inflater.inflate(R.layout.diagnosis_map_chat, null);

					TextView mapQuery = (TextView) row
							.findViewById(R.id.map_txt_query);
					TextView mapResp = (TextView) row
							.findViewById(R.id.map_txt_response);

					mapQuery.setText(diagnosis.getCurrent_query().toString());
					mapResp.setText(diagnosis.getReply().toString());

					MapView mapView = (MapView) row.findViewById(R.id.mapview);

					mapView.setBuiltInZoomControls(true);

					List<Overlay> mapOverlays = mapView.getOverlays();
					Drawable drawable = context.getResources().getDrawable(
							R.drawable.hospital);
					AliceItemizedOverlay itemizedoverlay = new AliceItemizedOverlay(
							drawable, context);
					GeoPoint point = new GeoPoint((int) -1.297322,
							(int) 36.792344);
					OverlayItem overlayitem = new OverlayItem(point,
							"Health Centre Location",
							"This is the nearest health centre");

					itemizedoverlay.addOverlay(overlayitem);

					mapOverlays.add(itemizedoverlay);

					MapController mapController = mapView.getController();

					mapController.animateTo(point); // attempt to center map

					mapController.setZoom(14); // this needs some investigation
												// to realise best zoom level

					break;
				case 4:
					// TODO Response Type 4 - CALL DOCTOR - Text with button to
					// call
					// doctor.
					row = inflater.inflate(R.layout.diagnosis_calldoc_chat,
							null);

					TextView callQuery = (TextView) row
							.findViewById(R.id.calldoc_txt_query);
					TextView callResp = (TextView) row
							.findViewById(R.id.calldoc_txt_response);

					callQuery.setText(diagnosis.getCurrent_query().toString());
					callResp.setText(diagnosis.getReply().toString());

					Button callBtn = (Button) row
							.findViewById(R.id.calldoc_btn);

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

					TextView infoQuery = (TextView) row
							.findViewById(R.id.info_txt_query);
					TextView infoResp = (TextView) row
							.findViewById(R.id.info_txt_response);

					infoQuery.setText(diagnosis.getCurrent_query().toString());
					infoResp.setText(diagnosis.getReply().toString());

					break;

				case 6:
					// TODO Response Type 6 - EXIT THE CURRENT GUIDE - Text
					row = inflater.inflate(R.layout.diagnosis_information_chat,
							null);

					TextView infoQuery2 = (TextView) row
							.findViewById(R.id.info_txt_query);
					TextView infoResp2 = (TextView) row
							.findViewById(R.id.info_txt_response);

					infoQuery2.setText(diagnosis.getCurrent_query().toString());
					infoResp2.setText(diagnosis.getReply().toString());

					break;

				default:
					break;
				}

			}

			return row;
		}

		public void resetAdapter() {
			listitems = new ArrayList<Diagnosis>();

		}

		public void addItem(Diagnosis diagnosis) {
			listitems.add(diagnosis);
		}

	}

}