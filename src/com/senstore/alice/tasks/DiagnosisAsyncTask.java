/**
 * 
 */
package com.senstore.alice.tasks;

import android.os.AsyncTask;

import com.senstore.alice.api.DiagnosisRESTHandler;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.models.Diagnosis;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class DiagnosisAsyncTask extends AsyncTask<Void, String, Diagnosis> {

	private AsyncTasksListener listener = null;

	private String health_guide = null;
	private String input_text = null;

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
		publishProgress("Diagnosing on remote server");
		DiagnosisRESTHandler handler = new DiagnosisRESTHandler();
		return handler.touchDiagnosis(health_guide, input_text);
	}

}
