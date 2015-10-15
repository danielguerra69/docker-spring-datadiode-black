package org.datadiode.model.event;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
public class Event implements Serializable {
    Date date;
    GeoLocation geoLocation;

    public Event(Date date, GeoLocation geoLocation) {
        this.date = date;
        this.geoLocation = geoLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
