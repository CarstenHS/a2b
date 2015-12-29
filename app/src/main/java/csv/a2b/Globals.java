package csv.a2b;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

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
public class Globals extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback, OnDBInsertDoneCallback
{
    static private Trip currentTrip = null;
    public Timer TripTimer;
    public Timer durationTimer;
    private Location mLastLocation;
    static private GoogleApiClient mGoogleApiClient = null;
    private boolean mapVisible = false;
    private Activity_newTrip mapActivity = null;
    private LocationRequest locationRequest;
    private a2bLoc lastLoc;
    static private Settings settings;
    private static final int DIST_UNIT_KILOMETERS = 0;
    //private static PendingIntent mGeofencePendingIntent = null;
    static private GoogleMap map;
    private static List<A2BGeofence> a2BGeofences;
    static public List<A2BCircle> circles;
    static public List<A2BdirInfo> dirEntries;
    public static final int RES_OK = 0;
    public static final int RES_EXISTS = -1;
    public static final int RES_INVALID = -2;
    public static final int RES_EMPTY = -3;
    public static final int GEO_FENCE_RADIUS = 100;
    public static final int COLOR_BASIC_GEOFENCE = 0xB2A9F6;
    static private Context ctx = null;
    static private FileHandler fileHandlerInstance = null;
    static private SQLiteHelper dbHelper = null;
    private int insertCount = 0;
    static private final String strNotSet = "Not set";
    static private boolean geofenceActivated = false;
    static private final int GEO_INACTIVITY_TIMEOUT = 1000;
    static private NotificationManager mNM;
    private int NOTIFICATION = 1;
    private Service serviceRef = null;
    static private boolean isServStarted = false;

    private static Globals instance;

    public Globals(){}

    public void setService(Service s){serviceRef = s;}
    static public boolean isServiceStarted(){return isServStarted;}

    private Notification createNotification()
    {
        mNM = (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "A2B";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setSmallIcon(R.drawable.notif)
                .setContentTitle("A2B")
                .setTicker(text)
                .setContentText("Return to A2B.")
                .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(ctx, Activity_main.class);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notif = builder.build();
        return notif;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        // not bindable, hence return null
        return null;
    }

     @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(instance.isServiceStarted() == false)
            Globals.GetInstance(null).setService(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
    }

    public static Globals GetInstance(Context c)
    {
        ctx = (c != null) ? c : ctx;
        if (instance == null)
        {
            instance = new Globals();

            fileHandlerInstance = FileHandler.GetInstance();
            dirEntries = fileHandlerInstance.LoadDirInfos();
            settings = fileHandlerInstance.loadSettings();
            settings = (settings != null) ? settings : new Settings();
            if(circles == null)
                circles = new ArrayList<>();
            if (dirEntries == null)
                dirEntries = new ArrayList<>();
            if (dirEntries.size() == 0)
            {
                A2BdirInfo di = new A2BdirInfo(FileHandler.GetInstance().getUncategorizedString());
                setDir(di);
            }
            a2BGeofences = fileHandlerInstance.LoadGeofences();
            a2BGeofences = (a2BGeofences != null) ? a2BGeofences : new ArrayList<A2BGeofence>();
            dbHelper = new SQLiteHelper(ctx);
        }
        return instance;
    }
    /*** accessing toolkit from secondary thread END ***/

    public void updateSettings(Settings settings)
    {
        this.settings = settings;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public String setEndTimestamp()
    {
        return currentTrip.SetTimeEnd();
    }

    public void setInsertCount(int cnt)
    {
        insertCount = cnt;
    }

    public SQLiteDatabase getWritableDB()
    {
        return dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDB()
    {
        return dbHelper.getReadableDatabase();
    }

    public SQLiteHelper getDbHelper()
    {
        return dbHelper;
    }

    public void removeDir(String name)
    {
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if (element.getDir().equals(name))
            {
                iter.remove();                          // remove from list circles list
            }
        }
        fileHandlerInstance.SaveDirInfos(dirEntries);
    }

    public A2BdirInfo getDir(String name)
    {
        if (dirEntries != null)
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
            if (element.equals(di))
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
        for (Iterator<A2BdirInfo> iter = dirEntries.iterator(); iter.hasNext(); )
        {
            A2BdirInfo element = iter.next();
            if (element.equals(di))
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
    /*************
     * GEOFENCING
     *****************/

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

    static public void createAndDrawGeofenceCircles()
    {
        if (a2BGeofences != null && a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                circles.add(new A2BCircle(createAndDrawGeofenceCircle(gf.getLat(), gf.getLon()), gf.getName()));
        }
    }

    @Override
    public void onResult(Result result){}

    public void setMap(GoogleMap map)
    {
        this.map = map;
    }

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
        //if (mGeofencePendingIntent != null)
        //return mGeofencePendingIntent;

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
        if (a2BGeofences != null && a2BGeofences.size() != 0)
        {
            new Timer().schedule(new InactivityTask(), GEO_INACTIVITY_TIMEOUT);
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
        if (name.equals(""))
            return RES_EMPTY;
        if (a2BGeofences.size() != 0)
        {
            for (A2BGeofence gf : a2BGeofences)
                if (name.equals(gf.name))
                    return RES_EXISTS;
        }
        return RES_OK;
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
                // remove from geofence framework
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, names);
                FileHandler.GetInstance().SaveGeofences(a2BGeofences);
                break;
            }
        }
        resetGeoInDirInfo(name);
        if (a2BGeofences.size() < 2)
        {
            settings.setAppMode(Settings.APP_MODE_MANUAL);
            fileHandlerInstance.saveSettings(settings);
        }
    }

    public List resolveGeoDir(String endGeo)
    {
        List<String> dirs = new ArrayList<>();
        if (dirEntries != null)
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

    /*************
     * GEOFENCING END
     *****************/

    public class a2bLoc
    {
        private Location lastLocation;

        public LatLng getLatlng()
        {
            return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        public void setLocation(Location l)
        {
            lastLocation = l;
        }
    }

    public String ExtractDurationFromTicks(int ticks)
    {
        String hours;
        String mins;
        String secs;
        int unit = 60 * 60;

        int temp = ticks / unit;
        hours = (temp != 0) ? String.format("%02d", temp) : "00";

        ticks -= temp * unit;

        unit = 60;
        temp = (ticks / unit);
        mins = (temp != 0) ? String.format("%02d", temp) : "00";

        ticks -= temp * unit;

        unit = 1;
        temp = ticks / unit;
        secs = (temp != 0) ? String.format("%02d", temp) : "00";

        return hours + ":" + mins + ":" + secs;
    }

    public a2bLoc getLastLoc()
    {
        return lastLoc;
    }

    static public Trip GetCurrentTrip()
    {
        return currentTrip;
    }

    public void StartTimers()
    {
        MarkerTask task = new MarkerTask();
        TripTimer = new Timer();
        int timeout = 1000 * 60 * settings.getMarkerTimeout();
        TripTimer.schedule(task, timeout, timeout);

        durationTimer = new Timer();
        durationTimer.schedule(new DurationTask(), 1000, 1000);
    }

    public void setMapActivity(Activity_newTrip activity){mapActivity = activity;}

    public void SetMapVisible(boolean visible)
    {
        mapVisible = visible;
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
            ll = new LatLng(lat, lon);
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
        if (lastLoc != null && currentTrip != null)
        {
            float speed = location.getSpeed();
            if(speed != 0)
            {
                float dist = lastLoc.lastLocation.distanceTo(location);
                currentTrip.UpdateDistance(dist);
                currentTrip.UpdateSpeed(speed);
            }
            if (mapVisible)
            {
                mapActivity.UpdateUIElement(Activity_newTrip.UI_ELEMENT_DISTANCE, currentTrip.getFormattedDistance(settings.getSpeedUnit()));
                mapActivity.UpdateUIElement(Activity_newTrip.UI_ELEMENT_SPEED, ConvertSpeed(speed));
            }
            lastLoc.setLocation(location);
        }
        else
        {
            lastLoc = new a2bLoc();
            lastLoc.setLocation(location);
            LatLng ll = lastLoc.getLatlng();
            if(this.map != null && mapVisible)
                mapActivity.zoomToPosition(ll);
        }
    }

    private String ConvertSpeed(float speed)
    {
        switch(settings.getSpeedUnit())
        {
            case Settings.SPEED_UNIT_MS: return String.valueOf(speed) + "m/s";
            case Settings.SPEED_UNIT_KPH: return String.format("%.2f", (speed * 3.6)) + "km/h";
            case Settings.SPEED_UNIT_MPH: return String.format("%.2f", (speed * 2.24)) + "mph";
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationReq(), this);
        if(mapActivity != null)
            map = mapActivity.getMap();
        if(circles == null)
            circles = new ArrayList<>();

        if(map != null)
            setGeofences();
    }

    public void startA2bService()
    {
        if(isServStarted == false)
        {
            isServStarted = true;
            serviceRef.startForeground(NOTIFICATION, createNotification());
        }
    }

    public void startTrip()
    {
        currentTrip = new Trip();
        currentTrip.setA2bMarkers(new ArrayList<A2BMarker>());
        currentTrip.SetTimeStart(new Date());
        StartTimers();

        LatLng ll = UpdateLocation();

        if(mapVisible)
            mapActivity.setMapExt(ll);

        startA2bService();
    }

    static public GoogleApiClient getGoogleApiClient(){return (mGoogleApiClient);}

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
            if(mapVisible)
                mapActivity.AddMarkerUI(ll, currentTrip.getNumMarkers() - 1);
        }
    }
    public class DurationTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            int ticks = currentTrip.IncTick();
            if(mapVisible)
                mapActivity.TickUI(ticks);
        }
    }

    public class InactivityTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            geofenceActivated = true;
        }
    }

    public boolean getGeofenceActivated(){return geofenceActivated;}

    public void insertInDB(Trip trip, String dir)
    {
        new SQLiteHelperThread().execute(SQLiteHelperThread.ACTION_INSERT, trip, dir, this);
    }

    @Override
    public void onDBInsertDone()
    {
        if(--insertCount == 0)
            instance.cleanUp();
    }

    public void cleanUp()
    {
        geofenceActivated = false;
        if(TripTimer != null)
            TripTimer.cancel();
        if(durationTimer != null)
            durationTimer.cancel();
        if(mGoogleApiClient != null)
        {
            stopGeofences();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        currentTrip = null;
        if(mapActivity != null)
            mapActivity.finish();
        if(isServStarted == true)
        {
            serviceRef.stopForeground(true);
            isServStarted = false;
        }
    }

}
