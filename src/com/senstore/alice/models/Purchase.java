/**
 * 
 */
package com.senstore.alice.models;

/**
 * @author Antony Evans - Antony@senstore.com
 * 
 */
public class Purchase {


	private String id = null;
	private String userId = null;
	private String secretHash = null;
	private String purchase = null;
	private Boolean purchased = false;


	public Purchase() {

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


	public String getSecretHash() {
		return secretHash;
	}

	public void setSecretHash(String secretHash) {
		this.secretHash = secretHash;
	}

	public String getPurchase() {
		return purchase;
	}

	public void setPurchase(String purchase) {
		this.purchase = purchase;
	}
	
	public Boolean getPurchasedState() {
		return purchased;
	}
	
	public void setPurchasedState(Boolean purchased) {
		this.purchased = purchased;
	}

}
