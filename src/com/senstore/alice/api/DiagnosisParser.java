/**
 * 
 */
package com.senstore.alice.api;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.utils.Constants;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class DiagnosisParser {

	public DiagnosisParser() {

	}

	/**
	 * 
	 * @param { created_at: "2012-05-25T05:20:15Z", current_query:
	 *        "birthcontrol_oct3", guide: "birthControlForWomen", id: 329,
	 *        input: "birthcontrol_oct3", last_query: "start", reply: "
	 *        <p>
	 *        Welcome to our Health Decision Guide on birth control for women.
	 *        </p>
	 *        <p>
	 *        Whether you are currently sexually active or have never been
	 *        sexually active, this guide will help you learn about the types of
	 *        birth control likely to fit your needs.
	 *        </p>
	 *        <p>
	 *        Although the guide is designed for women, we encourage men to
	 *        review our guide and discuss what they learn with their female
	 *        partners.
	 *        </p>
	 *        <p>
	 *        This guide is not meant to replace a visit with a health
	 *        professional.
	 *        </p>
	 *        <p>
	 *        <b>Are you already sexually active? That is, have you had sexual
	 *        intercourse?</b>
	 *        </p>
	 *        <p>
	 *        <InternalLink href="yesihavehadsexualintercourse.">Yes, I have had
	 *        sexual intercourse.</InternalLink>
	 *        </p>
	 *        <p>
	 *        <InternalLink href="noihavenothadsexualintercourse.">No, I have
	 *        not had sexual intercourse.</InternalLink>
	 *        </p>
	 *        ", response_type: "2", secret_hash: null, select_type: "touch",
	 *        updated_at: "2012-05-25T05:20:15Z", user_id: "1234" }
	 * @return {@link Diagnosis} object
	 */
	public Diagnosis parse(String jsonStr) {

		Diagnosis diag = new Diagnosis();

		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(jsonStr);

			String created_at = jsonObj.getString("created_at");
			String current_query = jsonObj.getString("current_query");
			String guide = jsonObj.getString("guide");
			String id = jsonObj.getString("id");
			String input = jsonObj.getString("input");
			String last_query = jsonObj.getString("last_query");
			String reply = jsonObj.getString("reply");
			String response_type = jsonObj.getString("response_type");
			String secret_hash = jsonObj.getString("secret_hash");
			String select_type = null;
			String updated_at = jsonObj.getString("updated_at");
			String user_id = jsonObj.getString("user_id");

			diag.setCreated_at(created_at);
			diag.setCurrent_query(current_query);
			diag.setGuide(guide);
			diag.setId(id);
			diag.setInput(input);
			diag.setReply_options(getReplyOptions(jsonObj));
			diag.setLast_query(last_query);
			diag.setReply(reply);
			diag.setResponse_type(response_type);
			diag.setSecret_hash(secret_hash);
			diag.setSelect_type(select_type);
			diag.setUpdated_at(updated_at);
			diag.setUser_id(user_id);

		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return diag;
	}

	private HashMap<String, String> getReplyOptions(JSONObject obj) {

		HashMap<String, String> options = new HashMap<String, String>();

		// Handle the reply options - text,link - K,V
		JSONArray reply_options;
		try {
			reply_options = obj.getJSONArray("options");

			// loop through the JSONArray and get all the items
			for (int i = 0; i < reply_options.length(); i++) {

				String text = reply_options.getJSONObject(i).getString("text")
						.toString();
				String link = reply_options.getJSONObject(i).getString("link")
						.toString();

				options.put(text, link);

				Log.i(Constants.TAG, text + " - " + link);
			}
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}
		return options;
	}
}
