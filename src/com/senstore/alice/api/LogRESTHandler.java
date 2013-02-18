/**
 * This is the Logging API Handler, with functions to handle API calls and responses
 */
package com.senstore.alice.api;

import android.util.Log;

import com.flurry.android.FlurryAgent;
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

	public ActionLog log(String log_type, String user_id, String user_phone) {

		String absoluteURL = Constants.SERVER_URL + "log.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", user_id);
		req.addParam("log_type", log_type);
		req.addParam("app_name", Constants.APP_NAME);
		req.addParam("tel_num", user_phone);
		
		if (log_type.equals(Constants.LOG_SEND_FEEDBACK)) {
			String message = Registry.instance().get(Constants.LOG_FEEDBACK_MSG)
					.toString();
			req.addParam("message", message);
		}
		
		Object loc = Registry.instance().get(Constants.REGISTRY_LOCATION);
		if (loc != null) {
			req.addParam("location",
					Registry.instance().get(Constants.REGISTRY_LOCATION)
							.toString());
		}
		Object country = Registry.instance().get(Constants.REGISTRY_COUNTRY);
		if (country != null) {
			req.addParam("country",
					Registry.instance().get(Constants.REGISTRY_COUNTRY)
							.toString());
		}
		Object state = Registry.instance().get(Constants.REGISTRY_STATE);
		if (state != null) {
			req.addParam("state",
					Registry.instance().get(Constants.REGISTRY_STATE)
							.toString());
		}
		Object locality = Registry.instance().get(Constants.REGISTRY_LOCALITY);
		if (locality != null) {
			req.addParam("locality",
					Registry.instance().get(Constants.REGISTRY_LOCALITY)
							.toString());
		}
		Object postalCode = Registry.instance().get(Constants.REGISTRY_POSTALCODE);
		if (postalCode != null) {
			req.addParam("postalCode",
					Registry.instance().get(Constants.REGISTRY_POSTALCODE)
							.toString());
		}
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
				FlurryAgent.onError("Log REST response error", "Response Code" + responseCode, req.getResponse());

			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "LogRestError" + e.fillInStackTrace());
			FlurryAgent.onError("Log REST error", "Error" + e.toString(), req.getFullURL());
		}

		return log;
	}

}
