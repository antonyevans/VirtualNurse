/**
 * 
 */
package com.senstore.alice.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.senstore.alice.models.ActionLog;
import com.senstore.alice.utils.Constants;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class LogParser {

	public LogParser() {

	}

	/**
	 * 
	 * @param created_at
	 *            : "2012-05-19T07:05:24Z", id: 18, location: "california",
	 *            log_type: "1", secret_hash: null, updated_at:
	 *            "2012-05-19T07:05:24Z", user_id: "1234"
	 * @return {@link ActionLog} object
	 */
	public ActionLog parse(String jsonStr) {

		ActionLog log = new ActionLog();

		JSONObject api;
		try {

			api = new JSONObject(jsonStr);

			JSONObject jsonObj = api.getJSONObject("record");

			String created_at = jsonObj.getString("created_at");
			String id = jsonObj.getString("id");
			String location = jsonObj.getString("location");
			String log_type = jsonObj.getString("log_type");
			String secret_hash = jsonObj.getString("secret_hash");
			String updated_at = jsonObj.getString("updated_at");
			String user_id = jsonObj.getString("user_id");

			log.setCreatedAt(created_at);
			log.setId(id);
			log.setLocation(location);
			log.setLogType(log_type);
			log.setSecretHash(secret_hash);
			log.setUpdatedAt(updated_at);
			log.setUserId(user_id);

		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return log;
	}
}
