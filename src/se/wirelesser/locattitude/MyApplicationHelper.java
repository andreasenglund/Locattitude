package se.wirelesser.locattitude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.android.maps.GeoPoint;

public class MyApplicationHelper {
	private static SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
	private static String token = null;

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
	
	public static String epochToUTCShortDate(String utcEpochTimeStamp){
		Date utc = new Date(Long.valueOf(utcEpochTimeStamp));
		return shortDateFormatter.format(utc);
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

	public static String subtractMillisFromEpoch(String utcEpochTimeStamp, int numberOfMilliSecondsToSubract) throws ParseException { 
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

	private String uTCToEpoch(String utcDateString) throws ParseException{
		Date utc = longDateFormatter.parse(utcDateString);
		return String.valueOf(utc.getTime());
	}

	private Date uTCStringToDate(String utcDateString) throws ParseException{
		return longDateFormatter.parse(utcDateString);
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		MyApplicationHelper.token = token;
	}
}

class GeoPointEpochTime {
	
	public GeoPointEpochTime(GeoPoint geoPoint, String epochTime) {
		super();
		this.geoPoint = geoPoint;
		this.epochTime = epochTime;
	}
	
	GeoPoint geoPoint = null;
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}
	public String getEpochTime() {
		return epochTime;
	}
	public void setEpochTime(String epochTime) {
		this.epochTime = epochTime;
	}
	String epochTime = null;
}