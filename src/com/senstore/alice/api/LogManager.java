/**
 * 
 */
package com.senstore.alice.api;

import android.util.Log;

import com.senstore.alice.http.RestClient;
import com.senstore.alice.http.RestClient.RequestMethod;
import com.senstore.alice.models.ActionLog;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Utils;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class LogManager {

	/**
	 * 
	 */
	public LogManager() {

	}

	/**
	 * Register this device with the server and @return {@link ActionLog} object
	 */
	public ActionLog register() {

		String absoluteURL = Constants.SERVER_URL + "log.xml";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getDeviceID());
		req.addParam("log_type", Integer.toString(Constants.LOG_REGISTER));
		req.addParam("location", "");
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);
				// Parse the results

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return null;
	}

	/**
	 * Log this device's location and @return {@link ActionLog} object
	 */
	public ActionLog location() {
		String absoluteURL = Constants.SERVER_URL + "log.xml";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getDeviceID());
		req.addParam("log_type", Integer.toString(Constants.LOG_LOCATION));
		req.addParam("location", "");
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);
				// Parse the results

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return null;
	}

	/**
	 * Log the call doctor action and @return {@link ActionLog} object
	 */
	public ActionLog callDoctor() {
		String absoluteURL = Constants.SERVER_URL + "log.xml";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getDeviceID());
		req.addParam("log_type", Integer.toString(Constants.LOG_CALL_DOCTOR));
		req.addParam("location", "");
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);
				// Parse the results

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return null;
	}

	/**
	 * Log the accepted call doctor action and @return {@link ActionLog} object
	 */
	public ActionLog acceptCallDoctor() {
		String absoluteURL = Constants.SERVER_URL + "log.xml";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getDeviceID());
		req.addParam("log_type", Constants.LOG_CALL_DOCTOR_ACCEPT);
		req.addParam("location", "");
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);
				// Parse the results

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return null;
	}

	/**
	 * Log the rejected call doctor action and @return {@link ActionLog} object
	 */
	public ActionLog rejectCallDoctor() {
		String absoluteURL = Constants.SERVER_URL + "log.xml";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getDeviceID());
		req.addParam("log_type", Constants.LOG_CALL_DOCTOR_REJECT);
		req.addParam("location", "");
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 200) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);
				// Parse the results

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return null;
	}

}
