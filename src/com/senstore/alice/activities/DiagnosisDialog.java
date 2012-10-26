package com.senstore.alice.activities;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.senstore.alice.harvard.R;

class DiagnosisDialog extends Dialog {
	private String _text;

	public DiagnosisDialog(Activity owner) {
		super(owner);

		_text = null;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_diagnosis);
		setOwnerActivity(owner);
		setCancelable(true);

		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.gravity = Gravity.BOTTOM;
		layout.width = WindowManager.LayoutParams.FILL_PARENT;
	}

	public void setText(String text) {
		_text = text;
		TextView t = (TextView) findViewById(R.id.text_asking);
		if (t != null) {
			t.setText(text);
		}
	}

	public String getText() {
		return _text;
	}

	public void setStoppable(boolean stoppable) {

	}

}
