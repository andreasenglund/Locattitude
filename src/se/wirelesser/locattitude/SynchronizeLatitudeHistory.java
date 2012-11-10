package se.wirelesser.locattitude;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.latitude.Latitude;
import com.google.api.services.latitude.Latitude.Builder;
import com.google.api.services.latitude.Latitude.Location.Delete;
import com.google.api.services.latitude.Latitude.Location.Get;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.Latitude.Location.List;
import com.google.api.services.latitude.model.LocationFeed;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;


public class SynchronizeLatitudeHistory extends AsyncTask<String, Void, Boolean> {

	static SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
	final String AUTH_TOKEN_TYPE = "https://www.googleapis.com/auth/latitude.all.best";
	Latitude service = null;
	Activity activity;

	public SynchronizeLatitudeHistory(Activity activity) {
		super();
		this.activity = activity;
	}

	protected Boolean doInBackground(String... params) {
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		GoogleCredential credential = new GoogleCredential().setAccessToken(LatitudeAuthenticator.token);
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("IOExpection on complete sync");
			return false;
		}
		return true;
	}

	private void syncCompleteHistory() throws ParseException, IOException {
		LocationFeed locationFeed = null;
		MyDatabaseHelper.clearOldHistory();
		Calendar now = GregorianCalendar.getInstance();
		String maxEpochTime = String.valueOf(now.getTimeInMillis());
		do {
			locationFeed = getHistoryList(maxEpochTime);	
			if (locationFeed != null && locationFeed.getItems() != null && locationFeed.getItems().size() > 0){
				MyDatabaseHelper.insertLocations(locationFeed);
				Location lastInList = locationFeed.getItems().get(locationFeed.getItems().size() - 1);
				maxEpochTime = subtractMillisFromEpoch((String)lastInList.getTimestampMs(), 1);
			}
		} while (locationFeed != null && locationFeed.getItems() != null && locationFeed.getItems().size() > 0);
		
		createNotification();
	}

	private void createNotification() {
		
	}

	/** The system calls this to perform work in the UI thread and delivers
	 * the result from doInBackground() */
	protected void onPostExecute(Boolean success) {
		if(success){
			System.out.println("Successful Sync");
		} else {
			System.out.println("Failed Sync");
		}
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
	
	private boolean deleteLatitudeHistoryRecord(String utcEpochTimeStamp){
		try {
			Delete deleteLocation = service.location().delete(utcEpochTimeStamp);
			deleteLocation.execute();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}




	private String subtractMillisFromEpoch(String utcEpochTimeStamp, int numberOfMilliSecondsToSubract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(Long.valueOf(utcEpochTimeStamp));
		calendar.add(Calendar.MILLISECOND, -numberOfMilliSecondsToSubract);
		return String.valueOf(calendar.getTimeInMillis());
	}

	private String uTCToEpoch(String toUtcDateString, String numberOfDaysToSubtract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(uTCStringToDate(toUtcDateString));
		calendar.add(Calendar.DATE, -Integer.valueOf(numberOfDaysToSubtract));
		return String.valueOf(calendar.getTimeInMillis());
	}
	
	
	private String subtractDaysFromEpoch(String utcEpochTimeStamp, String numberOfDaysToSubtract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(Long.valueOf(utcEpochTimeStamp));
		calendar.add(Calendar.DATE, -Integer.valueOf(numberOfDaysToSubtract));
		return String.valueOf(calendar.getTimeInMillis());
	}
	

	private String epochToUTC(String utcEpochTimeStamp){
		Date utc = new Date(Long.valueOf(utcEpochTimeStamp));
		return longDateFormatter.format(utc);
	}

	private String uTCToEpoch(String utcDateString) throws ParseException{
		Date utc = longDateFormatter.parse(utcDateString);
		return String.valueOf(utc.getTime());
	}

	private Date uTCStringToDate(String utcDateString) throws ParseException{
		return longDateFormatter.parse(utcDateString);
	}
}
