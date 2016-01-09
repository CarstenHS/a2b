package csv.a2b;

import android.location.Location;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by der_geiler on 09-01-2016.
 */
public class DelegGeofence
{
    public static final int GEO_FENCE_RADIUS = 100;
    public static final int COLOR_BASIC_GEOFENCE = 0xB2A9F6;
    private static List<A2BGeofence> a2BGeofences;
    static public List<A2BCircle> circles;
    static private String currGeofence = null;
    static public List<A2BdirInfo> dirEntries;
    static private final String strNotSet = "Not set";
    private FileHandler fileHandlerInstance = null;
    private Settings settings = null;
    public final static int GEOFENCE_EXIT = 0;
    public final static int GEOFENCE_ENTER = 1;
    private IA2BGeofenceCallbacks listener = null;

    private static DelegGeofence ourInstance = new DelegGeofence();


    public static DelegGeofence getInstance()
    {
        return ourInstance;
    }

    private DelegGeofence()
    {
    }

    public void Init(Settings set, IA2BGeofenceCallbacks cb)
    {
        listener = cb;
        if(circles == null)
            circles = new ArrayList<>();
        fileHandlerInstance = FileHandler.GetInstance();
        a2BGeofences = fileHandlerInstance.LoadGeofences();
        a2BGeofences = (a2BGeofences != null) ? a2BGeofences : new ArrayList<A2BGeofence>();
        settings = set;
        if(circles == null)
            circles = new ArrayList<>();
    }

    List<A2BGeofence> getGeoFencesPersist()
    {
        List<A2BGeofence> geofences = null;
        if (a2BGeofences != null)
        {
            geofences = new ArrayList<>();
            for (Iterator<A2BGeofence> iter = a2BGeofences.iterator(); iter.hasNext(); )
                geofences.add(iter.next());
        }
        return geofences;
    }

    public void createAndDrawGeofenceCircles(GoogleMap map)
    {
        if (a2BGeofences != null && a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                circles.add(new A2BCircle(createAndDrawGeofenceCircle(gf.getLat(), gf.getLon(), map), gf.getName()));
        }
    }

    public Circle createAndDrawGeofenceCircle(double lat, double lon, GoogleMap map)
    {
        return map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon)).radius(100)
                .fillColor(COLOR_BASIC_GEOFENCE));
    }

    public void initGeofences(GoogleMap map)
    {
        if (a2BGeofences != null && a2BGeofences.size() != 0)
            createAndDrawGeofenceCircles(map);
    }

    public int saveGeofence(A2BGeofence a2bGf)
    {
        a2BGeofences.add(a2bGf);
        FileHandler.GetInstance().SaveGeofences(getGeoFencesPersist());
        return Globals.RES_OK;
    }

    public int nameOk(String name)
    {
        if (name.equals(""))
            return Globals.RES_EMPTY;
        if (a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                if (name.equals(gf.name))
                    return Globals.RES_EXISTS;
        }
        return Globals.RES_OK;
    }

    public static A2BGeofence anyGeofenceHit(LatLng llPress)
    {
        float results[] = new float[3];
        if (a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
            {
                Location.distanceBetween(llPress.latitude, llPress.longitude, gf.lat, gf.lon, results);
                if (results[0] < GEO_FENCE_RADIUS)
                    return gf;
            }
        }
        return null;
    }

    public void removeCircle(String name)
    {
        for (Iterator<A2BCircle> iter = circles.iterator(); iter.hasNext(); )
        {
            A2BCircle element = iter.next();
            if (element.getName().equals(name))
            {
                element.getCircle().remove();           // remove from map
                iter.remove();                          // remove from list circles list
            }
        }
    }

    public Circle getCircle(String name)
    {
        for (Iterator<A2BCircle> iter = circles.iterator(); iter.hasNext(); )
        {
            A2BCircle element = iter.next();
            if (element.getName().equals(name))
                return element.getCircle();
        }
        return null;
    }

    public void addCircle(A2BCircle c)
    {
        circles.add(c);
    }

    private void resetGeoInDirInfo(String geo)
    {
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if (element.getGeofenceEnd().equals(geo))
                element.setGeofenceEnd(strNotSet);
            if (element.getGeofenceStart().equals(geo))
                element.setGeofenceStart(strNotSet);
        }
    }

    public void removeGeofence(String name)
    {
        for (Iterator<A2BGeofence> iter = a2BGeofences.iterator(); iter.hasNext(); )
        {
            A2BGeofence element = iter.next();
            if (element.name.equals(name))
            {
                List<String> names = new ArrayList<>(1);
                names.add(name);
                removeCircle(name);
                iter.remove();                          // remove from list circles list
                FileHandler.GetInstance().SaveGeofences(a2BGeofences);
                break;
            }
        }
        resetGeoInDirInfo(name);
        if (a2BGeofences.size() < 2)
        {
            settings.setAppMode(Settings.APP_MODE_MANUAL);
            fileHandlerInstance.saveSettings(settings);
            Globals.GetInstance(null).updateSettings(settings);
        }
    }

    public List resolveGeoDir(String endGeo, String startGeo)
    {
        List<String> dirs = new ArrayList<>();
        if (dirEntries != null)
        {
            for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
            {
                A2BdirInfo element = iter.next();
                if (element.getGeofenceStart().equals(startGeo)
                        &&
                        (element.getGeofenceEnd().equals(endGeo)))
                {
                    dirs.add(element.getDir());
                }
            }
        }
        return dirs;
    }

    public void locationUpdate(LatLng ll)
    {
        String geo = anyGeofenceHit(ll).getName();
        if(geo != null)
        {
            if(currGeofence == null)
            {
                listener.A2BGeofenceChange(GEOFENCE_ENTER, geo);
                currGeofence = geo;
            }
        }
        else
        {
            if(currGeofence != null) // exited geoFence ?
            {
                listener.A2BGeofenceChange(GEOFENCE_EXIT, currGeofence);
                currGeofence = null;
            }
        }
    }
}
