package csv.a2b.free;

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
    private float distance = 0;
    private float topSpeedInMetPerSec = 0;
    private String startGeo = null;
    private String endGeo = null;
    static public final String strFORMAT = "yyyy-MM-dd@HH:mm";

    Trip(){}

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
    public int getTicks(){return ticks;}
    public long getEndTimestamp(){return dateEnd.getTime();}
    public float getTopSpeed(){return topSpeedInMetPerSec;}
    public float getDistance(){return distance;}
    public String getFormattedDistance(int unit)
    {
        String dist;
        if(unit == Settings.SPEED_UNIT_KPH)
        {
            if(distance > 1000)
                dist = String.format("%.2f", (distance / 1000)) + " km";
            else
                dist = String.valueOf((int)distance) + " m";
        }
        else
        {
            if(distance > 1609)
                dist = String.format("%.2f", (distance / 1000)) + " miles";
            else
                dist = String.valueOf((int)(distance*1.0936)) + " yards";
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
}
