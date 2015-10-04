package com.example.der_geiler.checkmytrip;


import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
/*
import android.location.LocationListener;
*/
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

/**
 * Created by der_geiler on 13-05-2015.
 */
public class Globals extends Application implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback
{
    private Trip currentTrip = null;
    public Timer TripTimer;
    public Timer durationTimer;
    private int SetMarkTimeoutMins = 2;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient = null;
    private NewTripActivity mapActivity = null;
    private LocationRequest locationRequest;
    private a2bLoc lastLoc;
    private List<LatLng> currentLatLongs;
    static final int SPEED_UNIT_MS = 0;
    static final int SPEED_UNIT_KPH = 1;
    static final int SPEED_UNIT_MPH = 2;
    static final int DIST_UNIT_KILOMETERS = 0;
    static final int DIST_UNIT_MILES = 1;
    private PendingIntent mGeofencePendingIntent = null;
    public int distUnit = DIST_UNIT_KILOMETERS;
    private int speedUnit = SPEED_UNIT_KPH;
    private GoogleMap map;

    /* General todos:
    todo: redraw fences on change

     */
    /************* GEOFENCING *****************/

    @Override
    public void onResult(Result result)
    {
        result = result;
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences)
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private Geofence createGeofence(double lat, double lon, String id)
    {
        float radiusInMeters = 100;
        Geofence g = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lon, radiusInMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon)).radius(radiusInMeters)
                .fillColor(Color.parseColor("#B2A9F6")));

        return g;
    }

    private PendingIntent getGeofencePendingIntent()
    {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null)
        {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void stopGeofences()
    {
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    public void createGeofence()
    {
        // TODO: Get from somewhere
        boolean useGeofence = true;
        if(useGeofence)
        {
            List<Geofence> geofences = new ArrayList<>();

            geofences.add(createGeofence(55.6563766, 12.6124786, "hjem"));
            geofences.add(createGeofence(55.722849, 12.4238959, "techpeople"));

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(geofences),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
        else
        {
            map.addCircle(new CircleOptions()
                    .center(new LatLng(55.6563766, 12.6124786)).radius(100)
                    .fillColor(Color.parseColor("#B2A9F6")));

            map.addCircle(new CircleOptions()
                    .center(new LatLng(55.722849, 12.4238959)).radius(100)
                    .fillColor(Color.parseColor("#B2A9F6")));
        }
    }

    /************* GEOFENCING END *****************/

    private class a2bLoc
    {
        public Location lastLocation;
        public Date lastDate;
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

    public void setCurrentTrip(Trip trip)
    {
        if(currentTrip == null)
            currentLatLongs = new ArrayList<LatLng>();
        currentTrip = trip;
    }

    public Trip GetCurrentTrip()
    {
        return currentTrip;
    }

    public List<LatLng> GetLatLngs()
    {
        return currentLatLongs;
    }

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
    }

    public LatLng UpdateLocation()
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double lat = mLastLocation.getLatitude();
        double lon = mLastLocation.getLongitude();
        currentTrip.A2BMarkers.add(new A2BMarker(new Date(), lat, lon));
        currentLatLongs.add(new LatLng(lat, lon));
        return currentLatLongs.get(currentLatLongs.size()-1);
    }

    protected synchronized GoogleApiClient buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
                mapActivity.UpdateUIElement(NewTripActivity.UI_ELEMENT_DISTANCE, currentTrip.GetDistance());
                mapActivity.UpdateUIElement(NewTripActivity.UI_ELEMENT_SPEED, ConvertSpeed(speed));
            }
        }
        else
        {
            lastLoc = new a2bLoc();
        }
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
        LatLng ll = UpdateLocation();

        if(mapActivity != null)
        {
            mapActivity.SetMap(ll);
        }
        StartTimers();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationReq(), this);
        map = mapActivity.getMap();
        createGeofence();
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    public class MarkerTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            LatLng ll = UpdateLocation();
            if(mapActivity != null)
            {
                mapActivity.AddMarkerUI(ll, currentTrip.A2BMarkers.size() - 1);
            }
        }
    }

    public class DurationTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            int ticks = currentTrip.IncTick();
            if(mapActivity != null)
            {
                mapActivity.TickUI(ticks);
            }
        }
    }

    public void cleanUp()
    {
        TripTimer.cancel();
        durationTimer.cancel();
        stopGeofences();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

}
