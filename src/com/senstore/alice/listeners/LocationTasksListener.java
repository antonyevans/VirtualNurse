/**
 * 
 */
package com.senstore.alice.listeners;

import android.location.Address;

/**
 * @author Antony Evans - antony@senstore.com
 * 
 */
public interface LocationTasksListener {
	public void onLocationTaskPostExecute(Address address);
}
