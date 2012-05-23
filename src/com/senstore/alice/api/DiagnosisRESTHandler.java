/**
 * This is the diagnoses API Handler, handling the requests and responses.
 */
package com.senstore.alice.api;

import android.util.Log;

import com.senstore.alice.http.RestClient;
import com.senstore.alice.http.RestClient.RequestMethod;
import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Utils;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class DiagnosisRESTHandler {
	private Diagnosis diagnosis = null;

	/**
	 * 
	 */
	public DiagnosisRESTHandler() {

	}

	/**
	 * Build a server request with
	 * 
	 * @param input_text
	 *            that is either selection text on screen, or voice input
	 * @param input_source
	 *            that is either "voice" or "touch"
	 * @param chat_length
	 * @return {@link Diagnosis} object
	 */
	public Diagnosis diagnose(String input_text, String input_source,
			String chat_length) {

		String absoluteURL = Constants.SERVER_URL + "harvard.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_ID", Utils.getPhoneNumber());
		req.addParam("user_text", input_text);
		req.addParam("chat_length", chat_length);
		req.addParam("select_type", input_source);
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);

				// Proceed to parse result
				DiagnosisParser parser = new DiagnosisParser();
				diagnosis = parser.parse(responseBody);

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return diagnosis;
	}

}
