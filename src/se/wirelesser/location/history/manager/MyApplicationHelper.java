package se.wirelesser.location.history.manager;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}