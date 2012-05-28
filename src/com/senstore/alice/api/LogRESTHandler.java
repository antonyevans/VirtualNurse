/**
 * This is the Logging API Handler, with functions to handle API calls and responses
 */
package com.senstore.alice.api;

import android.util.Log;

import com.senstore.alice.http.RestClient;
import com.senstore.alice.http.RestClient.RequestMethod;
import com.senstore.alice.models.ActionLog;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Registry;
import com.senstore.alice.utils.Utils;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class LogRESTHandler {

	private ActionLog log = null;

	/**
	 * 
	 */
	public LogRESTHandler() {

	}

	public ActionLog log(String log_type) {

		String absoluteURL = Constants.SERVER_URL + "log.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getPhoneNumber());
		req.addParam("log_type", log_type);

		req.addParam("location",
				Registry.instance().get(Constants.REGISTRY_LOCATION).toString());
		// req.addParam("location", "Nairobi");

		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 201) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);

				// Proceed to parse the results
				LogParser parser = new LogParser();
				log = parser.parse(responseBody);

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return log;
	}

}
