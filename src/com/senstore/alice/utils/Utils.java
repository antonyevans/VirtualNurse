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

	public static String getPhoneNumber() {
		String userNumber = null;
		TelephonyManager tManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String uid = tManager.getLine1Number();

		String imei = tManager.getDeviceId();

		boolean uidHasText = !"".equals(uid);
		boolean imeiHasText = !"".equals(imei);

		if (uid != null && uidHasText) {
			userNumber = uid;
		} else if (imei != null && imeiHasText) {
			// Use the IMEI
			userNumber = imei;
		} else {
			// Since both uid and imei returned null, Generate a random
			// number/value and use it
			RandomStr generator = new RandomStr();
			userNumber = generator.get(5);

		}
		return userNumber;

	}
}
