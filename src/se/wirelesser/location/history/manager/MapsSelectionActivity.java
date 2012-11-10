package se.wirelesser.location.history.manager;

import java.util.List;

import com.google.android.maps.Overlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import android.os.Bundle;
import android.app.Activity;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class MapsSelectionActivity extends MapActivity implements OnClickListener, OnDoubleTapListener {
	
	MapView mapView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
	        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mapView.getOverlays();
        //LocationSelectOverlay overlay = new LocationSelectOverlay(messageMarker, Map.this);
        //mapOverlays.add(overlay);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
	    int x = (int)e.getX(), y = (int)e.getY();;  
	    Projection p = mapView.getProjection();  
	    mapView.getController().animateTo(p.fromPixels(x, y));
	    mapView.getController().zoomIn();  
	    return true; 
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onDoubleTap(MotionEvent e) {
		int x = (int)e.getX(), y = (int)e.getY();;  
	    Projection p = mapView.getProjection();  
	    mapView.getController().animateTo(p.fromPixels(x, y));
	    mapView.getController().zoomIn();  
	    return true;
	}
    
}
