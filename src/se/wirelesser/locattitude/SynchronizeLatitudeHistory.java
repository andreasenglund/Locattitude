package se.wirelesser.locattitude;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.latitude.Latitude;
import com.google.api.services.latitude.Latitude.Builder;
import com.google.api.services.latitude.Latitude.Location.Get;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.Latitude.Location.List;
import com.google.api.services.latitude.model.LocationFeed;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class SynchronizeLatitudeHistory extends AsyncTask<String, String, Boolean> {

	private Latitude service = null;
	private Activity activity;
	private MyNotificationHelper myNotificationHelper;
	private String currentNotificationDate = null;

	public SynchronizeLatitudeHistory(Activity activity, Context context) {
		super();
		this.activity = activity;
	    myNotificationHelper = new MyNotificationHelper(context);
	}

    protected void onPreExecute(){
        //Create the notification in the statusbar
    	myNotificationHelper.createNotification();
    }

	protected Boolean doInBackground(String... params) {
		if(!authenticate()){
			return false;  
		}
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		GoogleCredential credential = new GoogleCredential().setAccessToken(MyApplicationHelper.getToken());
		Builder builder = new Builder(transport, new JacksonFactory(), credential);
		service = builder
				.setApplicationName("Locattitude")
				.setHttpRequestInitializer(credential)
				.setJsonHttpRequestInitializer(new GoogleKeyInitializer("AIzaSyB-DUXnyltfAwFknXWe98qsMYXq6dIUi2Y"))
				.build();

		try {
			syncCompleteHistory();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("ParseExpection on complete sync");
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("IOExpection on complete sync");
			return false;
		}
		return true;
	}

	private boolean authenticate() {
		
		AccountManagerFuture<Bundle> amf = MyApplicationHelper.accountManager.getAccountManager().getAuthToken(MyApplicationHelper.account, "oauth2:https://www.googleapis.com/auth/latitude.all.best", null, activity, null, null);
		String token;
		try {
			token = amf.getResult().getString(AccountManager.KEY_AUTHTOKEN);
			MyApplicationHelper.setToken(token);
			Thread.sleep(10000);
			if (MyApplicationHelper.getToken() != null){
				return true;
			}
		} catch (OperationCanceledException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AuthenticatorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;  
	}

	private void syncCompleteHistory() throws ParseException, IOException {
		LocationFeed locationFeed = null;
		MyDatabaseHelper.clearOldHistory();
		Calendar now = GregorianCalendar.getInstance();
		String maxEpochTime = String.valueOf(now.getTimeInMillis());
		do {
			locationFeed = getHistoryList(maxEpochTime);	
			if (locationFeed != null && locationFeed.getItems() != null && locationFeed.getItems().size() > 0){
				currentNotificationDate = MyApplicationHelper.epochToUTCShortDate(maxEpochTime);
				publishProgress(MyApplicationHelper.epochToUTCShortDate(maxEpochTime));
				MyDatabaseHelper.insertLocations(locationFeed);
				Location lastInList = locationFeed.getItems().get(locationFeed.getItems().size() - 1);
				maxEpochTime = MyApplicationHelper.subtractMillisFromEpoch((String)lastInList.getTimestampMs(), 1);
			}
		} while (locationFeed != null && locationFeed.getItems() != null && locationFeed.getItems().size() > 0);
	}

	/** The system calls this to perform work in the UI thread and delivers
	 * the result from doInBackground() */
	protected void onPostExecute(Boolean success) {
		myNotificationHelper.completed();
		if(success){
			System.out.println("Successful Sync");
		} else {
			System.out.println("Failed Sync");
		}
	}
	
    protected void onProgressUpdate(String... parameters) {
        myNotificationHelper.progressUpdate(parameters[0]);
    }

	private LocationFeed getHistoryList(String maxUtcEpochTimeStamp) throws IOException{
		List listLocation = service.location().list();
		listLocation.setGranularity("best");
		listLocation.setMaxResults("1000");
		listLocation.setMaxTime(maxUtcEpochTimeStamp);
		return listLocation.execute();
	}

	private LocationFeed getHistoryList(String fromUtcEpochTimeStamp, String toUtcEpochTimeStamp, String maxRecords) throws IOException{
		List listLocation = service.location().list();
		listLocation.setGranularity("best");
		listLocation.setMaxResults(maxRecords);
		listLocation.setMaxTime(toUtcEpochTimeStamp);
		listLocation.setMinTime(fromUtcEpochTimeStamp);
		return listLocation.execute();
	}

	private Location getLatitudeHistoryRecord(String utcEpochTimeStamp){
		try {
			Get getLocation = service.location().get(utcEpochTimeStamp);
			getLocation.setGranularity("best");
			return getLocation.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
