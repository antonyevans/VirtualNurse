/**
 * 
 */
package com.senstore.alice.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.flurry.android.FlurryAgent;
import com.senstore.alice.listeners.AsyncTasksListener;
import com.senstore.alice.listeners.LocationTasksListener;
import com.senstore.alice.models.Diagnosis;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

// AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
// we do not want to invoke it from the UI thread.
public class ReverseGeocodingTask extends AsyncTask<Location, Void, Address> {
    Context mContext;
    Location location;

    private LocationTasksListener listener = null;
    
    public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}


	public LocationTasksListener getListener() {
		return listener;
	}

	public void setListener(LocationTasksListener listener) {
		this.listener = listener;
	}
	
	/*public ReverseGeocodingTask(Context context) {
        super();
        mContext = context;
    }*/

	@Override
	protected void onPostExecute(Address address) {
		listener.onLocationTaskPostExecute(address);
		super.onPostExecute(address);
	}
	
    @Override
    protected Address doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        List<Address> addresses = null;
        try {
            // Call the synchronous getFromLocation() method by passing in the lat/long values.
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            //e.printStackTrace();
            //FlurryAgent.onError("Geocoder error", "Response Code" + e, "");
        } catch (Exception e){
			FlurryAgent.onError("Geocoder error", "Error: " + e, "");
		}
        
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            return address;
        }
        return null;
    }

}