package se.wirelesser.locattitude;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class LatitudeAuthenticator extends AsyncTask<String, Void, Boolean> {
    

	final String AUTH_TOKEN_TYPE = "https://www.googleapis.com/auth/latitude.all.best";
	public static String token = null;
	Activity activity;
	
	public LatitudeAuthenticator(Activity activity) {
		super();
		this.activity = activity;
	}
	
   protected Boolean doInBackground(String... params) {
	   if(authenticate()){
       return true;
	   } else{
		 return false;  
	   }
   }
   
   private boolean authenticate() {
	   
	   MainActivity.accountManager.getAccountManager().getAuthToken(MainActivity.account, "oauth2:https://www.googleapis.com/auth/latitude.all.best", null, activity, new AccountManagerCallback<Bundle>() {
		    public void run(AccountManagerFuture<Bundle> future) {
		      try {
		        token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
		      } catch (Exception e) {
		    	  e.printStackTrace();
		    	  System.out.println("Error in authentication");
		      }
		    }
		  }, null);
	   if (token != null){
		   return true;
	   }
	   return false;
	   
	}

/** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
   protected void onPostExecute(Boolean success) {
       if(success){
    	   System.out.println("Successful Authentication");
       } else {
    	   System.out.println("Failed Authentication");
       }
   }

}
