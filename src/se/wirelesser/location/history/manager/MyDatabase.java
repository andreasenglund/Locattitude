package se.wirelesser.location.history.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabase extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "db";
    public static final int DATABASE_VERSION = 1;
    public static final String LOCATION_HISTORY_TABLE_NAME = "locationHistory";
    public static final String EPOCHTIME_FIELD = "epochTime";
    public static final String UTCTIME_FIELD = "utcTime";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";
    public static final String ACCURACY_FIELD = "accuracy";
    public static final String SPEED_FIELD = "speed";
    public static final String HEADING_FIELD = "heading";
    public static final String ALTITUDE_FIELD = "altitude";
    public static final String ALTITUDE_ACCURACY_FIELD = "altitudeAccuracy";
    public static final String ACTIVITY_ID_FIELD = "activityId";
    public static final String[] COLUMN_ARRAY = {EPOCHTIME_FIELD, UTCTIME_FIELD, LATITUDE_FIELD, LONGITUDE_FIELD, ACCURACY_FIELD, SPEED_FIELD, HEADING_FIELD, ALTITUDE_FIELD, ALTITUDE_ACCURACY_FIELD, ACTIVITY_ID_FIELD};

    MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	

}
