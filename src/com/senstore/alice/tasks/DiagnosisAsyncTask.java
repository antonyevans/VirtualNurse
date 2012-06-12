/**
 * 
 */
package com.senstore.alice.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.senstore.alice.api.DiagnosisRESTHandler;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.utils.Constants;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class DiagnosisAsyncTask extends AsyncTask<Void, String, Diagnosis> {

	private AsyncTasksListener listener = null;

	private String health_guide = null;
	private String input_text = null;
	private String last_query = null;
	private boolean isVoice = false;

	public String getLast_query() {
		return last_query;
	}

	public void setLast_query(String last_query) {
		this.last_query = last_query;
	}

	public boolean isVoice() {
		return isVoice;
	}

	public void setVoice(boolean isVoice) {
		this.isVoice = isVoice;
	}

	public void setHealth_guide(String health_guide) {
		this.health_guide = health_guide;
	}

	public void setInput_text(String input_text) {
		this.input_text = input_text;
	}

	public void setListener(AsyncTasksListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		listener.onTaskProgress(values[0]);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Diagnosis result) {
		listener.onTaskPostExecute(result);
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		listener.onTaskPreExecute();
		super.onPreExecute();

	}

	@Override
	protected Diagnosis doInBackground(Void... params) {
		Diagnosis diagnosis = null;
		publishProgress("Diagnosing on remote server");
		DiagnosisRESTHandler handler = new DiagnosisRESTHandler();

		Log.i(Constants.TAG, "Setting the last_query to : " + last_query);

		if (isVoice()) {
			diagnosis = handler.voiceDiagnosis(last_query, input_text);
		} else {
			diagnosis = handler.touchDiagnosis(health_guide, last_query,
					input_text);
		}
		return diagnosis;
	}

}
