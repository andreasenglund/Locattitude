package se.wirelesser.locattitude;

import se.wirelesser.locattitude.adapter.MenuArrayAdapter;
import android.os.Bundle;
import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ListActivity;
import android.widget.ListView;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class MainActivity extends ListActivity {
	
	private static final String[] MENU_ITEMS = new String[] { "Choose your point of interest", "Syncronize", "Dump database"};
	final int DIALOG_ACCOUNTS = 1;
	Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setListAdapter(new MenuArrayAdapter(this, MENU_ITEMS));
    }
    
	private void init() {
        MyApplicationHelper.accountManager = new GoogleAccountManager(this);
        Account accounts[] = MyApplicationHelper.accountManager.getAccounts();
        if(accounts.length == 1){
        	MyApplicationHelper.account = accounts[0];
        } else {
            showDialog(DIALOG_ACCOUNTS);
        }
        MyApplicationHelper.myDatabase = new MyDatabase(getApplicationContext());
        if (MyDatabaseHelper.isDatabaseEmptyOrDoesNotExist()){
        	Toast.makeText(getApplicationContext(), "Your database is currently empty. Please run a sync before using the application.", Toast.LENGTH_LONG).show();
        }
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, MapsSelectionActivity.class);
		switch (position) {
	    case 0:
	    	startActivity(intent);
	     break;
	    case 1:
	    	startSync();
	     break;
	    case 2:
	    	MyDatabaseHelper.dumpDatabaseToExternalMemory();
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
          final Account[] accounts = MyApplicationHelper.accountManager.getAccounts();
          String[] names = new String[accounts.length];
          for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
          }
          builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	MyApplicationHelper.account = accounts[which];
            }
          });
          return builder.create();
      }
      return null;
    }
}
