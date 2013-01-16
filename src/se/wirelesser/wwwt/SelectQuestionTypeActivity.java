package se.wirelesser.wwwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.maps.GeoPoint;

import se.wirelesser.wwwt.adapter.MenuArrayAdapter;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.app.ListActivity;
import android.widget.ListView;

public class SelectQuestionTypeActivity extends ListActivity {
	
	private static final List<String> MENU_ITEMS = Arrays.asList("When was I in this city?",  "When was I in this place? (Restaurant, Cafe, etc)");
	private String latitude = null;
	private String longitude = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        latitude = intent.getStringExtra("Latitude");
        longitude = intent.getStringExtra("Longitude");
        setListAdapter(new MenuArrayAdapter(this, MENU_ITEMS));
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, QuestionResponseListActivity.class);
		intent.putExtra("Latitude", latitude);
		intent.putExtra("Longitude", longitude);
		switch (position) {
	    case 0:
	    	intent.putExtra(MyApplicationHelper.questionTypeString, MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_CITY);
	    	startActivity(intent);
	     break;
	    case 1:
	    	intent.putExtra(MyApplicationHelper.questionTypeString, MyApplicationHelper.QUESTION_WHAT_DATES_WERE_I_IN_PLACE);
	    	startActivity(intent);
	     break;
		}
	}
    
	@Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
 
}
