/**
 * 
 */
package com.senstore.alice.listeners;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public interface AsyncTasksListener {
	public void onTaskPreExecute();
	public void onTaskProgress(CharSequence message);
	public void onTaskPostExecute(Object obj);
}
