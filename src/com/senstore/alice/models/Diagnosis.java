/**
 * 
 */
package com.senstore.alice.models;

import java.util.HashMap;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class Diagnosis {

	private String created_at = null;
	private String current_query = null;
	private String guide = null;
	private String id = null;
	private String input = null;
	private String last_query = null;
	private String reply = null;
	private HashMap<String, String> reply_options = null;
	private String response_type = null;
	private String secret_hash = null;
	private String select_type = null;
	private String updated_at = null;
	private String user_id = null;

	private String query_string = null;

	public String getQuery_string() {
		return query_string;
	}

	public void setQuery_string(String query_string) {
		this.query_string = query_string;
	}

	public Diagnosis() {

	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCurrent_query() {
		return current_query;
	}

	public void setCurrent_query(String current_query) {
		this.current_query = current_query;
	}

	public String getGuide() {
		return guide;
	}

	public void setGuide(String guide) {
		this.guide = guide;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getLast_query() {
		return last_query;
	}

	public void setLast_query(String last_query) {
		this.last_query = last_query;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public HashMap<String, String> getReply_options() {
		return reply_options;
	}

	public void setReply_options(HashMap<String, String> reply_options) {
		this.reply_options = reply_options;
	}

	public String getResponse_type() {
		return response_type;
	}

	public void setResponse_type(String response_type) {
		this.response_type = response_type;
	}

	public String getSecret_hash() {
		return secret_hash;
	}

	public void setSecret_hash(String secret_hash) {
		this.secret_hash = secret_hash;
	}

	public String getSelect_type() {
		return select_type;
	}

	public void setSelect_type(String select_type) {
		this.select_type = select_type;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

}