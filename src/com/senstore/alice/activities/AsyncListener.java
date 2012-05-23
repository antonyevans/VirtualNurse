/**
 * 
 */
package com.senstore.alice.activities;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public interface AsyncListener {
	public void onTaskProgress(CharSequence message);
	public void onTaskComplete(Object obj);
}
