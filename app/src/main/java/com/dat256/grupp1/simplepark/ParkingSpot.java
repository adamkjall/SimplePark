package com.dat256.grupp1.simplepark;

import org.osmdroid.util.GeoPoint;

/**
 * The ParkingSpot class describes the attributes of a parking space fetched from the Gothenburg
 * open API, for full documentation see: http://data.goteborg.se/ParkingService/v2.1/help
 */

public class ParkingSpot {
    private String name;
    private GeoPoint geoPoint;
    private int availableSpaces;
    private int totalSpaces;
    private String costInfo;
    private double currentCost;
    private String maxTime;

    private boolean visible;

    /**
     * Constructs a ParkingSpot object based on the data fetched from the Gothenburg API.
     *
     * @param name            Name om the parking space, "Unavailable" if unavailable
     * @param geoPoint        Latitude and Longitude coordinate of the parking space, (0,0) if unavailable
     * @param availableSpaces How many free spaces are available at the moment, -1 if unavailable
     * @param totalSpaces     How many total spaces in the parking spot, -1 if unavailable
     * @param costInfo        Price info for the parking spot, "Unavailable" if unavailable
     * @param currentCost     Current cost at this time, -1 if unavailable
     * @param maxTime         Maximum time you are allowed to park, "Unavailable" if unavailable
     */
    ParkingSpot(String name, GeoPoint geoPoint, int availableSpaces, int totalSpaces,
                String costInfo, double currentCost, String maxTime) {
        this.name = name;
        this.geoPoint = geoPoint;
        this.availableSpaces = availableSpaces;
        this.totalSpaces = totalSpaces;
        this.costInfo = costInfo;
        this.currentCost = currentCost;
        this.maxTime = maxTime;
        this.visible = true;
    }

    // Getters

    /**
     * @return Name of the parking spot
     */
    public String getName() {
        return name;
    }

    /**
     * @return Latitude and Longitude coordinate of the parking space, (0,0) if unavailable
     */
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    /**
     * @return How many free spaces are available at the moment, -1 if unavailable
     */
    public int getAvailableSpaces() {
        return availableSpaces;
    }

    /**
     * @return How many total spaces in the parking spot, -1 if unavailable
     */
    public int getTotalSpaces() {
        return totalSpaces;
    }

    /**
     * @return Price info for the parking spot, "Unavailable" if unavailable
     */
    public String getCostInfo() {
        return costInfo;
    }

    /**
     * @return Current cost at this time, -1 if unavailable
     */
    public double getCurrentCost() {
        return currentCost;
    }

    /**
     * @return Maximum time you are allowed to park, "Unavailable" if unavailable
     */
    public String getMaxTime() {
        return maxTime;
    }

    /**
     * @return The state of visibility, used for drawing the marker of the parking spot
     */
    public boolean getVisible() {
        return visible;
    }

    // Setters

    /**
     * @param visible Sets the visibility of this parking spot used for drawing the spots marker
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return The string representation of the parking spot
     */
    public String toString() {
        return "Name: " + name + "\n" +
                "Lat: " + geoPoint.getLatitude() + " Long: " + geoPoint.getLongitude() + "\n" +
                "Available spaces: " + availableSpaces + "\n" +
                "Total Spaces: " + totalSpaces + "\n" +
                "Cost Info: " + costInfo + "\n" +
                "Current Cost: " + currentCost + "\n" +
                "Max time: " + maxTime + "\n";
    }

}
