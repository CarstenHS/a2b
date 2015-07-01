package com.example.der_geiler.checkmytrip;


import android.app.Application;
import android.location.Location;
/*
import android.location.LocationListener;
*/
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
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

    public int distUnit = DIST_UNIT_KILOMETERS;
    private int speedUnit = SPEED_UNIT_KPH;

    private class a2bLoc
    {
        public Location lastLocation;
        public Date lastDate;
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

    public void StartApi()
    {
        buildGoogleApiClient();
    }

    public LatLng UpdateLocation()
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        currentTrip.A2BMarkers.add(new A2BMarker(new Date()));
        currentLatLongs.add(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        return currentLatLongs.get(currentLatLongs.size()-1);
    }

    protected synchronized void buildGoogleApiClient()
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
                mapActivity.UpdateUIElement(NewTripActivity.UI_ELEMENT_DISTANCE, currentTrip.GetDistance(distUnit));
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
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
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

}
