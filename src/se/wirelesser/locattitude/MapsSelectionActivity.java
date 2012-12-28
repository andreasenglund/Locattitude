package se.wirelesser.locattitude;

import java.util.ArrayList;
import java.util.List;

import se.wirelesser.locattitude.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.common.base.CaseFormat;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

public class MapsSelectionActivity extends MapActivity {
	
	private MapView mapView;
	private MapController mapController;
	
	private OverlayManager overlayManager;
	private LocationManager locationManager;
	private Projection projection;
	private MapsSelectionActivity thisActivity = null;
	public static String[] dateArray = null;
	ArrayList<GeoPointEpochTime> geoPoints = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisActivity = this;
        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(true);
		mapController = mapView.getController();
		overlayManager = new OverlayManager(getApplication(), mapView);
		createOverlayWithListener();
		mapView.getOverlays().add(new CurrentPathOverlay());
		mapController.setCenter(getLastKnownLocation());
		projection = mapView.getProjection();
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
				mapController.zoomIn();
				mapController.animateTo(point);
				return true;
			}

			public void onLongPress(MotionEvent e, ManagedOverlay overlay) {
				
			}


			public void onLongPressFinished(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				overlay.createItem(point);
				String longitude = MyApplicationHelper.microDegreesToDegrees(point.getLongitudeE6());
				String latitude = MyApplicationHelper.microDegreesToDegrees(point.getLatitudeE6());
				
				Intent intent = new Intent(thisActivity, SelectQuestionTypeActivity.class);
				intent.putExtra("Latitude", latitude);
				intent.putExtra("Longitude", longitude);
			    startActivity(intent);
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
		
	

	protected void handleFrequencyQuestion(String longitude, String latitude,
			int questionType) {
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void drawPathForDate(String date) {
		List<Overlay> overlays = mapView.getOverlays();
		for (Overlay overlay : overlays) {
			if (overlay instanceof CurrentPathOverlay){
				CurrentPathOverlay currentPathOverlay = (CurrentPathOverlay)overlay;
				currentPathOverlay.setDate(date);
			}
		}
	}
	
	private GeoPoint getLastKnownLocation() {
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
			return new GeoPoint(latitudeE6, longitudeE6);
		}
		return null;
	}
	
	public class CurrentPathOverlay extends Overlay{

		String date = null;
		boolean dateChanged = false;

	    public void setDate(String newDate) {
	    	if (!newDate.equals(date)){
		    	date = newDate;
		    	dateChanged = true;
	    	}
		}
	    
	    public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView){
	    	return super.onKeyUp(keyCode, event, mapView);
	    }

		public void draw(Canvas canvas, MapView mapv, boolean shadow){
	        if (date == null){
	        	return;
	        }
	        
	        if (dateChanged){
		        geoPoints = MyApplicationHelper.getGeoPointsForDate(date);
	        }
	        
	        Paint   mPaint = new Paint();
	        mPaint.setDither(true);
	        mPaint.setColor(Color.RED);
	        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	        mPaint.setStrokeJoin(Paint.Join.ROUND);
	        mPaint.setStrokeCap(Paint.Cap.ROUND);
	        mPaint.setStrokeWidth(2);
	        
	        GeoPoint internalGeoPoint1 = null, internalGeoPoint2 = null;
	        for (GeoPointEpochTime geoPointEpochTime : geoPoints) {
				
	            if(internalGeoPoint1 == null)
	            {
	            	internalGeoPoint1 = geoPointEpochTime.getGeoPoint();
	            	continue;
	            }
	            else
	            {
	            	internalGeoPoint2 = internalGeoPoint1;
	            	internalGeoPoint1 = geoPointEpochTime.getGeoPoint();
	            }
	            
		        Point p1 = new Point();
		        Point p2 = new Point();
		        Path path = new Path();
	
		        projection.toPixels(internalGeoPoint1, p1);
		        projection.toPixels(internalGeoPoint2, p2);
	
		        path.moveTo(p2.x, p2.y);
		        path.lineTo(p1.x,p1.y);
	
		        canvas.drawPath(path, mPaint);
	        }
	        super.draw(canvas, mapv, shadow);
	    }
	    
	}
}
