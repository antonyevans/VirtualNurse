/**
 * 
 */
package com.senstore.alice.test;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.senstore.alice.harvard.R;
import com.senstore.alice.api.HarvardGuide;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.tasks.DiagnosisAsyncTask;
import com.senstore.alice.utils.Constants;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class DiagnosisAPITester extends Activity implements AsyncTasksListener {

	private static final int DIAGNOSIS_DIALOG = 0;
	private ProgressDialog mProgressDialog;

	final AsyncTasksListener listener = this;

	private DiagnosisAsyncTask diagnosisTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.api_test_diagnosis);
		createHarvardGuideWidget();
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

	/**
	 * Loop through the Harvard Guide enum, to fetch the guides and their
	 * respective properties
	 * 
	 * @Mimano This is where you put in your buttons/list in a scroll view
	 */
	private void createHarvardGuideWidget() {

		LinearLayout layout = (LinearLayout) findViewById(R.id.test_scroll_layout);

		for (HarvardGuide hg : HarvardGuide.values()) {

			final String name = hg.officialName();
			final String guide = hg.guideName();
			final String start_input = hg.startInput();

			Button b = new Button(this);
			b.setText(name);

			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					doDiagnosis(guide, start_input);
				}
			});
			layout.addView(b);
			Log.i(Constants.TAG, guide + " :: " + start_input);
		}
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

	private void showAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alert.show();
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
			int responseType = Integer.parseInt(result.getResponse_type());
			String resultText = result.getReply();

			switch (responseType) {
			case 1:
				// TODO Response Type 1 - Show Confirm Dialog
				showAlert(getString(R.string.app_name), resultText);

				break;
			case 2:
				// TODO Response Type 2 - Show Options Dialog
				showAlert(getString(R.string.app_name), resultText + "\n"
						+ printOptionsMap(result.getReply_options()));

				break;
			case 3:
				// TODO Response Type 3 - EMERGENCY - Map with nearest
				// hospital/doctor
				showAlert(getString(R.string.app_name), resultText);
				break;
			case 4:
				// TODO Response Type 4 - CALL DOCTOR - Text with button to call
				// doctor.
				showAlert(getString(R.string.app_name), resultText);
				break;
			case 5:
				// TODO Response Type 5 - INFORMATION - Text
				showAlert(getString(R.string.app_name), resultText);
				break;

			default:
				break;
			}
		} else {
			Log.e(Constants.TAG, "onTaskPostExecute returned null");
		}

	}

	private String printOptionsMap(HashMap<String, String> options) {
		String option="";
		for (Entry<String, String> entry : options.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			option += key + " - " + value + "\n";
		}

		return option;
	}

}
