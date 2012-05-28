/**
 * 
 */
package com.senstore.alice.services;

import com.senstore.alice.api.LogRESTHandler;
import com.senstore.alice.models.ActionLog;
import com.senstore.alice.utils.Constants;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class BackgroundLogger extends IntentService {

	private String msg = "-1";

	private static final int ERROR = -1;
	private static final int REGISTER = 0;
	private static final int LOCATION = 1;
	private static final int CALL_DOCTOR = 2;
	private static final int CALL_DOCTOR_ACCEPT = 3;
	private static final int CALL_DOCTOR_REJECT = 4;

	String[] TYPES = { "1", "2", "3", "4a", "4b" };

	/**
	 * This function finds the corresponding "enum" integer to the given data
	 * type
	 * 
	 * @param action
	 * @return
	 */
	private int identifyType(String type) {
		for (int i = 0; i < TYPES.length; ++i) {
			if (TYPES[i].equalsIgnoreCase(type)) {
				return i;
			}
		}
		return ERROR;
	}

	public BackgroundLogger() {
		super(Constants.LOG_SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(Constants.TAG, "onHandleIntent() called");

		msg = intent.getStringExtra(Constants.LOG_SERVICE_IN_MSG);
		LogRESTHandler handler = new LogRESTHandler();

		switch (identifyType(msg)) {
		case ERROR: {
			break;
		}
		case REGISTER:
			Log.i(Constants.TAG, "onHandleIntent() REGISTER");
			ActionLog log = handler.log(msg);
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(Constants.ACTION_RESP);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent
					.putExtra(Constants.LOG_SERVICE_OUT_MSG, log.getLogType());
			sendBroadcast(broadcastIntent);
			break;
		case LOCATION:
			Log.i(Constants.TAG, "onHandleIntent() LOCATION");
			ActionLog logloc = handler.log(msg);
			Intent locIntent = new Intent();
			locIntent.setAction(Constants.ACTION_RESP);
			locIntent.addCategory(Intent.CATEGORY_DEFAULT);
			locIntent.putExtra(Constants.LOG_SERVICE_OUT_MSG, logloc.getLogType());
			sendBroadcast(locIntent);
			break;
		case CALL_DOCTOR:
			Log.i(Constants.TAG, "onHandleIntent() CALL_DOCTOR");
			ActionLog logcd = handler.log(msg);
			Intent cdIntent = new Intent();
			cdIntent.setAction(Constants.ACTION_RESP);
			cdIntent.addCategory(Intent.CATEGORY_DEFAULT);
			cdIntent.putExtra(Constants.LOG_SERVICE_OUT_MSG, logcd.getLogType());
			sendBroadcast(cdIntent);
			break;
		case CALL_DOCTOR_ACCEPT:
			Log.i(Constants.TAG, "onHandleIntent() CALL_DOCTOR_ACCEPT");
			ActionLog logcda = handler.log(msg);
			Intent cdaIntent = new Intent();
			cdaIntent.setAction(Constants.ACTION_RESP);
			cdaIntent.addCategory(Intent.CATEGORY_DEFAULT);
			cdaIntent.putExtra(Constants.LOG_SERVICE_OUT_MSG, logcda.getLogType());
			sendBroadcast(cdaIntent);
			break;
		case CALL_DOCTOR_REJECT:
			Log.i(Constants.TAG, "onHandleIntent() CALL_DOCTOR_REJECT");
			ActionLog logcdr = handler.log(msg);
			Intent cdrIntent = new Intent();
			cdrIntent.setAction(Constants.ACTION_RESP);
			cdrIntent.addCategory(Intent.CATEGORY_DEFAULT);
			cdrIntent.putExtra(Constants.LOG_SERVICE_OUT_MSG, logcdr.getLogType());
			sendBroadcast(cdrIntent);
			break;
		default:
			break;
		}

	}

}
