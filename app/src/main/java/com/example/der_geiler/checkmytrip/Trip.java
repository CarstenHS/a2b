package com.example.der_geiler.checkmytrip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.text.format.*;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by der_geiler on 03-05-2015.
 */
public class Trip
{
    private List<A2BMarker> A2BMarkers;
    private Date dateStart, dateEnd;
    private int ticks = 0;
    private float distance;
    private float topSpeedInMetPerSec;
    private int unit = DIST_UNIT_KILOMETERS;
    private String startGeo = null;
    private String endGeo = null;
    private final static String strFORMAT = "yyyy-MM-dd@HH:mm";

    Trip()
    {
        distance = 0;
        topSpeedInMetPerSec = 0;
    }

    public List<LatLng> getLatLngs()
    {
        List<LatLng> lls = new ArrayList<LatLng>();
        for(A2BMarker m: A2BMarkers)
            lls.add(new LatLng(m.GetLat(), m.GetLon()));
        return lls;
    }
    public void addA2bMarker(A2BMarker m){A2BMarkers.add(m);}
    public List<A2BMarker> getA2bMarkers() {return A2BMarkers;}
    public void setA2bMarkers(List<A2BMarker> markers) {A2BMarkers = markers;}
    public int getNumMarkers(){return A2BMarkers.size();}
    public A2BMarker getMarker(int index){return A2BMarkers.get(index);}
    public String getStartGeo(){return startGeo;}
    public String getEndGeo(){return endGeo;}
    public Date GetTimeStart() {return dateStart;}
    public int getTicks(){return ticks;}
    public long getStartTimestamp(){return dateStart.getTime();}
    public long getEndTimestamp(){return dateEnd.getTime();}
    public float getTopSpeed(){return topSpeedInMetPerSec;}
    public float getDistance(){return distance;}
    public String getFormattedDistance()
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

    public void setStartGeo(String geo){startGeo = geo;}
    public void setEndGeo(String geo){endGeo = geo;}
    public void SetTimeStart(Date start) {dateStart = start;}
    public String SetTimeEnd()
    {
        this.dateEnd = new Date();
        DateFormat df = new DateFormat();
        return df.format(strFORMAT, this.dateEnd).toString();
    }
    public String getFormattedTimeEnd()
    {
        DateFormat df = new DateFormat();
        return df.format(strFORMAT, this.dateEnd).toString();
    }
    static public String convertStampToName(long stamp)
    {
        DateFormat df = new DateFormat();
        return df.format(strFORMAT, stamp).toString();
    }
    public int IncTick()
    {
        return ++ticks;
    }
    public void UpdateDistance(float dist)
    {
        distance += dist;
    }
    public void UpdateSpeed(float speed)
    {
        topSpeedInMetPerSec = (speed > topSpeedInMetPerSec) ? speed : topSpeedInMetPerSec;
    }

    static final int DIST_UNIT_KILOMETERS = 0;  //TODO: use ones from globals
    static final int DIST_UNIT_MILES = 1;       //TODO: use ones from globals

    public void setUnit(int unit){this.unit = unit;}
}
