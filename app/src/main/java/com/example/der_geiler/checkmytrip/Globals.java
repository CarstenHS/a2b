package com.example.der_geiler.checkmytrip;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

/**
 * Created by der_geiler on 13-05-2015.
 */
public class Globals implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback, OnDBInsertDoneCallback
{
    static private Trip currentTrip = null;
    public Timer TripTimer;
    public Timer durationTimer;
    private int SetMarkTimeoutMins = 2;
    private Location mLastLocation;
    static private GoogleApiClient mGoogleApiClient = null;
    private NewTripActivity mapActivity = null;
    private LocationRequest locationRequest;
    private a2bLoc lastLoc;
    private List<LatLng> currentLatLongs;
    static final int SPEED_UNIT_MS = 0;
    static final int SPEED_UNIT_KPH = 1;
    static final int SPEED_UNIT_MPH = 2;
    static final int DIST_UNIT_KILOMETERS = 0;
    static final int DIST_UNIT_MILES = 1;
    static private PendingIntent mGeofencePendingIntent = null;
    public int distUnit = DIST_UNIT_KILOMETERS;
    private int speedUnit = SPEED_UNIT_KPH;
    static private GoogleMap map;
    private static List<A2BGeofence> a2BGeofences;
    static public List<A2BCircle> circles;
    static public List<A2BdirInfo> dirEntries;
    public static final int RES_OK          = 0;
    public static final int RES_EXISTS      = -1;
    public static final int RES_INVALID     = -2;
    public static final int RES_EMPTY       = -3;
    public static final int GEO_FENCE_RADIUS = 100;
    public static final int COLOR_BASIC_GEOFENCE = 0xB2A9F6;
    static private Context ctx = null;
    static private FileHandler fileHandlerInstance = null;
    static private SQLiteHelper dbHelper = null;
    private int insertCount = 0;

    private static Globals instance;
    public Globals(){}
    public static Globals GetInstance(Context c)
    {
        ctx = (c != null) ? c : ctx;
        if(instance == null)
        {
            instance = new Globals();
            fileHandlerInstance = FileHandler.GetInstance();
            dirEntries = fileHandlerInstance.LoadDirInfos();
/*
            dirEntries.clear();
            fileHandlerInstance.SaveDirInfos(dirEntries);
*/
            if(dirEntries == null)
                dirEntries = new ArrayList<>();
            if(dirEntries.size() == 0)
            {
                A2BdirInfo di = new A2BdirInfo(FileHandler.GetInstance().getUncategorizedString());
                setDir(di);
            }
            a2BGeofences = fileHandlerInstance.LoadGeofences();
            dbHelper = new SQLiteHelper(ctx);
        }
        return instance;
    }

    public String setEndTimestamp(){return currentTrip.SetTimeEnd();}
    public void setInsertCount(int cnt){insertCount = cnt;}
    public SQLiteDatabase getWritableDB(){return dbHelper.getWritableDatabase();}
    public SQLiteDatabase getReadableDB(){return dbHelper.getReadableDatabase();}
    public SQLiteHelper getDbHelper(){return dbHelper;}

    public void removeDir(String name)
    {
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if(element.getDir().equals(name))
            {
                iter.remove();                          // remove from list circles list
            }
        }
        fileHandlerInstance.SaveDirInfos(dirEntries);
    }

    public A2BdirInfo getDir(String name)
    {
        if(dirEntries != null)
        {
            for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
            {
                A2BdirInfo element = iter.next();
                if (element.getDir().equals(name))
                    return element;
            }
        }
        return null;
    }

    static public void setDir(A2BdirInfo di)
    {
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if(element.equals(di))
            {
                iter.remove();
                break;
            }
        }
        dirEntries.add(di);
        fileHandlerInstance.SaveDirInfos(dirEntries);
    }

    static public void renameDir(A2BdirInfo di)
    {
        A2BdirInfo diInfo = null;
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if(element.equals(di))
            {
                di.setGeofenceEnd(element.getGeofenceEnd());
                di.setGeofenceStart(element.getGeofenceStart());
                iter.remove();
            }
            break;
        }
        dirEntries.add(di);
        fileHandlerInstance.SaveDirInfos(dirEntries);
    }

    /* General todos:
    todo: auto-filtering of trips

    2nd:
        todo: Loading of types can be templates
        todo: Removing entry in array should be template
        todo: setTouchActions is used in dirGeo also. Could be made as lib
        todo: OnConnection lost etc???
     */
    /************* GEOFENCING *****************/

    List<A2BGeofence> getGeoFencesPersist()
    {
        List<A2BGeofence> geofences = null;
        if(a2BGeofences != null)
        {
            geofences = new ArrayList<>();
            for (Iterator<A2BGeofence> iter = a2BGeofences.iterator(); iter.hasNext(); )
                geofences.add((A2BGeofence) iter.next());
        }
        return geofences;
    }

    static public void createAndDrawGeofenceCircles()
    {
        if(a2BGeofences != null && a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                circles.add(new A2BCircle(createAndDrawGeofenceCircle(gf.getLat(), gf.getLon()), gf.getName()));
        }
    }
    @Override
    public void onResult(Result result)
    {
        result = result;
    }

    public void setMap(GoogleMap map){this.map = map;}

    static private GeofencingRequest getGeofencingRequest(List<Geofence> geofences)
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofences);
        return builder.build();
    }

    static public Circle createAndDrawGeofenceCircle(double lat, double lon)
    {
        return map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon)).radius(100)
                .fillColor(COLOR_BASIC_GEOFENCE));
    }

    static private Geofence createMapGeofence(A2BGeofence a2bGeofence)
    {
        Geofence g = new Geofence.Builder()
                .setRequestId(a2bGeofence.name)
                .setCircularRegion(a2bGeofence.lat, a2bGeofence.lon, GEO_FENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        return g;
    }

    private PendingIntent getGeofencePendingIntent()
    {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null)
            return mGeofencePendingIntent;

        Intent intent = new Intent(ctx, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void stopGeofences()
    {
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    public void initGeofences()
    {
        if(a2BGeofences != null && a2BGeofences.size() != 0)
        {
            List<Geofence> mapGeofences = new ArrayList<>();

            for (A2BGeofence gf : a2BGeofences)
                mapGeofences.add(createMapGeofence(gf));
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(mapGeofences),
                    getGeofencePendingIntent()
            ).setResultCallback(this);

            createAndDrawGeofenceCircles();
        }
    }

    public int saveGeofence(A2BGeofence a2bGf)
    {
        a2BGeofences.add(a2bGf);
        FileHandler.GetInstance().SaveGeofences(getGeoFencesPersist());
        return RES_OK;
    }

    public int nameOk(String name)
    {
        if(name.equals(""))
            return RES_EMPTY;
        if(a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                if(name.equals(gf.name))
                    return RES_EXISTS;
        }
        return RES_OK;
    }

    public static A2BGeofence anyGeofenceHit(LatLng llPress)
    {
        float results[] = new float[3];
        if(a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
            {
                Location.distanceBetween(llPress.latitude, llPress.longitude, gf.lat, gf.lon, results);
                if(results[0] < GEO_FENCE_RADIUS)
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
            if(element.getName().equals(name))
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
            if(element.getName().equals(name))
                return element.getCircle();
        }
        return null;
    }

    public void addCircle(A2BCircle c){circles.add(c);}

    public void removeGeofence(String name)
    {
        for (Iterator<A2BGeofence> iter = a2BGeofences.iterator(); iter.hasNext(); )
        {
            A2BGeofence element = iter.next();
            if(element.name.equals(name))
            {
                List<String> names = new ArrayList<>(1);
                names.add(name);
                removeCircle(name);
                iter.remove();                          // remove from list circles list
                // remove from geofence framework
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, names);
                FileHandler.GetInstance().SaveGeofences(a2BGeofences);
                break;
            }
        }
    }

    public List resolveGeoDir(String endGeo)
    {
        List<String> dirs = new ArrayList<>();
        if(dirEntries != null)
        {
            for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
            {
                A2BdirInfo element = iter.next();
                if (element.getGeofenceStart().equals(currentTrip.getStartGeo())
                    &&
                   (element.getGeofenceEnd().equals(endGeo)))
                {
                    dirs.add(element.getDir());
                }
            }
        }
        return dirs;
    }

    /************* GEOFENCING END *****************/

    public class a2bLoc
    {
        public Location lastLocation;
        public Date lastDate;
        public LatLng getLatlng()
        {
            return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    public String ExtractDurationFromTicks(int ticks)
    {
        String hours = null;
        String mins = null;
        String secs = null;
        int unit = 60*60;

        int temp = ticks / unit;
        hours  = (temp != 0) ? String.format("%02d", temp) : "00";

        ticks -= temp*unit;

        unit = 60;
        temp = (ticks / unit);
        mins = (temp != 0) ? String.format("%02d", temp) : "00";

        ticks -= temp*unit;

        unit = 1;
        temp = ticks / unit;
        secs = (temp != 0) ? String.format("%02d", temp) : "00";

        return hours + ":" + mins + ":" + secs;
    }

    public a2bLoc getLastLoc(){return lastLoc;}

    public void setCurrentTrip(Trip trip)
    {
        if(currentTrip == null)
            currentLatLongs = new ArrayList<LatLng>();
        currentTrip = trip;
    }

    static public Trip GetCurrentTrip(){return currentTrip;}

    public List<LatLng> GetLatLngs(){return currentLatLongs;}

    public void StartTimers()
    {
        MarkerTask task = new MarkerTask();
        TripTimer = new Timer();
        int timeout = 1000 * 60 * SetMarkTimeoutMins;
        TripTimer.schedule(task, timeout, timeout);

        durationTimer = new Timer();
        durationTimer.schedule(new DurationTask(), 1000, 1000);
    }


    public void SetMapVisible(NewTripActivity activity)
    {
        mapActivity = activity;
        if(mGoogleApiClient != null)
            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public LatLng UpdateLocation()
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng ll = null;
        if(mLastLocation != null)
        {
            double lat = mLastLocation.getLatitude();
            double lon = mLastLocation.getLongitude();
            currentTrip.addA2bMarker(new A2BMarker(new Date(), lat, lon));
            currentLatLongs.add(new LatLng(lat, lon));
            ll = currentLatLongs.get(currentLatLongs.size()-1);
        }
        return ll;
    }

    public GoogleApiClient buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mGoogleApiClient;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Date now = new Date();
        if (lastLoc != null)
        {
            float speed = location.getSpeed();
            if(speed != 0)
            {
                float dist = lastLoc.lastLocation.distanceTo(location);
                currentTrip.UpdateDistance(dist);
            }
            if (mapActivity != null)
            {
                mapActivity.UpdateUIElement(NewTripActivity.UI_ELEMENT_DISTANCE, currentTrip.getFormattedDistance());
                mapActivity.UpdateUIElement(NewTripActivity.UI_ELEMENT_SPEED, ConvertSpeed(speed));
            }
        }
        else
            lastLoc = new a2bLoc();

        lastLoc.lastLocation = location;
        lastLoc.lastDate = now;
    }

    private String ConvertSpeed(float speed)
    {
        switch(speedUnit)
        {
            case SPEED_UNIT_MS: return String.valueOf(speed) + "m/s";
            case SPEED_UNIT_KPH: return String.format("%.2f", (speed * 3.6)) + "km/h";
            case SPEED_UNIT_MPH: return String.format("%.2f", (speed * 2.24)) + "mph";
        }
        return "-1-1-1";
    }

    public void onStatusChanged(String provider, int status, Bundle extras){}

    public void onProviderEnabled(String provider){}

    public void onProviderDisabled(String provider){}

    private LocationRequest createLocationReq()
    {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        StartTimers();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationReq(), this);

        LatLng ll = UpdateLocation();

        if(mapActivity != null)
            mapActivity.SetMap(ll);

        map = mapActivity.getMap();
        if(circles == null)
            circles = new ArrayList<>();

        setGeofences();
    }

    static public boolean isGoogleApiConnectionState() {return mGoogleApiClient.isConnected();}

    public void setGeofences()
    {
        if(mGoogleApiClient.isConnected() == true)
        {
            initGeofences();
            createAndDrawGeofenceCircles();
        }
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}

    public class MarkerTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            LatLng ll = UpdateLocation();
            if(mapActivity != null)
                mapActivity.AddMarkerUI(ll, currentTrip.getNumMarkers() - 1);
        }
    }

    public class DurationTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            int ticks = currentTrip.IncTick();
            if(mapActivity != null)
                mapActivity.TickUI(ticks);
        }
    }

    public void insertInDB(Trip trip, String dir)
    {
        new SQLiteHelperThread().execute(SQLiteHelperThread.ACTION_INSERT, trip, dir, this);
    }

    @Override
    public void onDBInsertDone()
    {
        if(--insertCount == 0)
            cleanUp();
    }

    public void cleanUp()
    {
        if(TripTimer != null)
            TripTimer.cancel();
        if(durationTimer != null)
            durationTimer.cancel();
        if(mGoogleApiClient != null)
        {
            stopGeofences();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        instance = null;
        System.exit(0);
    }
}
