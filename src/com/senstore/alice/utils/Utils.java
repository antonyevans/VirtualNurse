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

	private static Context mContext = (Context) Registry.instance().get(
			Constants.REGISTRY_CONTEXT);

	public static String getDeviceID() {
		TelephonyManager tManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String uid = tManager.getDeviceId();
		return uid;
	}
}
