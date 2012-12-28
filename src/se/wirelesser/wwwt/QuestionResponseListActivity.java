package se.wirelesser.wwwt;

import java.util.ArrayList;

import se.wirelesser.wwwt.adapter.MenuArrayAdapter;
import android.os.Bundle;
import android.accounts.Account;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.app.ListActivity;
import android.widget.ListView;

import com.google.android.maps.GeoPoint;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class QuestionResponseListActivity extends ListActivity implements OnClickListener {
	
	public static GoogleAccountManager accountManager = null;
	public static MyDatabase myDatabase;
	public static Account account = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setListAdapter(new MenuArrayAdapter(this, answerQuestion(intent.getStringExtra("Longitude"), intent.getStringExtra("Latitude"), intent.getIntExtra(MyApplicationHelper.questionTypeString, -1))));
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, MapsSelectionActivity.class);
		intent.putExtra("DateToDraw", l.getItemAtPosition(position).toString());
    	startActivity(intent);
    	
    	// Make a toast message instead with info about what time and for how long and how many times that day
	}
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }

	public void onClick(View arg0) {
		
	}
	
	protected String[] answerQuestion(String longitude, String latitude,
			int questionType) {
		GeoPoint startPoint = new GeoPoint(MyApplicationHelper.degreesToMicroDegrees(latitude), MyApplicationHelper.degreesToMicroDegrees(longitude));
		int radius = 0;
		ArrayList<String> dateArrayList = new ArrayList<String>();
		switch (questionType) {
		case MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_CITY:
			radius = 25000;
			dateArrayList = MyDatabaseHelper.getDatesAtLocation(longitude, latitude, radius);
			if (dateArrayList.size() == 0){
				Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
				return new String[0];
			}
			String[] datesArray = new String[dateArrayList.size()];
			return dateArrayList.toArray(datesArray);
		case MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_AREA:
			radius = 5000;
			dateArrayList = MyDatabaseHelper.getDatesAtLocation(longitude, latitude, radius);
		break;
		case MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_PLACE:
			radius = 100;
			ArrayList<GeoPointEpochTime> geoPoints = MyApplicationHelper.getSequencesAtLocationIncludingFirstPointNotAtLocation(longitude, latitude, radius);
			ArrayList<String> dateTimeArrayList = new ArrayList<String>();
			boolean sequenceStarted = false;
			String sequenceStartTime = null;
			for (GeoPointEpochTime geoPointEpochTime : geoPoints) {
				if (!sequenceStarted){
					sequenceStartTime = geoPointEpochTime.getEpochTime();
					sequenceStarted = true;
				}
				if (!MyApplicationHelper.isWithinThresholdOfPoint(startPoint, geoPointEpochTime.getGeoPoint(), radius)){
					sequenceStarted = false;
					if (Long.valueOf(geoPointEpochTime.getEpochTime()) - Long.valueOf(sequenceStartTime) > 600000){
						dateTimeArrayList.add(MyApplicationHelper.epochToUTC(sequenceStartTime) + " to "  +  MyApplicationHelper.epochToUTC(geoPointEpochTime.getEpochTime()));
						continue;
					}
				}
			}
			if (dateTimeArrayList.size() == 0){
				Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
				return new String[0];
			}
			String[] dateTimeArray = new String[dateTimeArrayList.size()];
			return dateTimeArrayList.toArray(dateTimeArray);
		}
		
		Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
		return new String[0];
	}
    
}
