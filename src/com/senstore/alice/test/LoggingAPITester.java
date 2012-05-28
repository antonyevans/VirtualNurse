/**
 * 
 */
package com.senstore.alice.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.senstore.alice.R;
import com.senstore.alice.api.LogRESTHandler;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.ActionLog;
import com.senstore.alice.utils.Constants;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class LoggingAPITester extends Activity implements AsyncTasksListener {
	public static final int SYNC_DIALOG = 0;
	private ProgressDialog mProgressDialog;
	AsyncLogger logger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.api_test_logger);

		final AsyncTasksListener listener = this;

		((Button) findViewById(R.id.log_register))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger = new AsyncLogger();
						logger.setLog_type(Integer
								.toString(Constants.LOG_REGISTER));
						logger.setListener(listener);
						logger.execute();
					}
				});
		((Button) findViewById(R.id.log_location))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger = new AsyncLogger();
						logger.setLog_type(Integer
								.toString(Constants.LOG_LOCATION));
						logger.setListener(listener);
						logger.execute();
					}
				});
		((Button) findViewById(R.id.log_calldoctor))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger = new AsyncLogger();
						logger.setLog_type(Integer
								.toString(Constants.LOG_CALL_DOCTOR));
						logger.setListener(listener);
						logger.execute();
					}
				});
		((Button) findViewById(R.id.log_calldoctor_accept))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger = new AsyncLogger();
						logger.setLog_type(Constants.LOG_CALL_DOCTOR_ACCEPT);
						logger.setListener(listener);
						logger.execute();
					}
				});
		((Button) findViewById(R.id.log_calldoctor_reject))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger = new AsyncLogger();
						logger.setLog_type(Constants.LOG_CALL_DOCTOR_REJECT);
						logger.setListener(listener);
						logger.execute();
					}
				});
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
		case SYNC_DIALOG:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle(getString(R.string.app_name));
			mProgressDialog.setMessage("Synchronizing...");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		}
		return null;
	}

	@Override
	public void onTaskProgress(CharSequence message) {
		mProgressDialog.setMessage(message);

	}

	@Override
	public void onTaskPreExecute() {
		showDialog(SYNC_DIALOG);

	}

	@Override
	public void onTaskPostExecute(Object obj) {
		removeDialog(SYNC_DIALOG);
		ActionLog log = (ActionLog) obj;
		if (log != null) {
			showAlert(
					getString(R.string.app_name) + " - Log Type "
							+ log.getLogType(),
					"Log {" + log.getId() + "} On {" + log.getCreatedAt()
							+ "} in {" + log.getLocation() + "} by {"
							+ log.getUserId() + "}");
		} else {
			Log.e(Constants.TAG, "onTaskCompletion returned null");
		}

	}

	class AsyncLogger extends AsyncTask<Void, String, ActionLog> {
		String log_type = null;
		boolean isLocation = false;
		AsyncTasksListener listener = null;

		public void setListener(AsyncTasksListener listener) {
			this.listener = listener;
		}

		public String getLog_type() {
			return log_type;
		}

		public void setLog_type(String log_type) {
			this.log_type = log_type;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			listener.onTaskProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(ActionLog result) {
			listener.onTaskPostExecute(result);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			listener.onTaskPreExecute();
			super.onPreExecute();

		}

		@Override
		protected ActionLog doInBackground(Void... params) {
			publishProgress("Logging on remote server");
			LogRESTHandler handler = new LogRESTHandler();

			return handler.log(log_type);
		}

	}

}
