package csv.a2b.free;

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

    double GetLat() {return lat;}
    double GetLon() {return lon;}
}
