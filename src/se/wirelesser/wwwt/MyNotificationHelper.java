package se.wirelesser.wwwt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

public class MyNotificationHelper {
    private Context mContext;
    private int NOTIFICATION_ID = 1;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private PendingIntent mContentIntent;
    private CharSequence mContentTitle;
    private Builder mBuilder = null;
    public MyNotificationHelper(Context context)
    {
        mContext = context;
    	mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Put the notification into the status bar
     */
    public void createNotification() {
    	
    	mBuilder =
    	        new NotificationCompat.Builder(mContext)
    	        .setSmallIcon(android.R.drawable.stat_sys_download)
    	        .setContentTitle("Synchronizing...")
    	        .setContentText("Starting synchronization...")
    	        .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(), 0))
    	        .setOngoing(true);
    	
    	mNotification = mBuilder.getNotification();
    	
    	mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     * @param percentageComplete
     */
    public void progressUpdate(String date) {
    
    	mBuilder.setContentText("Currently syncing: " + date);
	    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.getNotification());
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ‘task complete’ notification
     */
    public void completed()    {
    	mBuilder.setContentText("Sync completed!")
    	.setOngoing(false);
    	
	    mNotificationManager.notify(
            NOTIFICATION_ID,
            mBuilder.getNotification());
    }
}
