package com.senstore.alice.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;






/**
 * @author Mimano Muthondu gmimano@bityarn.co.ke
 * 
 * Trying to resolve some List selection issues via extending
 *
 */
public class ChatListView extends ListView {

	public ChatListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ChatListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ChatListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
	}
	
	
	/* (non-Javadoc)
	 * @see android.widget.AbsListView#smoothScrollToPosition(int)
	 */
	@Override
	public void smoothScrollToPosition(int position) {
		// TODO Auto-generated method stub
		super.smoothScrollToPosition(position);
	}
	
	
	

	
	
	
	
	
	
	

}
