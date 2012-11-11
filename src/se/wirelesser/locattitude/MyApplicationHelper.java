package se.wirelesser.locattitude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;

public class MyApplicationHelper {
	public SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");

	public MyApplicationHelper(SimpleDateFormat shortDateFormatter) {
		this.shortDateFormatter = shortDateFormatter;
	}
	
	public static String objectToString(Object object) {
		if (object == null){
			return null;
		}
		return String.valueOf(object);
	}

	public static Long objectToLong(Object object) {
		if (object == null){
			return null;
		}
		return Long.valueOf(objectToString(object));
	}
	
	public static Double objectToDouble(Object object) {
		if (object == null){
			return null;
		}
		return Double.valueOf(objectToString(object));
	}

	public static String epochToUTC(String utcEpochTimeStamp){
		Date utc = new Date(Long.valueOf(utcEpochTimeStamp));
		return longDateFormatter.format(utc);
	}
	
	public static String microDegreesToDegrees(int microDegrees) {
		
		return Double.toString(microDegrees / 1E6);
	}
	
	public static int degreesToMicroDegrees(double degrees) {
		
		return (int)(degrees * 1E6);
	}

	public static ArrayList<GeoPoint> getGeoPointsForDay(String date) {
		return MyDatabaseHelper.getGeoPointsForDate(date);
		
	}
}