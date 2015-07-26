package com.example.der_geiler.checkmytrip;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by der_geiler on 14-05-2015.
 */

public class A2BMarker
{
    private double lat;
    private double lon;
    public Date date;

    A2BMarker(Date d)
    {
        date = d;
        lat = 0;
        lon = 0;
    }

    A2BMarker(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }
    double GetLat() {return lat;}
    double GetLon() {return lon;}
}