/**
 * 
 */
package com.senstore.alice.api;

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

			String chat_length = jsonObj.getString("chat_length");
			String created_at = jsonObj.getString("created_at");
			String guide = jsonObj.getString("guide");
			String id = jsonObj.getString("id");
			String input = jsonObj.getString("input");
			String option1 = jsonObj.getString("option1");
			String option2 = jsonObj.getString("option2");
			String option3 = jsonObj.getString("option3");
			String option4 = jsonObj.getString("option4");
			String option5 = jsonObj.getString("option5");
			String reply = jsonObj.getString("reply");
			String response_type = jsonObj.getString("response_type");
			String secret_hash = jsonObj.getString("secret_hash");
			String select_type = null;
			String updated_at = jsonObj.getString("updated_at");
			String user_id = jsonObj.getString("user_id");

			diag.setChat_length(chat_length);
			diag.setCreated_at(created_at);
			diag.setGuide(guide);
			diag.setId(id);
			diag.setInput(input);
			diag.setOption1(option1);
			diag.setOption2(option2);
			diag.setOption3(option3);
			diag.setOption4(option4);
			diag.setOption5(option5);
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
}
