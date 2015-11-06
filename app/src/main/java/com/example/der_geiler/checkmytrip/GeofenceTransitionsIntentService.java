package com.example.der_geiler.checkmytrip;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService
{
    public GeofenceTransitionsIntentService (){super("GeofenceTransitionsIntentService");};

    protected void onHandleIntent(Intent intent)
    {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
            /*
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            */
            Log.e("geoFenceCSV", "hasError");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            MediaPlayer mp;
            int id = R.raw.out;

            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            {
                try
                {
                    FileHandler.GetInstance().SaveTrip(Globals.GetInstance(null).GetCurrentTrip());
                    Globals.GetInstance(null).cleanUp();
                    System.exit(0);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            {
                id = R.raw.in;
                //Start
            }
            mp = MediaPlayer.create(this.getApplicationContext(), id);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    // TODO Auto-generated method stub
                    mp.reset();
                    mp.release();
                    mp = null;
                }

            });
            mp.start();

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            /*
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            */
            Log.e("geoFenceCSV", String.valueOf(geofenceTransition));

                    // Send notification and log the transition details.
            /*
                    sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
            */
        }
        else
        {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            Log.e("geoFenceCSV", "Wrong type");
        }
    }
}
