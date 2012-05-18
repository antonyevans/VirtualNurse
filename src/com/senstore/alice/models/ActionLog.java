/**
 * 
 */
package com.senstore.alice.models;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class ActionLog {

	private String id = null;
	private String userId = null;
	private String logType = null;
	private String location = null;
	private String secretHash = null;
	private String createdAt = null;
	private String updatedAt = null;

	/**
	 * <record> <id type="integer">8</id> <user-id>1234</user-id>
	 * <log-type>3</log-type> <location>california</location> <secret-hash
	 * nil="true"/> <created-at
	 * type="datetime">2012-05-18T14:13:48Z</created-at> <updated-at
	 * type="datetime">2012-05-18T14:13:48Z</updated-at> </record>
	 */
	public ActionLog() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSecretHash() {
		return secretHash;
	}

	public void setSecretHash(String secretHash) {
		this.secretHash = secretHash;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

}
