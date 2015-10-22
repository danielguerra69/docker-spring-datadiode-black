package org.datadiode.model.event.sensor;

import org.datadiode.model.event.Event;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by marcelmaatkamp on 15/10/15.
 */
public class SensorEvent implements Serializable {
    Event event;

    public SensorEvent(Sensor sensor) {
        this.event = new Event(new Date(), sensor.getGeoLocation());
    }

    public SensorEvent() {

    }
}
