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
	 * 
	 * http://sharp-waterfall-5241.herokuapp.com/harvard.json?guide=
	 * birthControlForWomen
	 * &input=birthcontrol_oct3&user_id=1234&select_type=touch
	 * &last_query=start&security=foobar
	 * 
	 * 
	 */
	public Diagnosis touchDiagnosis(String guide,String last_query, String input_text) {

		String absoluteURL = Constants.SERVER_URL + "harvard.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getPhoneNumber());
		req.addParam("user_text", input_text);
		req.addParam("guide", guide);
		req.addParam("last_query", last_query);
		req.addParam("input", input_text);
		req.addParam("select_type", Constants.DIAGNOSIS_TOUCH);
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 201) {
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

	/**
	 * 
	 * http://www.senstore.com/selectguide.json?input=birth+control&user_id=1234
	 * &select_type=voice&last_query=start&security=701319f
	 * db07288ca9f4bfae8b214b81d
	 * 
	 * 
	 */
	public Diagnosis voiceDiagnosis(String last_query, String input_text) {

		String absoluteURL = Constants.SERVER_URL + "harvard.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getPhoneNumber());
		req.addParam("last_query", last_query);
		req.addParam("input", input_text);
		req.addParam("select_type", Constants.DIAGNOSIS_VOICE);
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 201) {
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
