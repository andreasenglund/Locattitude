package se.wirelesser.wwwt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class MyApplicationHelper {
	
	protected static final int QUESTION_WHAT_DATES_WERE_I_IN_CITY = 0;
	protected static final int QUESTION_WHAT_DATES_WERE_I_IN_AREA = 1;
	protected static final int QUESTION_WHAT_DATES_WERE_I_IN_PLACE = 2;
	protected static final int QUESTION_WHAT_DAYS_DO_I_USUALLY_COME_HERE_CITY = 3;
	protected static final int QUESTION_WHAT_DAYS_DO_I_USUALLY_COME_HERE_AREA = 4;
	protected static final int QUESTION_WHAT_DAYS_DO_I_USUALLY_COME_HERE_PLACE = 5;
	protected static final int QUESTION_WHEN_DO_I_ARRIVE_AND_LEAVE_WORK = 6;
	protected static final int QUESTION_WHEN_DO_I_LEAVE_AND_ARRIVE_HOME_FROM_WORK = 7;
	protected static final String questionTypeString = "TypeOfQuestion";
	
	
	private static SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat longDateFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");
	private static String token = null;
	
	public static GoogleAccountManager accountManager = null;
	public static MyDatabase myDatabase;
	public static Account account = null;
	
	public static List<GeoPoint> pointsToDraw = null;
	
	private static Double latConversionRatio = 0.0000117;
	private static Double longConversionRatio = 0.000009;
	
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
	
	public static double microDegreesToDegreesDouble(int microDegrees) {
		return microDegrees / 1E6;
	}
	
	public static int degreesToMicroDegrees(double degrees) {
		return (int)(degrees * 1E6);
	}
	
	public static int degreesToMicroDegrees(String degrees) {
		return degreesToMicroDegrees(Double.valueOf(degrees));
	}

	public static ArrayList<GeoPointEpochTime> getGeoPointsForDate(String date) {
		return MyDatabaseHelper.getGeoPointsForDate(date);
	}
	
	public static ArrayList<GeoPointEpochTime> getGeoPointsSequential (String fromEpochTime, int numberOfPoints) {
		return MyDatabaseHelper.getGeoPoints(fromEpochTime, numberOfPoints);
	}
	
	public static ArrayList<GeoPointEpochTime> getSequenceStartingPoints(String date, String longitude, String latitude, int withinRadiusMeters) {
		return MyDatabaseHelper.getSequenceStartingPoints(date, longitude, latitude, withinRadiusMeters);
	}
	
	public static ArrayList<GeoPointEpochTime> getGeoPointsAtLocationForDate(String date, String longitude, String latitude, int withinRadiusMeters) {
		return MyDatabaseHelper.getGeoPointsAtLocationForDate(date, longitude, latitude, withinRadiusMeters);
	}

	public static String subtractMillisFromEpoch(String utcEpochTimeStamp, int numberOfMilliSecondsToSubract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(Long.valueOf(utcEpochTimeStamp));
		calendar.add(Calendar.MILLISECOND, -numberOfMilliSecondsToSubract);
		return String.valueOf(calendar.getTimeInMillis());
	}

	public static String uTCToEpoch(String toUtcDateString, String numberOfDaysToSubtract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(uTCStringToDate(toUtcDateString));
		calendar.add(Calendar.DATE, -Integer.valueOf(numberOfDaysToSubtract));
		return String.valueOf(calendar.getTimeInMillis());
	}
	
	
	public static String subtractDaysFromEpoch(String utcEpochTimeStamp, String numberOfDaysToSubtract) throws ParseException { 
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(Long.valueOf(utcEpochTimeStamp));
		calendar.add(Calendar.DATE, -Integer.valueOf(numberOfDaysToSubtract));
		return String.valueOf(calendar.getTimeInMillis());
	}

	public static String uTCToEpoch(String utcDateString) throws ParseException{
		Date utc = longDateFormatter.parse(utcDateString);
		return String.valueOf(utc.getTime());
	}

	public static Date uTCStringToDate(String utcDateString) throws ParseException{
		return longDateFormatter.parse(utcDateString);
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		MyApplicationHelper.token = token;
	}

	public static boolean isWithinThresholdOfPoint(GeoPoint startPoint,
			GeoPoint geoPointFromSeqList, int radius) {
		LatLongLowHigh latLongLowHigh = getLatLongLowHigh(MyApplicationHelper.microDegreesToDegrees(startPoint.getLongitudeE6()), MyApplicationHelper.microDegreesToDegrees(startPoint.getLatitudeE6()), radius);
		if (MyApplicationHelper.degreesToMicroDegrees(latLongLowHigh.getLatLow()) <= geoPointFromSeqList.getLatitudeE6() 
				&& geoPointFromSeqList.getLatitudeE6() <= MyApplicationHelper.degreesToMicroDegrees(latLongLowHigh.getLatHigh())   
				&& MyApplicationHelper.degreesToMicroDegrees(latLongLowHigh.getLongLow()) <= geoPointFromSeqList.getLongitudeE6()
				&& geoPointFromSeqList.getLongitudeE6() <= MyApplicationHelper.degreesToMicroDegrees(latLongLowHigh.getLongHigh())){
			return true;
		}
		return false;
	}
	
	public static LatLongLowHigh getLatLongLowHigh(String longitude, String latitude, int radius) {
		Double latLow = Double.parseDouble(latitude) - (latConversionRatio*radius);
		Double latHigh = Double.parseDouble(latitude) + (latConversionRatio*radius);
		Double longLow = Double.parseDouble(longitude) - (longConversionRatio*radius);
		Double longHigh = Double.parseDouble(longitude) + (longConversionRatio*radius);
		return new LatLongLowHigh(latLow, latHigh, longLow, longHigh);
	}

	public static ArrayList<GeoPointEpochTime> getSequencesAtLocationIncludingFirstPointNotAtLocation(String longitude, String latitude, int radius) {
		return MyDatabaseHelper.getSequencesAtLocationIncludingFirstPointNotAtLocation(longitude, latitude, radius);
	}

	public static LatLng toLatLng(GeoPoint geoPoint) {
		return new LatLng(MyApplicationHelper.microDegreesToDegreesDouble(geoPoint.getLatitudeE6()), MyApplicationHelper.microDegreesToDegreesDouble(geoPoint.getLongitudeE6()));
	}
	
}

class GeoPointEpochTime {
	
	private GeoPoint geoPoint = null;
	private String epochTime = null;
	
	public GeoPointEpochTime(GeoPoint geoPoint, String epochTime) {
		super();
		this.geoPoint = geoPoint;
		this.epochTime = epochTime;
	}
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
}

class QuestionTypeData {
	
	private int radius = 0;
	
	public QuestionTypeData(int radius) {
		super();
		this.radius = radius;
	}
	
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
}

class ResponseListDateData {
	
	private int radius = 0;
	
	public ResponseListDateData(int radius) {
		super();
		this.radius = radius;
	}
	
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
}

class QuestionResponseData {
	
	private List<String> menuItemStrings = null;
	private List<MenuItem> menuItems = new ArrayList<MenuItem>();
	
	public QuestionResponseData(List<String> menuItemsStrings) {
		super();
		this.setMenuItemStrings(menuItemsStrings);
	}

	public List<String> getMenuItemStrings() {
		return menuItemStrings;
	}

	public void setMenuItemStrings(List<String> menuItemStrings) {
		this.menuItemStrings = menuItemStrings;
	}

	public List<MenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<MenuItem> menuItems) {
		this.menuItems = menuItems;
	}
}

class MenuItem implements Parcelable{
	
	private List<GeoPoint> geoPoints = null;
	
	public MenuItem(List<GeoPoint> geoPoints) {
		super();
		this.setGeoPoints(geoPoints);
	}

	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}

	public void setGeoPoints(List<GeoPoint> geoPoints) {
		this.geoPoints = geoPoints;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}

class LatLongLowHigh {
	
	private Double latLow = null;
	private Double latHigh = null;
	private Double longLow = null;
	private Double longHigh = null;
	
	public LatLongLowHigh(Double latLow, Double latHigh, Double longLow, Double longHigh) {
		this.latLow = latLow;
		this.latHigh = latHigh;
		this.longLow = longLow;
		this.longHigh = longHigh;
	}
	
	public Double getLatLow() {
		return latLow;
	}

	public void setLatLow(Double latLow) {
		this.latLow = latLow;
	}

	public Double getLatHigh() {
		return latHigh;
	}

	public void setLatHigh(Double latHigh) {
		this.latHigh = latHigh;
	}

	public Double getLongLow() {
		return longLow;
	}

	public void setLongLow(Double longLow) {
		this.longLow = longLow;
	}

	public Double getLongHigh() {
		return longHigh;
	}

	public void setLongHigh(Double longHigh) {
		this.longHigh = longHigh;
	}
}