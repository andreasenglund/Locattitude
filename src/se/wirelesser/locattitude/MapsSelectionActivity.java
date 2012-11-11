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

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

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

	@Override
    public void onCreate(Bundle savedInstanceState) {
	        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
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
				String lastDate = null;
				for (String date : datesAtLocation) {
					if (itemText == null){
						itemText = date;
					} else {
						itemText = itemText + "\n" + date;  
					}
					lastDate = date;
				}
				if (itemText != null){
					Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_LONG).show();
					drawCurrentPath(lastDate);
				} else {
					Toast.makeText(getApplicationContext(), "You've never been here. You should go!", Toast.LENGTH_LONG).show();
				}
					
			}

			private void drawCurrentPath(String lastDate) {
				
				List<Overlay> overlays = mapView.getOverlays();
				for (Overlay overlay : overlays) {
					if (overlay instanceof CurrentPathOverlay){
						CurrentPathOverlay currentPathOverlay = (CurrentPathOverlay)overlay;
						currentPathOverlay.setDate(lastDate);
					}
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
	
	class CurrentPathOverlay extends Overlay{

		String date = null;
		boolean reDraw = false;
	    public CurrentPathOverlay(){
	    	
	    }   

	    public void setDate(String lastDate) {
	    	date = lastDate;
	    	reDraw = true;
		}
	    
	    public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView){
	    	reDraw = true;
	    	return super.onKeyUp(keyCode, event, mapView);
	    }

		public void draw(Canvas canvas, MapView mapv, boolean shadow){
	        super.draw(canvas, mapv, shadow);
	        
	        if (date == null || !reDraw){
	        	return;
	        }
	        
	        reDraw = false;
	        Paint   mPaint = new Paint();
	        mPaint.setDither(true);
	        mPaint.setColor(Color.RED);
	        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	        mPaint.setStrokeJoin(Paint.Join.ROUND);
	        mPaint.setStrokeCap(Paint.Cap.ROUND);
	        mPaint.setStrokeWidth(2);
	        
	        ArrayList<GeoPoint> geoPoints = MyApplicationHelper.getGeoPointsForDay(date);
	        
	        GeoPoint internalGeoPoint1 = null, internalGeoPoint2 = null;
	        for (GeoPoint geoPoint : geoPoints) {
				
	            if(internalGeoPoint1 == null)
	            {
	            	internalGeoPoint1 = geoPoint;
	            	continue;
	            }
	            else
	            {
	            	internalGeoPoint2 = internalGeoPoint1;
	            	internalGeoPoint1 = geoPoint;
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
