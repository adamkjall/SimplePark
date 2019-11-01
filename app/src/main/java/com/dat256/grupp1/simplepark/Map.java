package com.dat256.grupp1.simplepark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.VectorDrawable;
import android.location.LocationManager;
import android.util.AttributeSet;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;


/**
 * Custom MapView class that uses osmdroid
 */
public class Map extends MapView implements MapEventsReceiver {
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private Context context;
    HashMap<ParkingSpot, Marker> markerMap;


    /**
     * The constructor that is called when the Map in activity_main.xml is created
     *
     * @param context, the application context
     * @param attrs,   the attribute set
     */
    public Map(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        markerMap = new HashMap<>();
        setTileSource(TileSourceFactory.MAPNIK);
        mapController = getController();
        init();
        setBuiltInZoomControls(true);
        setMultiTouchControls(true);
    }

    /**
     * Initializes the map's starting position, draws a person on the map and follows that person
     * when the gps location is changed
     */
    private void init() {
        // configures map starting position
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(57.699775, 11.979478); // gbg coordinates
        mapController.setCenter(startPoint);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_cluster);

        // configures location overlay (the "person" icon that shows on the map)
        GpsMyLocationProvider gpsProvider = new GpsMyLocationProvider(context);
        gpsProvider.addLocationSource(LocationManager.GPS_PROVIDER);
        final MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(gpsProvider, this);
        locationOverlay.enableMyLocation(); // draw person on map
        locationOverlay.setPersonIcon(icon);
        locationOverlay.setPersonHotspot(icon.getWidth() / 2, icon.getHeight() / 2);
        locationOverlay.enableFollowLocation(); // move map when location changes
        this.getOverlayManager().add(locationOverlay);
        this.locationOverlay = locationOverlay;

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(0, mapEventsOverlay);
    }

    /**
     * Prints out markers for parking spots from a list with parking spots, with colour depending
     * on availability.
     */
    public void drawMarkers() {
        for (Entry<ParkingSpot, Marker> entry : markerMap.entrySet()) {
            Marker m = entry.getValue();
            m.closeInfoWindow();
            ParkingSpot p = entry.getKey();
            int x = p.getAvailableSpaces();
            VectorDrawable markerImg;
            if (x == -1)
                markerImg = (VectorDrawable) getResources().getDrawable(R.drawable.ic_parkingspot_default);
            else if (x <= 3)
                markerImg = (VectorDrawable) getResources().getDrawable(R.drawable.ic_parkingspot_red);
            else if (x <= 10)
                markerImg = (VectorDrawable) getResources().getDrawable(R.drawable.ic_parkingspot_orange);
            else
                markerImg = (VectorDrawable) getResources().getDrawable(R.drawable.ic_parkingspot_green);
            m.setPosition(p.getGeoPoint());
            m.setIcon(markerImg);
            m.setInfoWindowAnchor(Marker.ANCHOR_CENTER + (float) 1, Marker.ANCHOR_CENTER - (float) 0.3);
            m.setTitle(p.getName() + "\n" + p.getCostInfo());
            if (p.getAvailableSpaces() != -1)
                m.setSubDescription("Available parkingspots: " + p.getAvailableSpaces() + " Of " + p.getTotalSpaces());
            else if (p.getTotalSpaces() != -1)
                m.setSubDescription("Total parkingspots: " + p.getTotalSpaces());
            else
                m.setSubDescription("Total parkingspots not available");
            m.setPanToView(true); // If true, the map will be centered around marker if clicked on
            m.setDraggable(false);
            this.getOverlays().remove(m);
            if (p.getVisible()) {
                this.getOverlays().add(m);
            }
        }
        this.invalidate();

    }


    /**
     * Takes a list with ParkingSpots and connects them with a Marker each in a HashMap.
     *
     * @param pList, the list containing ParkingSpots
     */
    public void fillMarkerMap(List<ParkingSpot> pList) {
        for (ParkingSpot ps : pList) {

            if (markerMap.get(ps) == null) {
                final ParkingSpot parkingSpot = ps;
                Marker marker = new Marker(this);
                markerMap.put(ps, marker);

                // Override the markers clicklistener so we can show
                // a route to it if clicked
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        marker.showInfoWindow();
                        //mapView.getController().animateTo(marker.getPosition());
                        ((MainActivity) context).drawRoute(parkingSpot);
                        ((MainActivity) context).showResetButton();
                        return true;
                    }
                });
            }
        }
    }


    /**
     * @return The mapController
     */
    public IMapController getMapController() {
        return mapController;
    }

    /**
     * @return The location overlay
     */
    public MyLocationNewOverlay getLocationOverlay() {
        return locationOverlay;
    }

    /**
     * Sets all parking spots to visible and calls the draw method to reset all filters.
     */
    public void resetMarkers() {
        for (Entry<ParkingSpot, Marker> entry : markerMap.entrySet()) {
            ParkingSpot parkingSpot = entry.getKey();
            parkingSpot.setVisible(true);
        }
        drawMarkers();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(this);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        //Not used
        return false;
    }
}