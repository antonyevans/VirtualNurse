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
	 * @param chat_length
	 *            : 0, created_at: "2012-05-19T09:31:10Z", guide: "1", id: 245,
	 *            input: "test", option1: "Yes", option2: "No", option3: null,
	 *            option4: null, option5: null, reply:
	 *            "Breast lumps guide selected, is this right?", response_type:
	 *            "1", secret_hash: null, select_type: "voice", updated_at:
	 *            "2012-05-19T09:31:10Z", user_id: "1234"
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
