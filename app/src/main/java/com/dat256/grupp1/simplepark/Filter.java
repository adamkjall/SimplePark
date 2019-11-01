package com.dat256.grupp1.simplepark;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * The filter class is used to apply different filters to a generated list of parking spots
 * in order to fetch only the desired spots based on different criterion
 */

public class Filter {

    /**
     * Static method for filtering out the nearest parking spot to the user with regards to shortest
     * linear distance.
     *
     * @param parkingSpots The list of parking spots to be filtered through.
     * @param userPosition The users current GPS-position, used to calculate .
     */
    public static ParkingSpot showNearest(List<ParkingSpot> parkingSpots, GeoPoint userPosition) {
        ParkingSpot closest = null;
        double distance = Double.MAX_VALUE;
        double tmp;
        for (ParkingSpot p : parkingSpots) {
            tmp = userPosition.distanceToAsDouble(p.getGeoPoint());
            p.setVisible(false);
            if (tmp < distance) {
                closest = p;
                distance = tmp;
            }
        }
        if (closest != null) {
            closest.setVisible(true);
        }
        return closest;
    }

    /**
     * Returns the "optimal" parking spot in terms of checking all the nearest spots within a custom
     * radius and selecting the cheapest on from them.
     *
     * @param parkingSpots The list of parking spots to be filtered through.
     * @param userPosition The users current GPS-position, used to calculate distance.
     * @param radius       The radius to search in relative to the users position.
     * @return The optimal spot
     */

    public static ParkingSpot showCustomOptimal(List<ParkingSpot> parkingSpots, GeoPoint userPosition, double radius) {
        List<ParkingSpot> pList = new ArrayList<>();
        // Create temporary list with nearest spaces

        for (ParkingSpot p : parkingSpots) {
            if ((userPosition.distanceToAsDouble(p.getGeoPoint()) <= radius) && (p.getCurrentCost() != -1)) {
                pList.add(p);
            }
        }

        // From the temporary list, get the cheapest parking spot
        double cheapest = Double.MAX_VALUE;
        ParkingSpot cSpot = null;
        for (ParkingSpot p : pList) {
            if (p.getCurrentCost() < cheapest) {
                cheapest = p.getCurrentCost();
                cSpot = p;
            }
        }

        // Set every parking spot invisible
        for (ParkingSpot ps : parkingSpots) {
            ps.setVisible(false);
        }
        if (cSpot != null) {
            cSpot.setVisible(true);
        }
        return cSpot;
    }
}
