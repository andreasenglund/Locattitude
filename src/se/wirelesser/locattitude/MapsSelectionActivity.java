package se.wirelesser.locattitude;

import java.util.ArrayList;

import se.wirelesser.locattitude.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

public class MapsSelectionActivity extends MapActivity {
	
	private MapView mapView;
	private MapController mapController;
	
	private OverlayManager overlayManager;
	private LocationManager locationManager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
	        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		overlayManager = new OverlayManager(getApplication(), mapView);
		createOverlayWithListener();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location lastKnownLocation = null;
		for (String provider : locationManager.getAllProviders()) {
			if (locationManager.getLastKnownLocation(provider) != null){
				lastKnownLocation = locationManager.getLastKnownLocation(provider);
				break;
			}
		}
		if (lastKnownLocation != null){
			int latitudeE6 = MyApplicationHelper.degreesToMicroDegrees(lastKnownLocation.getLatitude());
			int longitudeE6 = MyApplicationHelper.degreesToMicroDegrees(lastKnownLocation.getLongitude()); 
			mapController.setCenter(new GeoPoint(latitudeE6, longitudeE6));
		}
		
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
	
	public void createOverlayWithListener() {
		ManagedOverlay managedOverlay = overlayManager.createOverlay("listenerOverlay", getResources().getDrawable(R.drawable.marker));
		managedOverlay.setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {
			public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay) {
				return false;
			}

			public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				mapController.animateTo(point);
				mapController.zoomIn();
				return true;
			}

			public void onLongPress(MotionEvent e, ManagedOverlay overlay) {
				
			}


			public void onLongPressFinished(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {

				String longitude = MyApplicationHelper.microDegreesToDegrees(point.getLongitudeE6());
				String latitude = MyApplicationHelper.microDegreesToDegrees(point.getLatitudeE6());
				ArrayList<String> datesAtLocation = MyDatabaseHelper.getDatesAtLocation(longitude, latitude, 25000);
				String itemText = null;
				for (String date : datesAtLocation) {
					if (itemText == null){
						itemText = date;
					} else {
						itemText = itemText + "\n" + date;  
					}
				}
				if (itemText != null){
					Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
				}
					
			}

			public boolean onScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, ManagedOverlay overlay) {
				return false;
			}


			public boolean onSingleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				return false;
			}
		});
		overlayManager.populate();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
}
