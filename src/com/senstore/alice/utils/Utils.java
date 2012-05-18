/**
 * 
 */
package com.senstore.alice.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class Utils {

	public static String getDeviceID(Context mContext) {
		TelephonyManager tManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String uid = tManager.getDeviceId();
		return uid;
	}
}
