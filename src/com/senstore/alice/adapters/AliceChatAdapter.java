package com.senstore.alice.adapters;

import java.util.ArrayList;
import java.util.List;

import com.senstore.alice.models.Diagnosis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class AliceChatAdapter extends BaseAdapter{
	ArrayList<Diagnosis> listitems = new ArrayList<Diagnosis>();
	LayoutInflater inflater;
	
	public AliceChatAdapter(Context context) {
		// TODO Auto-generated constructor stub
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		// TODO Auto-generated method stub
		Diagnosis diagnosis = listitems.get(position);
		
		
		
		return null;
	}

}
