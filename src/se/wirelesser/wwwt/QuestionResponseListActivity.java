package se.wirelesser.wwwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import se.wirelesser.wwwt.adapter.MenuArrayAdapter;
import android.R.menu;
import android.os.Bundle;
import android.os.Parcelable;
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
	QuestionResponseData questionResponseData = new QuestionResponseData(new ArrayList<String>());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        questionResponseData = answerQuestion(intent.getStringExtra("Longitude"), intent.getStringExtra("Latitude"), intent.getIntExtra(MyApplicationHelper.questionTypeString, -1));
        setListAdapter(new MenuArrayAdapter(this, questionResponseData.getMenuItemStrings()));
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, MapsSelectionActivity.class);
		intent.putExtra("NumberOfGeoPointsToDraw", questionResponseData.getMenuItems().get(position).getGeoPoints().size());
		MyApplicationHelper.pointsToDraw = questionResponseData.getMenuItems().get(position).getGeoPoints();
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
	
	protected QuestionResponseData answerQuestion(String longitude, String latitude,
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
				return new QuestionResponseData(new ArrayList<String>());
			}
			return new QuestionResponseData(dateArrayList);
		case MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_AREA:
			radius = 5000;
			dateArrayList = MyDatabaseHelper.getDatesAtLocation(longitude, latitude, radius);
		break;
		case MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_PLACE:
			radius = 100;
			ArrayList<GeoPointEpochTime> geoPoints = MyApplicationHelper.getSequencesAtLocationIncludingFirstPointNotAtLocation(longitude, latitude, radius);
			boolean sequenceStarted = false;
			String sequenceStartTime = null;
			QuestionResponseData questionResponse = new QuestionResponseData(new ArrayList<String>());
			MenuItem menuItem = new MenuItem(new ArrayList<GeoPoint>());
			for (GeoPointEpochTime geoPointEpochTime : geoPoints) {
				if (!sequenceStarted){
					questionResponse = new QuestionResponseData(new ArrayList<String>()); 
					sequenceStartTime = geoPointEpochTime.getEpochTime();
					menuItem = new MenuItem(new ArrayList<GeoPoint>());
					sequenceStarted = true;
				}
				menuItem.getGeoPoints().add(geoPointEpochTime.getGeoPoint());
				if (!MyApplicationHelper.isWithinThresholdOfPoint(startPoint, geoPointEpochTime.getGeoPoint(), radius)){
					sequenceStarted = false;
					if (Long.valueOf(geoPointEpochTime.getEpochTime()) - Long.valueOf(sequenceStartTime) > 600000){
						questionResponse.getMenuItemStrings().add(MyApplicationHelper.epochToUTC(sequenceStartTime) + " to "  +  MyApplicationHelper.epochToUTC(geoPointEpochTime.getEpochTime()));
						questionResponse.getMenuItems().add(menuItem);
						continue;
					}
				}
			}
			if (questionResponse.getMenuItems().size() == 0){
				Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
			}
			return questionResponse;
		}
		
		Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
		return new QuestionResponseData(new ArrayList<String>());
	}
    
}
