package com.dat256.grupp1.simplepark;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an AsyncTask that fetches parking data from Göteborgs API
 * and converts it to a List<ParkingSpot> and delegates the list via
 * the interface AsyncResponse
 */
public class ParkingFetcher extends AsyncTask<String, Void, String> {

    /**
     * Interface that enables ParkingFetcher to communicate with other
     * classes that implement AsyncResponse
     */
    public interface AsyncResponse {
        void processFinish(List<ParkingSpot> output);
    }

    private AsyncResponse delegate;

    /**
     * Class constructor
     *
     * @param delegate Used to delegate response to the caller
     */
    public ParkingFetcher(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    /**
     * Starts a new thread that runs in the background
     *
     * @param strings URL parameters
     * @return Returns a JSON string with parking data
     */
    @Override
    protected String doInBackground(String... strings) {
        return NetworkUtils.getParkInfo(strings[0]);
    }

    // After the doInBackground has fetched the data this method runs

    /**
     * Runs when the AsyncTask thread is finished,
     * converts the JSON string to a List<ParkingSpot>
     *
     * @param s JSON string from our AsyncTask thread
     */
    @Override
    protected void onPostExecute(String s) {
        List<ParkingSpot> parkingList = new ArrayList<>();

        try {
            // Convert our string of data to a JSONArray
            JSONArray jsonArray = new JSONArray(s);

            // For every item in the array, save the data we're interested
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String name;
                if (jsonObject.has("Name")) {
                    name = jsonObject.getString("Name");
                } else {
                    name = "Unavailable";
                }

                int availableSpaces;
                if (jsonObject.has("FreeSpaces")) {
                    availableSpaces = jsonObject.getInt("FreeSpaces");
                } else {
                    // Information unknown
                    availableSpaces = -1;
                }

                Integer totalSpaces;
                if (jsonObject.has("ParkingSpaces")) {
                    totalSpaces = jsonObject.getInt("ParkingSpaces");
                } else {
                    // Information unknown
                    totalSpaces = -1;
                }

                String costInfo;
                if (jsonObject.has("ParkingCost")) {
                    costInfo = jsonObject.getString("ParkingCost");
                } else {
                    costInfo = "Unavailable";
                }

                double currentCost;
                if (jsonObject.has("CurrentParkingCost")) {
                    currentCost = jsonObject.getDouble("CurrentParkingCost");
                } else {
                    currentCost = -1;
                }

                String maxTime;
                if (jsonObject.has("MaxParkingTime")) {
                    maxTime = jsonObject.getString("MaxParkingTime");
                } else {
                    maxTime = "Unavailable";
                }

                double lat;
                if (jsonObject.has("Lat")) {
                    lat = jsonObject.getDouble("Lat");
                } else {
                    break;
                }

                double lon;
                if (jsonObject.has("Long")) {
                    lon = jsonObject.getDouble("Long");
                } else {
                    break;
                }

                // Make a new parkingSpot and add it to the list
                GeoPoint geo = new GeoPoint(lat, lon);
                parkingList.add(new ParkingSpot(name, geo, availableSpaces, totalSpaces, costInfo, currentCost, maxTime));
            }

        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            // Finished, publish data via AsyncResponse
            delegate.processFinish(parkingList);
        }

    }
}
