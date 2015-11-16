package com.example.der_geiler.checkmytrip;

import java.util.Date;
import java.util.List;
import android.text.format.*;

/**
 * Created by der_geiler on 03-05-2015.
 */
public class Trip
{
    public List<A2BMarker> A2BMarkers;
    private Date dateStart, dateEnd;
    private int ticks = 0;
    private float distance;
    private int unit = DIST_UNIT_KILOMETERS;
    private String startGeo = null;

    Trip()
    {
        distance = 0;
    }

    public void setStartGeo(String geo){startGeo = geo;}
    public String getStartGeo(){return startGeo;}
    public Date GetTimeStart() {return dateStart;}
    public void SetTimeStart(Date start) {dateStart = start;}
    public String SetTimeEnd()
    {
        this.dateEnd = new Date();
        DateFormat df = new DateFormat();
        return df.format("yyyy-MM-dd@HH:mm", this.dateEnd).toString();
    }
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
                dist = String.format("%.2f", (distance / 1000)) + "km";
            else
                dist = String.valueOf((int)distance) + "m";
        }
        else
        {
            if(distance > 1609)
                dist = String.format("%.2f", (distance / 1000)) + "miles";
            else
                dist = String.valueOf((int)distance*1.0936) + "yards";
        }
        return dist;
    }
}
