package com.example.der_geiler.checkmytrip;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by der_geiler on 03-05-2015.
 */
public class Trip
{
    public List<A2BMarker> A2BMarkers;
    private Date timeStart;
    private int ticks = 0;
    private float distance;
    private int unit = DIST_UNIT_KILOMETERS;

    Trip()
    {
        distance = 0;
    }

    public Date GetTimeStart() {return timeStart;}
    public void SetTimeStart(Date start) {timeStart = start;}
    public int IncTick()
    {
        return ++ticks;
    }
    public void UpdateDistance(float dist)
    {
        distance += dist;
    }
    public List<A2BMarker> getA2bMarkers() {return A2BMarkers;}

    static final int DIST_UNIT_KILOMETERS = 0;  //TODO: use ones from globals
    static final int DIST_UNIT_MILES = 1;       //TODO: use ones from globals

    public void setUnit(int unit){this.unit = unit;}

    public int getTicks(){return ticks;}

    public String GetDistance()
    {
        String dist;
        if(unit == DIST_UNIT_KILOMETERS)
        {
            if(distance > 1000)
            {
                dist = String.format("%.2f", (distance / 1000)) + "km";
            }
            else
            {
                dist = String.valueOf((int)distance) + "m";
            }
        }
        else
        {
            if(distance > 1609)
            {
                dist = String.format("%.2f", (distance / 1000)) + "miles";
            }
            else
            {
                dist = String.valueOf((int)distance*1.0936) + "yards";
            }
        }
        return dist;
    }
}
