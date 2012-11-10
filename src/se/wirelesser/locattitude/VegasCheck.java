package se.wirelesser.locattitude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.google.api.services.latitude.Latitude;

import android.app.Activity;
import android.os.AsyncTask;


public class VegasCheck extends AsyncTask<String, Void, Boolean> {

	SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
	Latitude service = null;
	Activity activity;

	public VegasCheck(Activity activity) {
		super();
		this.activity = activity;
	}

	protected Boolean doInBackground(String... params) {
		ArrayList<String> datesInVegas = MyDatabaseHelper.getDatesAtLocation("-115.22952","36.20550", 25000);
		for (String string : datesInVegas) {
			System.out.println(string);
		}
		return true;
	}

}
