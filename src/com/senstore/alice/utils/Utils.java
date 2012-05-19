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

		if (uid != null) {
			userNumber = uid;
		} else {
			// Since it returned null, Generate a random number/value
			RandomStr generator = new RandomStr();
			userNumber = generator.get(5);

		}
		return userNumber;

	}
}
