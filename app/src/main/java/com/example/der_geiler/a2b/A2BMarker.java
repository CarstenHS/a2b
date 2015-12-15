package com.example.der_geiler.a2b;

import java.util.Date;

/**
 * Created by der_geiler on 14-05-2015.
 */

public class A2BMarker
{
    private double lat;
    private double lon;
    public Date date;

    A2BMarker(Date d, double lat, double lon)
    {
        date = d;
        this.lat = lat;
        this.lon = lon;
    }

    A2BMarker(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }
    double GetLat() {return lat;}
    double GetLon() {return lon;}
}
