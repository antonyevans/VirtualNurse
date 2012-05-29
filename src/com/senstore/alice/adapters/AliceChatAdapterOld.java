package com.senstore.alice.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.senstore.alice.R;
import com.senstore.alice.models.Diagnosis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AliceChatAdapterOld extends BaseAdapter{
	ArrayList<Diagnosis> listitems;
	LayoutInflater inflater;
	Context context;
	
	public AliceChatAdapterOld(Context context) {
		this.context = context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listitems = new ArrayList<Diagnosis>();
		
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
		String type =diagnosis.getResponse_type();
		View row=null;
		
		if (type!=null) {
			//retrive ID for discriminating the different views
			int diagnosisType = Integer.parseInt(type);
			
			
			
			switch (diagnosisType) {
			case 1:
				// TODO Response Type 1 - Show Confirm Dialog . This is ignored for now
				break;
			case 2:
				
				// TODO Response Type 2 - Show Options Dialog
				row = inflater.inflate(R.layout.diagnosis_options_chat, null);
				
				TextView optQuery = (TextView)row.findViewById(R.id.options_text_query);
				TextView optResp = (TextView)row.findViewById(R.id.options_text_response);
				
				optQuery.setText(diagnosis.getCurrent_query().toString());
				optResp.setText(diagnosis.getReply().toString());
				
				RadioGroup optGroup = (RadioGroup)row.findViewById(R.id.options_query_options);
				
				HashMap<String, String> respOpts = diagnosis.getReply_options();
				
				Iterator it = respOpts.entrySet().iterator();
				
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
					RadioButton rb = new RadioButton(this.context);
					rb.setText(pairs.getValue());
					rb.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//maybe initiate connection to db here
						}
					});
					optGroup.addView(rb);
					
				}
				
				String sanitized = android.text.Html
				.fromHtml(diagnosis.getReply()).toString();
				break;
			case 3:
				// TODO Response Type 3 - EMERGENCY - Map with nearest
				// hospital/doctor
				

				break;
			case 4:
				// TODO Response Type 4 - CALL DOCTOR - Text with button to call
				// doctor.

				break;
			case 5:
				// TODO Response Type 5 - INFORMATION - Text
				break;

			default:
				break;
			}
			
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
