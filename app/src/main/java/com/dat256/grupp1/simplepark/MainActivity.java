package com.dat256.grupp1.simplepark;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


// example coordinates (Gothenburg):
// Latitude: 57.699775
// Longitude: 11.979478

/**
 * The default activity, initializes map + UI
 */
public class MainActivity extends Activity implements ParkingFetcher.AsyncResponse {
    private List<ParkingSpot> parkingSpots;
    private Map map = null;
    private GpsTracker gpsTracker;
    private Polyline roadOverlay;
    private double radius = 300.0;
    private ParkingSpot currentParkingSpot;
    private FloatingActionButton resetButton;
    private FloatingActionsMenu menu;

    /**
     * Runs when activity is created
     *
     * @param savedInstanceState Saved application instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parkingSpots = new ArrayList<>();
        setContentView(R.layout.activity_main);
        checkLocationPermissions();
        map = (Map) findViewById(R.id.map);
        init();
        //Disable StrictMode as we are not allowed to make network calls in the main thread otherwise
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Starts a new thread that tries to fetch parking data
        // If successful the list parkingSpots will be updated with the data
        new ParkingFetcher(this).execute("");

        // get gps coordinates
        gpsTracker = new GpsTracker(this);
    }

    /**
     * Runs when app is resumed
     */
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * Runs when activity is paused
     */
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * Executes when Async thread has finished retrieving data
     *
     * @param output Is a list with our parking data
     */
    @Override
    public void processFinish(List<ParkingSpot> output) {

        parkingSpots = output;
        // For testing
        String size = String.valueOf(output.size());
        //Toast toast = Toast.makeText(getApplicationContext(), size, Toast.LENGTH_LONG);
        //toast.show();

        // For testing the updated parking spot fetcher
        // for (ParkingSpot p : parkingSpots)
        //     System.out.println(p);
        map.fillMarkerMap(parkingSpots);
        map.drawMarkers();
    }

    /**
     * Initializes osmdroid configuration and adds listeners to buttons
     */
    private void init() {
        // load/initialize the osmdroid configuration
        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        final LinearLayout optimalContainer = findViewById(R.id.optimalContainer);
        menu = (FloatingActionsMenu) findViewById(R.id.menuButton);
        resetButton = findViewById(R.id.resetButton);
        final FloatingActionButton findOptimalParkingSpot = (FloatingActionButton) findViewById(R.id.findOptimalParkingSpot);
        final FloatingActionButton followButton = (FloatingActionButton) findViewById(R.id.followButton);
        final FloatingActionButton findNearButton = (FloatingActionButton) findViewById(R.id.findNearButton);
        final Button decreaseRadius = findViewById(R.id.decreaseRadius);
        final Button increaseRadius = findViewById(R.id.increaseRadius);
        final Button pickParkingspot = findViewById(R.id.pickParkingspot);


        // recenter map around user when followButton is clicked
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getLocationOverlay().enableFollowLocation();
                map.getMapController().setZoom(15.5);
            }
        });

        // shows closest parking to the user when findNearButton is clicked
        findNearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint currentGeopoint = new GeoPoint(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                currentParkingSpot = Filter.showNearest(parkingSpots, currentGeopoint);
                map.drawMarkers();
                if (currentParkingSpot != null) {
                    drawRoute(currentGeopoint, currentParkingSpot.getGeoPoint());
                }
                optimalContainer.setVisibility(View.INVISIBLE);
                resetButton.setVisibility(View.VISIBLE); // Activates the reset button
                menu.collapse(); // Close menu when nearest button is clicked
            }
        });

        // Button to call resetMarker, to reset the markers visibility
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.resetMarkers();
                map.getOverlays().remove(roadOverlay); // Remove the roadOverlay(The drawn route)
                currentParkingSpot = null;
                resetButton.setVisibility(View.INVISIBLE); // Deactivates the reset button
                optimalContainer.setVisibility(View.INVISIBLE);
            }
        });

        // Button to decide on a parkingspot after pressing optimal parkingspot button.
        pickParkingspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optimalContainer.setVisibility(View.INVISIBLE);
            }
        });

        //Decrease radius and find cheapest parkingspot in radius.
        decreaseRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getOverlays().remove(roadOverlay); // Remove the roadOverlay(The drawn route)
                GeoPoint currentGeopoint = new GeoPoint(map.getLocationOverlay().getMyLocation());
                if (radius >= 200) {
                    radius = radius - 100;
                }
                pickParkingspot.setText((int) (radius) + "m");
                currentParkingSpot = Filter.showCustomOptimal(parkingSpots, currentGeopoint, radius);
                map.drawMarkers();
                if (currentParkingSpot != null) {
                    drawRoute(currentGeopoint, currentParkingSpot.getGeoPoint());
                }

            }
        });
        //Increase radius and find cheapest parkingspot in radius.
        increaseRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getOverlays().remove(roadOverlay); // Remove the roadOverlay(The drawn route)
                GeoPoint currentGeopoint = new GeoPoint(map.getLocationOverlay().getMyLocation());
                if (radius < 2500) {
                    radius = radius + 100;
                }
                //pickParkingspot.setFontFeatureSettings();
                pickParkingspot.setText((int) (radius) + "m");
                currentParkingSpot = Filter.showCustomOptimal(parkingSpots, currentGeopoint, radius);
                map.drawMarkers();
                if (currentParkingSpot != null) {
                    drawRoute(currentGeopoint, currentParkingSpot.getGeoPoint());
                }
            }
        });

        // Button to find cheapest and nearest parking spot
        findOptimalParkingSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius = 500;
                pickParkingspot.setText((int) (radius) + "m");
                map.getOverlays().remove(roadOverlay); // Remove the roadOverlay(The drawn route)
                GeoPoint currentGeopoint = new GeoPoint(map.getLocationOverlay().getMyLocation());
                LinearLayout optimalContainer = findViewById(R.id.optimalContainer);
                currentParkingSpot = Filter.showCustomOptimal(parkingSpots, currentGeopoint, radius);
                map.drawMarkers();
                if (currentParkingSpot != null) {
                    drawRoute(currentGeopoint, currentParkingSpot.getGeoPoint());
                }
                menu.collapse(); // Close menu when optimal button is clicked
                resetButton.setVisibility(View.VISIBLE); // Activates the reset button
                optimalContainer.setVisibility(View.VISIBLE); // Activates the optimal container
            }
        });

        menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {

            @Override
            public void onMenuExpanded() {
                findNearButton.setVisibility(View.VISIBLE);
                findOptimalParkingSpot.setVisibility(View.VISIBLE);

            }

            @Override
            public void onMenuCollapsed() {
                findNearButton.setVisibility(View.GONE);
                findOptimalParkingSpot.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (menu.isExpanded()) {
                Rect outRect = new Rect();
                menu.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    menu.collapse();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Prompts the user for location permissions
     */
    private void checkLocationPermissions() {
        // check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
    }

    /**
     * Updates the displayed route to adapt to users change of position.
     * @param location Users current location
     */

    public void updateRoute(Location location) {
        if (currentParkingSpot != null)
            drawRoute(new GeoPoint(location), currentParkingSpot.getGeoPoint());
    }

    /**
     * Draws a route between the start and end
     *
     * @param start starting geopoint
     * @param end   end geopoint
     */
    public void drawRoute(GeoPoint start, GeoPoint end) {
//        Alternative RoadManager:
//        RoadManager roadManager = new OSRMRoadManager(this);
        if (start == null || end == null) {
            Toast.makeText(this.getApplicationContext(), "Can't find route.", Toast.LENGTH_SHORT).show();
            return;
        }
        map.getOverlays().remove(roadOverlay); // Remove the roadOverlay(The drawn route)

        RoadManager roadManager = new MapQuestRoadManager("Bv7ktUoK9HXCYRE74H8JmkJL1OC4bA7p"); // API-key belonging to Martin K, https://developer.mapquest.com/user/me/apps
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(start);
        waypoints.add(end);

        Road road;
        try {
            road = roadManager.getRoad(waypoints);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this.getApplicationContext(), "Can't find route.", Toast.LENGTH_SHORT).show();
            return;
        }

        roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }


    /**
     * Draws a route from current position to the parkingspot
     *
     * @param parkingSpot
     */
    public void drawRoute(ParkingSpot parkingSpot) {
        currentParkingSpot = parkingSpot;
        Location location = gpsTracker.getLocation();
        if (location != null && parkingSpot != null) {
            drawRoute(new GeoPoint(location), parkingSpot.getGeoPoint());
        }
    }

    /**
     * Show the "reset filters" button
     */
    public void showResetButton() {
        resetButton.setVisibility(View.VISIBLE);
    }

    public List<ParkingSpot> getParkingSpots() {
        return parkingSpots;
    }
}


