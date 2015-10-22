package org.datadiode.model.event;

import java.io.Serializable;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
public class GeoLocation implements Serializable {

    private double longitude;
    private double latitude;

    public GeoLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
