package csv.a2b;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by der_geiler on 13-05-2015.
 */
/*
public class Globals extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback, OnDBInsertDoneCallback, IA2BGeofenceCallbacks
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
    static private GoogleMap map;
    static public List<A2BdirInfo> dirEntries;
    public static final int RES_OK = 0;
    public static final int RES_EXISTS = -1;
    public static final int RES_INVALID = -2;
    public static final int RES_EMPTY = -3;
    static private Context ctx = null;
    static private FileHandler fileHandlerInstance = null;
    static private SQLiteHelper dbHelper = null;
    private int insertCount = 0;
    static private NotificationManager mNM;
    private int NOTIFICATION = 1;
    private Service serviceRef = null;
    static private boolean isServStarted = false;

    private int mockCnt = 0;

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

    static public Globals GetInstance(Context c)
    {
        ctx = (c != null) ? c : ctx;
        if (instance == null)
        {
            instance = new Globals();

            fileHandlerInstance = FileHandler.GetInstance();
            dirEntries = fileHandlerInstance.LoadDirInfos();
            settings = fileHandlerInstance.loadSettings();
            settings = (settings != null) ? settings : new Settings();
            if (dirEntries == null)
                dirEntries = new ArrayList<>();
            if (dirEntries.size() == 0)
            {
                A2BdirInfo di = new A2BdirInfo(FileHandler.GetInstance().getUncategorizedString());
                setDir(di);
            }
            dbHelper = new SQLiteHelper(ctx);
            DelegGeofence.getInstance().Init(settings, instance);
        }
        return instance;
    }

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

    @Override
    public void onResult(Result result){}

    public void setMap(GoogleMap map)
    {
        this.map = map;
    }

    @Override
    public void A2BGeofenceChange(int action, String geo)
    {
        switch(action)
        {
            case DelegGeofence.GEOFENCE_ENTER:
            {
                if(currentTrip != null)
                {
                    try
                    {
                        String startGeo = currentTrip.getStartGeo();
                        if(startGeo != null && startGeo.equals(geo) == false) // no re-entrent
                        {
                            currentTrip.setEndGeo(geo);

                            FileHandler fh = FileHandler.GetInstance();
                            setEndTimestamp();
                            List dirsToSaveIn = DelegGeofence.getInstance().resolveGeoDir(geo, currentTrip.getStartGeo());
                            FileHandler.GetInstance().SaveTrip(dirsToSaveIn, currentTrip);
                            String saveDir;
                            if (dirsToSaveIn.size() == 0)
                            {
                                saveDir = fh.getUncategorizedString();
                                setInsertCount(1);
                                insertInDB(currentTrip, saveDir);
                            } else
                            {
                                setInsertCount(dirsToSaveIn.size());
                                for (String s : (List<String>) dirsToSaveIn)
                                    insertInDB(currentTrip, s); // this results in end on callback
                            }
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case DelegGeofence.GEOFENCE_EXIT:
            {
                if(currentTrip == null)
                    Test_startTrip(geo);
                break;
            }
        }
    }

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

    //******************** testing

    public void Test_StartTimers()
    {
        Test_MarkerTask task = new Test_MarkerTask();
        TripTimer = new Timer();
        int timeout = 500 * 60 * settings.getMarkerTimeout();
        TripTimer.schedule(task, timeout, timeout);

        durationTimer = new Timer();
        durationTimer.schedule(new DurationTask(), 1000, 1000);
    }

    public void Test_StartMarkerTimer()
    {
        Test_MarkerTask task = new Test_MarkerTask();
        TripTimer = new Timer();
        int timeout = 500 * 60 * settings.getMarkerTimeout();
        TripTimer.schedule(task, timeout, timeout);
    }

    public void Test_StartDurationTimer()
    {
        durationTimer = new Timer();
        durationTimer.schedule(new DurationTask(), 1000, 1000);
    }
    public void Test_SetMapVisible(Activity_newTrip activity)
    {
        mapActivity = activity;
        if (mGoogleApiClient != null && mLastLocation != null)
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, mLastLocation);
    }

    public LatLng Test_UpdateLocation()
    {
        LatLng ll = null;
        if (mLastLocation != null)
        {
            double lat = mLastLocation.getLatitude();
            double lon = mLastLocation.getLongitude();
            if(currentTrip != null)
                currentTrip.addA2bMarker(new A2BMarker(new Date(), lat, lon));
            ll = new LatLng(lat, lon);
        }
        return ll;
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient,true);
        //Set test location
        mLastLocation=new Location("network");
        mLastLocation.setLatitude(51.553889);
        mLastLocation.setLongitude(-0.293616);
        mLastLocation.setAltitude(0);
        mLastLocation.setAccuracy(1);
        mLastLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        mLastLocation.setTime(System.currentTimeMillis());

        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, mLastLocation);
        lastLoc = new a2bLoc();
        lastLoc.setLocation(mLastLocation);
        Test_StartMarkerTimer();
        onLocationChanged(mLastLocation);
        setGeofences();
        if(settings.getAppMode() == Settings.APP_MODE_AUTO)
            DelegGeofence.getInstance().locationUpdate(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

    }

    public class Test_MarkerTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            switch(mockCnt)
            {
                case 0:
                {
                    // temp
                    mLastLocation.setLatitude(51.547247);
                    mLastLocation.setLongitude(-0.274680);
                    break;
                }
                case 1:
                {
                    //test1
                    mLastLocation.setLatitude(51.540302);
                    mLastLocation.setLongitude(-0.254287);
                    break;
                }
                case 2:
                {
                    //test1
                    mLastLocation.setLatitude(51.532148);
                    mLastLocation.setLongitude(-0.236808);
                    break;
                }
                case 3:
                {
                    //test1
                    mLastLocation.setLatitude(51.527919);
                    mLastLocation.setLongitude(-0.215445);
                    break;
                }
                case 4:
                {
                    //test1
                    mLastLocation.setLatitude(51.520064);
                    mLastLocation.setLongitude(-0.189712);
                    break;
                }
                case 5:
                {
                    //test1
                    mLastLocation.setLatitude(51.520064);
                    mLastLocation.setLongitude(-0.169319);
                    break;
                }
                case 6:
                {
                    //test1
                    mLastLocation.setLatitude(51.509791);
                    mLastLocation.setLongitude(-0.156696);
                    break;
                }
                case 7:
                {
                    //test1
                    mLastLocation.setLatitude(51.502689);
                    mLastLocation.setLongitude(-0.150141);
                    break;
                }
                case 8:
                {
                    //test1
                    mLastLocation.setLatitude(51.501934);
                    mLastLocation.setLongitude(-0.141159);
                    break;
                }
            }
            mLastLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mLastLocation.setTime(System.currentTimeMillis());
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, mLastLocation);
            mockCnt++;

            LatLng ll = Test_UpdateLocation();
            if(mapActivity != null && currentTrip != null)
                mapActivity.AddMarkerUI(ll, currentTrip.getNumMarkers() - 1);
            if(settings.getAppMode() == Settings.APP_MODE_AUTO)
                DelegGeofence.getInstance().locationUpdate(ll);
        }
    }

    public void Test_startTrip(String geo)
    {
        currentTrip = new Trip();
        currentTrip.setA2bMarkers(new ArrayList<A2BMarker>());
        currentTrip.SetTimeStart(new Date());
        Test_StartDurationTimer();
        //Test_StartMarkerTimer();
        LatLng ll = Test_UpdateLocation();

        if(mapActivity != null)
            mapActivity.setMapExt(ll);

        startA2bService();

        if(settings.getAppMode() == Settings.APP_MODE_AUTO)
            currentTrip.setStartGeo(geo);
    }

    //******************** testing END
    public void setMapActivity(Activity_newTrip activity){mapActivity = activity;}

/*

    public void StartTimers()
    {
        MarkerTask task = new MarkerTask();
        TripTimer = new Timer();
        int timeout = 1000 * 60 * settings.getMarkerTimeout();
        TripTimer.schedule(task, timeout, timeout);

        durationTimer = new Timer();
        durationTimer.schedule(new DurationTask(), 1000, 1000);
    }

    public void SetMapVisible(boolean visible)
    {
        mapVisible = visible;
        if(mGoogleApiClient != null)
            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public LatLng addMarker2CurrentTrip()
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

    @Override
    public void onConnected(Bundle connectionHint)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationReq(), this);
        if(mapActivity != null)
            map = mapActivity.getMap();
        if(map != null)
            setGeofences();
    }
    public void startTrip(String geo)
    {
        currentTrip = new Trip();
        currentTrip.setA2bMarkers(new ArrayList<A2BMarker>());
        currentTrip.SetTimeStart(new Date());
        StartTimers();

        LatLng ll = addMarker2CurrentTrip();

        if(mapVisible)
            mapActivity.setMapExt(ll);

        startA2bService();

        if(settings.getAppMode() == Settings.APP_MODE_AUTO)
            currentTrip.setStartGeo(geo);
    }

    public class MarkerTask extends TimerTask implements Runnable
    {
        @Override
        public void run()
        {
            LatLng ll = addMarker2CurrentTrip();
            if(mapVisible)
                mapActivity.AddMarkerUI(ll, currentTrip.getNumMarkers() - 1);
        }
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

            if(settings.getAppMode() == Settings.APP_MODE_AUTO)
                DelegGeofence.getInstance().locationUpdate(ll);
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

    public void startA2bService()
    {
        if(isServStarted == false)
        {
            isServStarted = true;
            serviceRef.startForeground(NOTIFICATION, createNotification());
        }
    }

    static public GoogleApiClient getGoogleApiClient(){return (mGoogleApiClient);}

    public void setGeofences()
    {
        if(mGoogleApiClient.isConnected() == true)
            DelegGeofence.getInstance().initGeofences(map);
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}

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
        if(TripTimer != null)
            TripTimer.cancel();
        if(durationTimer != null)
            durationTimer.cancel();
        if(mGoogleApiClient != null)
        {
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
*/