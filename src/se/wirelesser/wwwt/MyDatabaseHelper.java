package se.wirelesser.wwwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.UUID;

import com.google.android.maps.GeoPoint;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.model.LocationFeed;

import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class MyDatabaseHelper {

	private static MyDatabase myDatabaseWrapper = MyApplicationHelper.myDatabase;
	private static SQLiteDatabase myWriteableDatabase = myDatabaseWrapper.getWritableDatabase();
	private static SQLiteDatabase myReadableDatabase = myDatabaseWrapper.getReadableDatabase();
	private static InsertHelper iHelp = new InsertHelper(myWriteableDatabase, MyDatabase.LOCATION_HISTORY_TABLE_NAME);
    private static final String LOCATION_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + MyDatabase.LOCATION_HISTORY_TABLE_NAME + 
            " (id INTEGER PRIMARY KEY, " +
            " epochTime INTEGER, " +
            " utcTime DATETIME, " +
            " latitude REAL, " +
            " longitude REAL, " +
            " accuracy INTEGER, " +
            " speed INTEGER, " +
            " heading INTEGER, " +
            " altitude INTEGER, " +
            " altitudeAccuracy INTEGER, " +
            " activityId TEXT);";

	public static boolean insertLocations(LocationFeed locationFeed) {

		myWriteableDatabase.beginTransaction();
		try {
			for (Location location : locationFeed.getItems()) {
				iHelp.prepareForInsert();
				iHelp.bind(2, MyApplicationHelper.objectToLong(location.getTimestampMs()));
				iHelp.bind(3, MyApplicationHelper.epochToUTC(MyApplicationHelper.objectToString(location.getTimestampMs())));
				iHelp.bind(4, MyApplicationHelper.objectToDouble(location.getLatitude()));
				iHelp.bind(5, MyApplicationHelper.objectToDouble(location.getLongitude()));

				if (MyApplicationHelper.objectToLong(location.getAccuracy()) != null){
					iHelp.bind(6, MyApplicationHelper.objectToLong(location.getAccuracy()));
				}

				if (MyApplicationHelper.objectToLong(location.getSpeed()) != null){
					iHelp.bind(7, MyApplicationHelper.objectToLong(location.getSpeed()));
				}
				
				if (MyApplicationHelper.objectToLong(location.getHeading()) != null){
					iHelp.bind(8, MyApplicationHelper.objectToLong(location.getHeading()));
				}

				if (MyApplicationHelper.objectToLong(location.getAltitude()) != null){
					iHelp.bind(9, MyApplicationHelper.objectToLong(location.getAltitude()));
				}

				if (MyApplicationHelper.objectToLong(location.getAltitudeAccuracy()) != null){
					iHelp.bind(10, MyApplicationHelper.objectToLong(location.getAltitudeAccuracy()));
				}
				
				if (MyApplicationHelper.objectToString(location.getActivityId()) != null){
					iHelp.bind(11, MyApplicationHelper.objectToString(location.getActivityId()));
				}
				
				iHelp.execute();
			}

			myWriteableDatabase.setTransactionSuccessful();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(locationFeed.values().toString());
			String errMsg = (e.getMessage() == null) ? "bulkInsert failed" : e.getMessage();
			System.out.println(errMsg);
			return false;
		}
		finally {
			myWriteableDatabase.endTransaction();
		}
		return true;
	}
	
	public static ArrayList<GeoPointEpochTime> getGeoPoints(String fromEpochTime, int numberOfPoints) {

		String SQL = "SELECT DISTINCT " 
				+ MyDatabase.LATITUDE_FIELD 
				+ " AS latitude, "
				+ MyDatabase.LONGITUDE_FIELD 
				+ " AS longitude, "
				+ MyDatabase.EPOCHTIME_FIELD 
				+ " AS epochTime FROM "
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE " 
				+ MyDatabase.EPOCHTIME_FIELD 
				+ " > ? ORDER BY epochTime LIMIT " + numberOfPoints;

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{fromEpochTime});

		ArrayList<GeoPointEpochTime> geoPoints = new ArrayList<GeoPointEpochTime>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
			String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
			geoPoints.add(new GeoPointEpochTime(new GeoPoint(latitudeE6, longitudeE6), epochTime));
			cursor.moveToNext();
		}
		cursor.close();
		return geoPoints;
	}
	
	public static ArrayList<String> getDatesAtLocation(String longitude, String latitude, int withinRadiusMeters) {

		LatLongLowHigh latLongLowHigh = MyApplicationHelper.getLatLongLowHigh(longitude, latitude, withinRadiusMeters);

		String latLow = String.valueOf(latLongLowHigh.getLatLow());
		String latHigh = String.valueOf(latLongLowHigh.getLatHigh());
		String longLow = String.valueOf(latLongLowHigh.getLongLow());
		String longHigh = String.valueOf(latLongLowHigh.getLongHigh());

		String SQL = "SELECT DISTINCT SUBSTR(" 
				+ MyDatabase.UTCTIME_FIELD 
				+ ", 1, 10)  AS utcTime FROM " 
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE " 
				+ MyDatabase.LATITUDE_FIELD
				+ " > ? AND " 
				+ MyDatabase.LATITUDE_FIELD
				+ " < ? AND "
				+ MyDatabase.LONGITUDE_FIELD
				+ " > ? AND "
				+ MyDatabase.LONGITUDE_FIELD
				+ " < ?"; 

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{latLow, latHigh, longLow, longHigh});

		ArrayList<String> datesAtLocation = new ArrayList<String>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			datesAtLocation.add(cursor.getString(cursor.getColumnIndex(MyDatabase.UTCTIME_FIELD)));
			cursor.moveToNext();
		}
		cursor.close();
		return datesAtLocation;
	}
	
	public static ArrayList<GeoPointEpochTime> getSequenceStartingPoints(String date, String longitude, String latitude, int withinRadiusMeters) {
		
		LatLongLowHigh latLongLowHigh = MyApplicationHelper.getLatLongLowHigh(longitude, latitude, withinRadiusMeters);

		Double latLow = latLongLowHigh.getLatLow();
		Double latHigh = latLongLowHigh.getLatHigh();
		Double longLow = latLongLowHigh.getLongLow();
		Double longHigh = latLongLowHigh.getLongHigh();

		String SQL = "SELECT DISTINCT " 
				+ MyDatabase.LATITUDE_FIELD 
				+ " AS latitude, "
				+ MyDatabase.LONGITUDE_FIELD 
				+ " AS longitude, "
				+ MyDatabase.EPOCHTIME_FIELD 
				+ " AS epochTime FROM "
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE SUBSTR(" + MyDatabase.UTCTIME_FIELD
				+ ", 1, 10) = ? ORDER BY epochTime";

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{date});

		ArrayList<GeoPointEpochTime> geoPoints = new ArrayList<GeoPointEpochTime>();
		cursor.moveToFirst();
		boolean isSequenceStarted = false;
		while(!cursor.isAfterLast()) {
			if (latLow < cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD))
					&& latHigh > cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD))
					&& longLow < cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD))
					&& longHigh > cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD))){
				if(!isSequenceStarted){
					int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
					int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
					String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
					geoPoints.add(new GeoPointEpochTime(new GeoPoint(latitudeE6, longitudeE6), epochTime));
					isSequenceStarted = true;
				}
			} else {
				isSequenceStarted = false;
			}
			cursor.moveToNext();
		}
		cursor.close();
		return geoPoints;
	}
	
	public static ArrayList<GeoPointEpochTime> getGeoPointsAtLocationForDate(String date, String longitude, String latitude, int withinRadiusMeters) {

		LatLongLowHigh latLongLowHigh = MyApplicationHelper.getLatLongLowHigh(longitude, latitude, withinRadiusMeters);

		String latLow = String.valueOf(latLongLowHigh.getLatLow());
		String latHigh = String.valueOf(latLongLowHigh.getLatHigh());
		String longLow = String.valueOf(latLongLowHigh.getLongLow());
		String longHigh = String.valueOf(latLongLowHigh.getLongHigh());
		
		String SQL = "SELECT DISTINCT " 
				+ MyDatabase.LATITUDE_FIELD 
				+ " AS latitude, "
				+ MyDatabase.LONGITUDE_FIELD 
				+ " AS longitude, "
				+ MyDatabase.EPOCHTIME_FIELD 
				+ " AS epochTime FROM "
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE " + MyDatabase.LATITUDE_FIELD
				+ " > ? AND " 
				+ MyDatabase.LATITUDE_FIELD
				+ " < ? AND "
				+ MyDatabase.LONGITUDE_FIELD
				+ " > ? AND "
				+ MyDatabase.LONGITUDE_FIELD
				+ " < ? AND "
				+ "SUBSTR(" + MyDatabase.UTCTIME_FIELD
				+ ", 1, 10) = ? ORDER BY epochTime";

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{latLow, latHigh, longLow, longHigh, date});

		ArrayList<GeoPointEpochTime> geoPoints = new ArrayList<GeoPointEpochTime>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
			String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
			geoPoints.add(new GeoPointEpochTime(new GeoPoint(latitudeE6, longitudeE6), epochTime));
			cursor.moveToNext();
		}
		cursor.close();
		return geoPoints;
	}
	
	public static ArrayList<GeoPointEpochTime> getSequencesAtLocationIncludingFirstPointNotAtLocation(String longitude, String latitude, int withinRadiusMeters) {

		LatLongLowHigh latLongLowHigh = MyApplicationHelper.getLatLongLowHigh(longitude, latitude, withinRadiusMeters);

		String latLow = String.valueOf(latLongLowHigh.getLatLow());
		String latHigh = String.valueOf(latLongLowHigh.getLatHigh());
		String longLow = String.valueOf(latLongLowHigh.getLongLow());
		String longHigh = String.valueOf(latLongLowHigh.getLongHigh());
		
		String SQL = "SELECT DISTINCT id, latitude, longitude, epochTime FROM locationHistory WHERE id IN" 
				+ " (SELECT DISTINCT id - 1 AS id FROM locationHistory" 
				+ " WHERE latitude > " + latLow + " AND latitude < " + latHigh + " AND longitude > " + longLow + " AND longitude < " + longHigh 
				+ " UNION SELECT DISTINCT id AS id FROM locationHistory WHERE latitude > " + latLow + " AND latitude < " + latHigh + " AND longitude > " + longLow + " AND longitude < " + longHigh + ")"
				+ " ORDER BY id desc";

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{});

		ArrayList<GeoPointEpochTime> geoPoints = new ArrayList<GeoPointEpochTime>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
			String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
			geoPoints.add(new GeoPointEpochTime(new GeoPoint(latitudeE6, longitudeE6), epochTime));
			cursor.moveToNext();
		}
		cursor.close();
		return geoPoints;
	}
	
	public static ArrayList<GeoPointEpochTime> getGeoPointsForDate(String date) {

		String SQL = "SELECT DISTINCT " 
				+ MyDatabase.LATITUDE_FIELD 
				+ " AS latitude, "
				+ MyDatabase.LONGITUDE_FIELD 
				+ " AS longitude, "
				+ MyDatabase.EPOCHTIME_FIELD 
				+ " AS epochTime FROM "
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE SUBSTR(" 
				+ MyDatabase.UTCTIME_FIELD 
				+ ", 1, 10) = ? ORDER BY epochTime";

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{date});

		ArrayList<GeoPointEpochTime> geoPointsForDay = new ArrayList<GeoPointEpochTime>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
			String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
			geoPointsForDay.add(new GeoPointEpochTime(new GeoPoint(latitudeE6, longitudeE6), epochTime));
			cursor.moveToNext();
		}
		cursor.close();
		return geoPointsForDay;
	}
	
	public static boolean clearOldHistory() {
		try {
			myWriteableDatabase.execSQL("DROP TABLE IF EXISTS " + MyDatabase.LOCATION_HISTORY_TABLE_NAME);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database table could not be dropped.");
			return false;
		}
		
		try {
			myWriteableDatabase.execSQL("VACUUM");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database table could not be vaccumed.");
			return false;
		}
		
		try {
			myWriteableDatabase.execSQL(LOCATION_HISTORY_TABLE_CREATE);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database table could not be created.");
			return false;
		}
		
		return true;
	}
	
	public static boolean isDatabaseEmptyOrDoesNotExist() {
		
		String SQL = "SELECT * FROM " + MyDatabase.LOCATION_HISTORY_TABLE_NAME + " LIMIT 1";
		Cursor cursor = null;
		try {
			cursor = myReadableDatabase.rawQuery(SQL, new String[]{});
		} catch (Exception e){
			return true;
		}
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			String epochTime = cursor.getString(cursor.getColumnIndex(MyDatabase.EPOCHTIME_FIELD));
			if(epochTime != null){
				cursor.close();
				return false;
			}
			cursor.moveToNext();
		}
		cursor.close();
		return true;
	}
	
	public static void closeDatabase(){
		myWriteableDatabase.close();
	}

	public static void dumpDatabaseToExternalMemory() {
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "se.wirelesser.wwwt" +"//databases//"+MyDatabase.DATABASE_NAME;
                String backupDBPath = MyDatabase.DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, UUID.randomUUID().toString());

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

            }
        } catch (Exception e) {
        	e.printStackTrace();

        }
		
	}
}
