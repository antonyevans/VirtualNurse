package com.senstore.alice.models;

import java.util.HashMap;

import com.senstore.alice.utils.Constants;

public class AdapterDiagnosis {
	private Diagnosis diagnosis;
	private int type = Constants.INPUT_TYPE;
	
	private String prevText="";
			
	public AdapterDiagnosis(Diagnosis diagnosis) {
		super();
		this.diagnosis = diagnosis;
	}
	
	public String getCreated_at() {
		return diagnosis.getCreated_at();
	}

	

	public String getCurrent_query() {
		return diagnosis.getCurrent_query();
	}


	public String getGuide() {
		return diagnosis.getGuide();
	}


	public String getId() {
		return diagnosis.getId();
	}


	public String getInput() {
		return diagnosis.getInput();
	}

	

	public String getLast_query() {
		return diagnosis.getLast_query();
	}


	public String getReply() {
		return diagnosis.getReply();
	}


	public HashMap<String, String> getReply_options() {
		return diagnosis.getReply_options();
	}

	

	public int getResponse_type() {
		if (diagnosis!=null) {
			String type = diagnosis.getResponse_type();
			int diagnosisType = Integer.parseInt(type);
			return diagnosisType;
		}else{
			return this.type;
		}
	}

	

	public String getSecret_hash() {
		return diagnosis.getSecret_hash();
	}

	

	public String getSelect_type() {
		return diagnosis.getSelect_type();
	}

	

	public String getUpdated_at() {
		return diagnosis.getUpdated_at();
	}


	public String getUser_id() {
		return diagnosis.getUser_id();
	}

	public String getPrevText() {
		return prevText;
	}

	public void setPrevText(String prevText) {
		this.prevText = prevText;
	}
	
	

	
	
	

}
