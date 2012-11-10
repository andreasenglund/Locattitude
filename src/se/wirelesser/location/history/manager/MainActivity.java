package se.wirelesser.location.history.manager;

import android.os.Bundle;
import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class MainActivity extends Activity implements OnClickListener {
	
	public static GoogleAccountManager accountManager = null;
	public static MyDatabase myDatabase;
	public static Account account = null;
	final int DIALOG_ACCOUNTS = 1;
	Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDatabase = new MyDatabase(getApplicationContext());
        setContentView(R.layout.activity_main);
        accountManager = new GoogleAccountManager(this);
        Account accounts[] = accountManager.getAccounts();
        if(accounts.length > 1){
        	showDialog(DIALOG_ACCOUNTS);
        } else {
            account = accounts[0];
        }
        LatitudeAuthenticator authenticate = new LatitudeAuthenticator(this);
        authenticate.execute();
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        this.findViewById(R.id.button1).setOnClickListener(this);
        this.findViewById(R.id.button2).setOnClickListener(this);
        this.findViewById(R.id.button3).setOnClickListener(this);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
      switch (id) {
        case DIALOG_ACCOUNTS:
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Select a Google account");
          accountManager = new GoogleAccountManager(this);
          final Account[] accounts = accountManager.getAccounts();
          final int size = accounts.length;
          String[] names = new String[size];
          for (int i = 0; i < size; i++) {
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

	public void onClick(View v) {
		switch (v.getId()) {
	    case R.id.button1: 
	    	SynchronizeLatitudeHistory synchronizeHistory = new SynchronizeLatitudeHistory(this);
			synchronizeHistory.execute();
	     break;
	    case R.id.button2:
	    	Intent intent = new Intent(this, MapsSelectionActivity.class);
	    	startActivity(intent);
	    	//VegasCheck vegasCheck = new VegasCheck(this);
			//vegasCheck.execute();	
	     break;
	    case R.id.button3:
	    	MyDatabaseHelper.dumpDatabaseToExternalMemory();
	     break;
		}
	}
    
}
