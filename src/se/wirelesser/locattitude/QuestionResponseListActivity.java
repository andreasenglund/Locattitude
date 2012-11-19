package se.wirelesser.locattitude;

import se.wirelesser.locattitude.adapter.MenuArrayAdapter;
import android.os.Bundle;
import android.accounts.Account;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.ListActivity;
import android.widget.ListView;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class QuestionResponseListActivity extends ListActivity implements OnClickListener {
	
	public static GoogleAccountManager accountManager = null;
	public static MyDatabase myDatabase;
	public static Account account = null;
	private static String[] MENU_ITEMS = null;
	final int DIALOG_ACCOUNTS = 1;
	Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MENU_ITEMS = getIntent().getStringArrayExtra("MenuIems");

        setListAdapter(new MenuArrayAdapter(this, MENU_ITEMS));
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, MapsSelectionActivity.class);
		intent.putExtra("DateToDraw", l.getItemAtPosition(position).toString());
    	startActivity(intent);
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
    
}
