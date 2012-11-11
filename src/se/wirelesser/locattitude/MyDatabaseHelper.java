package se.wirelesser.locattitude;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import com.google.android.maps.GeoPoint;
import com.google.api.services.latitude.Latitude.Location.Delete;
import com.google.api.services.latitude.Latitude.Location.Get;
import com.google.api.services.latitude.Latitude.Location.List;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.model.LocationFeed;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

public class MyDatabaseHelper {

	private static MyDatabase myDatabaseWrapper = MainActivity.myDatabase;
	private static SQLiteDatabase myWriteableDatabase = myDatabaseWrapper.getWritableDatabase();
	private static SQLiteDatabase myReadableDatabase = myDatabaseWrapper.getReadableDatabase();
	private static InsertHelper iHelp = new InsertHelper(myWriteableDatabase, MyDatabase.LOCATION_HISTORY_TABLE_NAME);
    private static final String LOCATION_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + MyDatabase.LOCATION_HISTORY_TABLE_NAME + 
            " (epochTime INTEGER, " +
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
				iHelp.bind(1, MyApplicationHelper.objectToLong(location.getTimestampMs()));
				iHelp.bind(2, MyApplicationHelper.epochToUTC(MyApplicationHelper.objectToString(location.getTimestampMs())));
				iHelp.bind(3, MyApplicationHelper.objectToDouble(location.getLatitude()));
				iHelp.bind(4, MyApplicationHelper.objectToDouble(location.getLongitude()));

				if (MyApplicationHelper.objectToLong(location.getAccuracy()) != null){
					iHelp.bind(5, MyApplicationHelper.objectToLong(location.getAccuracy()));
				}

				if (MyApplicationHelper.objectToLong(location.getSpeed()) != null){
					iHelp.bind(6, MyApplicationHelper.objectToLong(location.getSpeed()));
				}
				
				if (MyApplicationHelper.objectToLong(location.getHeading()) != null){
					iHelp.bind(7, MyApplicationHelper.objectToLong(location.getHeading()));
				}

				if (MyApplicationHelper.objectToLong(location.getAltitude()) != null){
					iHelp.bind(8, MyApplicationHelper.objectToLong(location.getAltitude()));
				}

				if (MyApplicationHelper.objectToLong(location.getAltitudeAccuracy()) != null){
					iHelp.bind(9, MyApplicationHelper.objectToLong(location.getAltitudeAccuracy()));
				}
				
				if (MyApplicationHelper.objectToString(location.getActivityId()) != null){
					iHelp.bind(10, MyApplicationHelper.objectToString(location.getActivityId()));
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

	public static ArrayList<String> getDatesAtLocation(String longitude, String latitude, int withinRadiusMeters) {

		double latConversionRatio = 0.0000117;
		double longConversionRatio = 0.000009;

		String latLow = String.valueOf(Double.parseDouble(latitude) - (latConversionRatio*withinRadiusMeters));
		String latHigh = String.valueOf(Double.parseDouble(latitude) + (latConversionRatio*withinRadiusMeters));
		String longLow = String.valueOf(Double.parseDouble(longitude) - (longConversionRatio*withinRadiusMeters));
		String longHigh = String.valueOf(Double.parseDouble(longitude) + (longConversionRatio*withinRadiusMeters));

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
	
	public static ArrayList<GeoPoint> getGeoPointsForDate(String date) {

		String SQL = "SELECT DISTINCT " 
				+ MyDatabase.LATITUDE_FIELD 
				+ " AS latitude, "
				+ MyDatabase.LONGITUDE_FIELD 
				+ " AS longitude FROM "
				+ MyDatabase.LOCATION_HISTORY_TABLE_NAME 
				+ " WHERE SUBSTR(" 
				+ MyDatabase.UTCTIME_FIELD 
				+ ", 1, 10) = ?";

		Cursor cursor = myReadableDatabase.rawQuery(SQL, new String[]{date});

		ArrayList<GeoPoint> geoPointsForDay = new ArrayList<GeoPoint>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LATITUDE_FIELD)));
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(cursor.getDouble(cursor.getColumnIndex(MyDatabase.LONGITUDE_FIELD)));
			geoPointsForDay.add(new GeoPoint(latitudeE6, longitudeE6));
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
	
	public static void closeDatabase(){
		myWriteableDatabase.close();
	}

	public static void dumpDatabaseToExternalMemory() {
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ "se.wirelesser.locattitude" +"//databases//"+MyDatabase.DATABASE_NAME;
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
