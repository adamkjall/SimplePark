package com.dat256.grupp1.simplepark;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class NetworkUtils {
    private static final String PARK_BASE_URL = "http://data.goteborg.se/ParkingService/v2.1/PrivateTollParkings/54f155e0-85ca-4a46-8cb9-b980bb97a85e?";
    private static final String LATITUDE_PARAM = "latitude";
    private static final String LONGITUDE_PARAM = "longitude";
    private static final String RADIUS_PARAM = "radius";
    private static final String FORMAT_PARAM = "format";

    private static String lat = "57.699775";
    private static String lon = "11.9794782";
    private static String radius = "5000";
    private static String format = "JSON";

    /**
     * Helper method for getParkInfo, builds an URL with parameters
     * latitude, longitude, radius and format
     *
     * @return URL to parking data
     * @throws MalformedURLException
     */
    private static URL buildURL() throws MalformedURLException {
        Uri builtUri = Uri.parse(PARK_BASE_URL)
                .buildUpon()
                .appendQueryParameter(LATITUDE_PARAM, lat)
                .appendQueryParameter(LONGITUDE_PARAM, lon)
                .appendQueryParameter(RADIUS_PARAM, radius)
                .appendQueryParameter(FORMAT_PARAM, format)
                .build();
        return new URL(builtUri.toString());
    }

    /**
     * @param queryString
     * @return JSON string with parking data
     */
    static String getParkInfo(String queryString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonString = "";
        try {
            // Setup URL connection
            URL url = buildURL();
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the input stream
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader for the input stream
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a stringBuilder to hold the incoming response
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                // Since it's JSON, adding a new line isn't necessary (it won't
                // affect parsing) but it does make debugging a lot easier
                // if you print out the completed buffer for debugging
                builder.append("\n");
            }

            if (builder.length() == 0) {
                return null;
            }
            jsonString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the connection & reader
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonString;
    }
}
