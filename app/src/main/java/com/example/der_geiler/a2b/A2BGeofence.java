package com.example.der_geiler.a2b;

/**
 * Created by der_geiler on 03-11-2015.
 */
public class A2BGeofence
{
    protected double lat;
    protected double lon;
    protected String name;

    A2BGeofence(){}

    A2BGeofence(double lat, double lon, String name)
    {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }
    public double getLat(){return lat;}
    public double getLon(){return lon;}
    public String getName(){return name;}
}