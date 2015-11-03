package com.example.der_geiler.checkmytrip;

import com.google.android.gms.maps.model.Circle;

/**
 * Created by der_geiler on 03-11-2015.
 */
public class A2BGeofencePersist
{
    protected double lat;
    protected double lon;
    protected String name;

    A2BGeofencePersist(){}

    A2BGeofencePersist(double lat, double lon, String name)
    {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }
    public double getLat(){return lat;}
    public double getLon(){return lon;}
    public String getName(){return name;}
}

class A2BGeofence extends A2BGeofencePersist
{
    private Circle circle;

    A2BGeofence(){}

    A2BGeofence(double lat, double lon, String name, Circle c)
    {
        super(lat, lon, name);
        circle = c;
    }
    Circle getCircle(){return circle;}
    void setCircle(Circle c){circle = c;}
}