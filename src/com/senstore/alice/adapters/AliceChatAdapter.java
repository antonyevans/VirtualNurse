package com.senstore.alice.adapters;

import java.util.ArrayList;
import java.util.List;

import com.senstore.alice.R;
import com.senstore.alice.models.Diagnosis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AliceChatAdapter extends BaseAdapter{
	ArrayList<Diagnosis> listitems;
	LayoutInflater inflater;
	
	public AliceChatAdapter(Context context) {
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listitems = new ArrayList<Diagnosis>();
		
		//add dummy Object for first row. Always displays the menu on opening alice
		listitems.add(new Diagnosis());
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listitems.size();
	}

	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return listitems.get(index);
	}

	@Override
	public long getItemId(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//retrieve currently selected item
		Diagnosis diagnosis = listitems.get(position);
		
		//retrive ID for discriminating the different views
		int diagnosisType = Integer.parseInt(diagnosis.getResponse_type());
		
		//inflating test Ui
		View row=null;
		
		row = inflater.inflate(R.layout.view_test_flip_list, null);
		TextView tv = (TextView)row.findViewById(R.id.diag_test);
		
		switch (diagnosisType) {
		case 1:
			// TODO Response Type 1 - Show Confirm Dialog
			tv.setText("-->"+diagnosis.getCurrent_query() +"--"+diagnosis.getInput()+"--"+diagnosis.getReply()+"--"+diagnosis.getReply());
			break;
		case 2:
			
			// TODO Response Type 2 - Show Options Dialog
			String sanitized = android.text.Html
			.fromHtml(diagnosis.getReply()).toString();
			tv.setText(sanitized+"-->"+diagnosis.getCurrent_query() +"--"+diagnosis.getInput()+"--"+diagnosis.getReply()+"--"+diagnosis.getReply());
			break;
		case 3:
			// TODO Response Type 3 - EMERGENCY - Map with nearest
			// hospital/doctor
			
			tv.setText("-->"+diagnosis.getCurrent_query() +"--"+diagnosis.getInput()+"--"+diagnosis.getReply()+"--"+diagnosis.getReply());

			break;
		case 4:
			// TODO Response Type 4 - CALL DOCTOR - Text with button to call
			// doctor.
			tv.setText("-->"+diagnosis.getCurrent_query() +"--"+diagnosis.getInput()+"--"+diagnosis.getReply()+"--"+diagnosis.getReply());

			break;
		case 5:
			// TODO Response Type 5 - INFORMATION - Text
			tv.setText("-->"+diagnosis.getCurrent_query() +"--"+diagnosis.getInput()+"--"+diagnosis.getReply()+"--"+diagnosis.getReply());
			break;

		default:
			break;
		}
		
		
		
		
		
		
		
		
		return row;
	}
	
	public void resetAdapter() {
		listitems = new ArrayList<Diagnosis>();
		
	}
	
	public void addItem(Diagnosis diagnosis) {
		listitems.add(diagnosis);
	}

}
