/**
 * This is the Logging API Handler, with functions to handle API calls and responses
 */
package com.senstore.alice.api;

import android.util.Log;

import com.senstore.alice.http.RestClient;
import com.senstore.alice.http.RestClient.RequestMethod;
import com.senstore.alice.models.Purchase;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.utils.Utils;

/**
 * @author Antony Evans - antony@senstore.com
 * 
 */
public class PurchaseRESTHandler {

	private Purchase purchase = null;

	/**
	 * 
	 */
	public PurchaseRESTHandler() {

	}

	public Purchase purchase(String purchaseType) {
		
		String absoluteURL = Constants.SERVER_URL + "purchase.json";
		RestClient req = new RestClient(absoluteURL);
		req.addParam("user_id", Utils.getPhoneNumber());
		req.addParam("purchase", purchaseType);
		req.addParam("app_name", Constants.APP_NAME);
		req.addParam("security", Constants.SECURITY_HASH);

		try {
			req.execute(RequestMethod.GET);
			int responseCode = req.getResponseCode();
			if (responseCode == 201) {
				String responseBody = req.getResponse();
				Log.i(Constants.TAG, responseBody);

				// Set result as positive, might want to introduce more formal check here later by parsing the response
				//purchase.setPurchaseStated(true);

			} else {
				Log.i(Constants.TAG, responseCode + " - " + req.getResponse());
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return purchase;
	}

}
