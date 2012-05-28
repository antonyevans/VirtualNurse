/**
 * 
 */
package com.senstore.alice.services;

import com.senstore.alice.api.LogRESTHandler;
import com.senstore.alice.utils.Constants;

import android.app.IntentService;
import android.content.Intent;

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

		msg = intent.getStringExtra(Constants.LOG_SERVICE_TYPE);
		LogRESTHandler handler = new LogRESTHandler();

		switch (identifyType(msg)) {
		case ERROR: {
			break;
		}
		case REGISTER:
			handler.log(msg);
			break;
		case LOCATION:
			handler.log(msg);
			break;
		case CALL_DOCTOR:
			handler.log(msg);
			break;
		case CALL_DOCTOR_ACCEPT:
			handler.log(msg);
			break;
		case CALL_DOCTOR_REJECT:
			handler.log(msg);
			break;
		default:
			break;
		}

	}

}
