/**
 * 
 */
package com.senstore.alice.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.senstore.alice.models.Diagnosis;
import com.senstore.alice.utils.Constants;
import com.senstore.alice.models.Guide;

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

		JSONObject JSONresponse;
		
		try {
			JSONresponse = new JSONObject(jsonStr);
			
			if (JSONresponse.has("api")) {
				
				JSONObject jsonObj = JSONresponse.getJSONObject("api");
	
				String created_at = jsonObj.getString("created_at");
				String current_query = jsonObj.getString("current_query");
				String guide = jsonObj.getString("guide");
				String id = jsonObj.getString("id");
				String input = jsonObj.getString("input");
				String last_query = jsonObj.getString("last_query");
				String reply = jsonObj.getString("reply");
				String response_type = jsonObj.getString("response_type");
				String secret_hash = jsonObj.getString("secret_hash");
				String select_type = jsonObj.getString("select_type");
				String updated_at = jsonObj.getString("updated_at");
				String user_id = jsonObj.getString("user_id");
				Boolean purchased = true;
	
				diag.setCreated_at(created_at);
				diag.setCurrent_query(current_query);
				diag.setGuide(guide);
				diag.setId(id);
				diag.setInput(input);
				diag.setPurchased(purchased);
	
				if (hasOptions(jsonObj)) {
					diag.setReply_options(getReplyOptions(jsonObj));
				}
				diag.setLast_query(last_query);
				diag.setReply(reply);
				diag.setResponse_type(response_type);
				diag.setSecret_hash(secret_hash);
				diag.setSelect_type(select_type);
				diag.setUpdated_at(updated_at);
				diag.setUser_id(user_id);
				diag.setHas_guides(false);

			} else if	(JSONresponse.has("guides")) {
				//case when we are getting list of guides back
				JSONArray jsonGuides = JSONresponse.getJSONArray("guides");
				diag.setHas_guides(true);
				List <Guide> guides = new ArrayList <Guide>();
				
				for(int i = 0; i < jsonGuides.length(); i++) {
					JSONObject items = jsonGuides.getJSONObject(i);
					Guide guide = new Guide();
					guide.setSimpleName(items.optString("SimpleName"));
					guide.setFileName(items.optString("FileName"));
					guide.setStartOption(items.optString("StartOption"));
					guides.add(guide);
					
				}
				
				
				diag.setGuides(guides);
				
				
				diag.setInput(JSONresponse.optString("User_input"));
				diag.setReply(JSONresponse.optString("Alice"));
				
				
			} else {
				Boolean purchased = false;
				diag.setPurchased(purchased);
			}
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		return diag;
	}

	private boolean checkPurchaseError(String error) {
		return true;
	}
	
	private boolean hasOptions(JSONObject obj) {
		boolean hasOptions = false;
		Object optionsObject;
		try {
			optionsObject = obj.get("options");

			if (optionsObject == JSONObject.NULL) {
				hasOptions = false;
			} else {
				hasOptions = true;
			}
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}
		return hasOptions;

	}

	private HashMap<String, String> getReplyOptions(JSONObject obj) {

		HashMap<String, String> options = new HashMap<String, String>();

		try {

			// Handle the reply options - text,link - K,V
			JSONArray reply_options;

			reply_options = obj.getJSONArray("options");

			// loop through the JSONArray and get all the items
			for (int i = 0; i < reply_options.length(); i++) {

				String text = reply_options.getJSONObject(i).getString("text")
						.toString();
				String link = reply_options.getJSONObject(i).getString("link")
						.toString();

				options.put(text, link);

				
			}

		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}
		return options;
	}
}
