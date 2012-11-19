package se.wirelesser.locattitude;

import se.wirelesser.locattitude.R;
import se.wirelesser.locattitude.adapter.MenuArrayAdapter;
import android.os.Bundle;
import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.ListActivity;
import android.widget.ListView;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class MainActivity extends ListActivity implements OnClickListener {
	
	public static GoogleAccountManager accountManager = null;
	public static MyDatabase myDatabase;
	public static Account account = null;
	private static final String[] MENU_ITEMS = new String[] { "When was I here?", "How long do I stay here?", "How often do I come here?", "Syncronize", "Dev Tools"};
	final int DIALOG_ACCOUNTS = 1;
	Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        setListAdapter(new MenuArrayAdapter(this, MENU_ITEMS));
    }
    
	private void init() {
        accountManager = new GoogleAccountManager(this);
        Account accounts[] = accountManager.getAccounts();
        if(accounts.length == 1){
        	account = accounts[0];
        } else {
            showDialog(DIALOG_ACCOUNTS);
        }
        myDatabase = new MyDatabase(getApplicationContext());
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch (position) {
	    case 0:
	    	Intent intent0 = new Intent(this, MapsSelectionActivity.class);
	    	intent0.putExtra("TypeOfQuestion", 1);
	    	startActivity(intent0);
	     break;
	    case 1:
	    	Intent intent1 = new Intent(this, MapsSelectionActivity.class);
	    	intent1.putExtra("TypeOfQuestion", 2);
	    	startActivity(intent1);
	     break;
	    case 2:
	    	Intent intent2 = new Intent(this, MapsSelectionActivity.class);
	    	intent2.putExtra("TypeOfQuestion", 3);
	    	startActivity(intent2);
	     break;
	    case 3:
	    	startSync();
	     break;
	    case 4:
	    	
	     break;
		}
 
	}
    
    private void startSync() {
    	SynchronizeLatitudeHistory synchronizeHistory = new SynchronizeLatitudeHistory(this, getApplicationContext());
		synchronizeHistory.execute();
		
	}

	@Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
      switch (id) {
        case DIALOG_ACCOUNTS:
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Select a Google account");
          final Account[] accounts = accountManager.getAccounts();
          String[] names = new String[accounts.length];
          for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
          }
          builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	account = accounts[which];
            }
          });
          return builder.create();
      }
      return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
