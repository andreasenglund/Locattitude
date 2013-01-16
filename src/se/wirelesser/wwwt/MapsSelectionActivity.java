package se.wirelesser.wwwt;

import java.util.ArrayList;
import java.util.List;

import se.wirelesser.wwwt.R;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import android.widget.TextView;

public class MapsSelectionActivity extends android.support.v4.app.FragmentActivity
	implements OnMapClickListener, OnMapLongClickListener {
	
	private MapView mapView;
	private MapController mapController;
	
	private OverlayManager overlayManager;
	private LocationManager locationManager;
	private Projection projection;
	private MapsSelectionActivity thisActivity = null;
	public static String[] dateArray = null;
	ArrayList<GeoPointEpochTime> geoPoints = null;
	
    private GoogleMap mMap;
    private TextView mTapTextView;
    private TextView mCameraTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmaps);
        setUpMapIfNeeded();
        if (getIntent().getExtras() != null && getIntent().getExtras().getInt("NumberOfGeoPointsToDraw", 0) > 0){
        	drawGeoPoints(MyApplicationHelper.pointsToDraw);
        }
    }
    
    private void drawGeoPoints(List<GeoPoint> geoPoints) {
    	PolylineOptions options = new PolylineOptions();
    	for (GeoPoint geoPoint : geoPoints) {
    		options.add(new LatLng(MyApplicationHelper.microDegreesToDegreesDouble(geoPoint.getLatitudeE6()),MyApplicationHelper.microDegreesToDegreesDouble(geoPoint.getLongitudeE6())));
    	}
    	
    	mMap.addPolyline(options);
		
	}

	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
    	CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(MyApplicationHelper.toLatLng(getLastKnownLocation()), 12);
    	mMap.moveCamera(cameraUpdate);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        setUpMapIfNeeded();
    }
		
	

	protected void handleFrequencyQuestion(String longitude, String latitude,
			int questionType) {
		
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

	public void onMapLongClick(LatLng point) {
		mMap.addMarker(new MarkerOptions()
        .position(point)
        .title("POI")
        .snippet("Your chosen point of interest."));
		String longitude = Double.toString(point.longitude);
		String latitude = Double.toString(point.latitude);
		Intent intent = new Intent(this, SelectQuestionTypeActivity.class);
		intent.putExtra("Latitude", latitude);
		intent.putExtra("Longitude", longitude);
	    startActivity(intent);
	}

	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		
	}
}
