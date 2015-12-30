package csv.a2b; /**
 * Created by der_geiler on 11-04-2015.
*/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Activity_newTrip extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener
{
    public GoogleMap map;
    int MAX_ZOOM = 15;
    public static final int UI_ELEMENT_SPEED = 0;
    public static final int UI_ELEMENT_DISTANCE = 1;
    private Globals globals = null;
    private boolean removeGeoCircle = true;
    static final int MENU_ITEM_ID_START = 0;
    static final int MENU_ITEM_ID_SAVE_AND_END = 1;
    static final int MENU_ITEM_ID_CREATE_START_END_POINT = 2;
    static final int MENU_ITEM_ID_END_SESSION = 3;

    /***** SINGLETON ********/
    public Activity_newTrip(){}
    private static Activity_newTrip instance;
    public static Activity_newTrip GetInstance()
    {
        instance = (instance == null) ? new Activity_newTrip() : instance;
        return instance;
    }
    /***** SINGLETON END *****/

    public void setMapExt(final LatLng ll)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AddMarkerToMap(ll, 0);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, MAX_ZOOM);
                map.animateCamera(cameraUpdate);
            }
        });
    }

    public GoogleMap getMap(){return map;}

    public void zoomToPosition(LatLng ll)
    {
        if(map != null)
        {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, MAX_ZOOM);
            map.animateCamera(cameraUpdate);
        }
    }

    public void UpdateUIElement(final int element, final String text)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                switch(element)
                {
                    case UI_ELEMENT_SPEED:
                        ((TextView) findViewById(R.id.speed)).setText(text);
                        break;
                    case UI_ELEMENT_DISTANCE:
                        ((TextView) findViewById(R.id.distance)).setText(text);
                        break;
                }
            }
        });
    }

    public void AddMarkerUI(final LatLng ll, final int num)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AddMarkerToMap(ll, num);
                zoomToPosition(ll);
            }
        });
    }

    public void TickUI(final int ticks)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TextView tv = (TextView) findViewById(R.id.duration);
                tv.setText(globals.ExtractDurationFromTicks(ticks));
            }
        });
    }

    public void AddMarkerToMap(LatLng ll, int num)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        Trip currentTrip = globals.GetCurrentTrip();
        if((num == 0))
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("Start")
                    .snippet(fmt.format(currentTrip.getMarker(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            marker.showInfoWindow();
        }
        else
        {
             map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title(fmt.format(currentTrip.getMarker(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    public void onMapReady(GoogleMap map)
    {
        Globals g = Globals.GetInstance(null);
        if(this.map == null)
        {
            this.map = map;
            this.map.setOnMapLongClickListener(this);
            this.map.setOnMapClickListener(this);
            globals.setMapActivity(this);
            globals.setMap(map);
            map.setMyLocationEnabled(true);
            Globals.GetInstance(this.getApplicationContext()).setGeofences();
        }
        Trip curTrip = g.GetCurrentTrip();
        if(curTrip != null)
        {
            int i = 0;
            List<LatLng> lls =curTrip.getLatLngs();
            if (lls != null)
            {
                for (LatLng ll : lls)
                {
                    AddMarkerUI(ll, i);
                    ++i;
                }
            }
        }
        if(g.getSettings().getAppMode() == Settings.APP_MODE_AUTO)
            g.startA2bService();
    }

    private void InitMap()
    {
        globals.SetMapVisible(true);
        if(globals.getGoogleApiClient() == null)
            globals.buildGoogleApiClient();
        else
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    private void setInitUnitText()
    {
        TextView tvSpeed = (TextView) findViewById(R.id.speed);
        TextView tvDist = (TextView) findViewById(R.id.distance);
        switch(globals.getSettings().getSpeedUnit())
        {
            case Settings.SPEED_UNIT_MS:
            {
                tvSpeed.setText("0m/s");
                tvDist.setText("0m");
                break;
            }
            case Settings.SPEED_UNIT_MPH:
            {
                tvSpeed.setText("0mph");
                tvDist.setText("0 yards");
                break;
            }
            case Settings.SPEED_UNIT_KPH:
            {
                tvSpeed.setText("0km/h");
                tvDist.setText("0m");
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_trip);

        if(BuildConfig.FLAVOR.equals("free"))
        {
            AdView mAdView = (AdView) findViewById(R.id.adViewNewTrip);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        findViewById(R.id.tripPropsTableLayout).bringToFront();

        globals = Globals.GetInstance(this.getApplicationContext());
        setInitUnitText();
        InitMap();
    }

    @Override
    protected void onPause()
    {
        globals.SetMapVisible(false);
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        Globals g = Globals.GetInstance(null);
        if(g.getSettings().getAppMode() == Settings.APP_MODE_MANUAL && g.GetCurrentTrip() == null)
            menu.add(Menu.NONE, MENU_ITEM_ID_START, Menu.NONE, R.string.start_trip);
        if(g.GetCurrentTrip() != null)
            menu.add(Menu.NONE, MENU_ITEM_ID_SAVE_AND_END, Menu.NONE, R.string.end_trip);
        else if(g.getSettings().getAppMode() == Settings.APP_MODE_AUTO)
            menu.add(Menu.NONE, MENU_ITEM_ID_END_SESSION, Menu.NONE, R.string.end_session);

        if(globals.getIsUsingGeofences())
            menu.add(Menu.NONE, MENU_ITEM_ID_CREATE_START_END_POINT, Menu.NONE, R.string.create_start_end);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        FileHandler fileHandler = FileHandler.GetInstance();
        switch(id)
        {
            case MENU_ITEM_ID_SAVE_AND_END:
            {
                try
                {
                    globals.setEndTimestamp();
                    Trip ct = globals.GetCurrentTrip();
                    fileHandler.SaveTrip(null, ct);
                    globals.setInsertCount(1);
                    globals.insertInDB(ct, fileHandler.getUncategorizedString());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return true;
            }
            case MENU_ITEM_ID_CREATE_START_END_POINT:
            {
                onMapLongClick(globals.getLastLoc().getLatlng());
                break;
            }
            case MENU_ITEM_ID_START:
            {
                globals.startTrip();
                break;
            }
            case MENU_ITEM_ID_END_SESSION:
            {
                globals.cleanUp();
                try
                {
                    Activity_newTrip.GetInstance().finalize();
                } catch (Throwable throwable)
                {
                    throwable.printStackTrace();
                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        InitMap();
        super.onResume();
    }

    void showDialogSaveFail()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Name already exist or is empty please choose another.");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        alert.show();
    }

    @Override
    public void onMapLongClick(final LatLng latLng)
    {
        if(globals.getIsUsingGeofences())
        {
            removeGeoCircle = true;
            final Circle circle = globals.createAndDrawGeofenceCircle(latLng.latitude, latLng.longitude);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Create and name Start or End point at this location:");
            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    if (removeGeoCircle == true)
                        circle.remove();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    circle.remove();
                }
            });
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String name = input.getText().toString();
                    if (globals.nameOk(name) != Globals.RES_OK)
                        showDialogSaveFail();
                    else
                    {
                        removeGeoCircle = false;
                        globals.addCircle(new A2BCircle(circle, name));
                        globals.saveGeofence(new A2BGeofence(latLng.latitude, latLng.longitude, name));
                    }
                }
            });
            alert.show();
        }
    }

    private void ShowDialogGeofenceKill(final A2BGeofence gf)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Start or End point \"" + gf.getName() + "\"?");
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which){
                globals.getCircle(gf.getName()).setFillColor(Globals.COLOR_BASIC_GEOFENCE);}
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                globals.removeGeofence(gf.getName());
            }
        });
        alert.show();
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if(globals.getIsUsingGeofences())
        {
            A2BGeofence hitGeofence = Globals.anyGeofenceHit(latLng);
            if (hitGeofence != null)
            {
                globals.getCircle(hitGeofence.getName()).setFillColor(Color.RED);
                ShowDialogGeofenceKill(hitGeofence);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_HOME)
            try
            {
                finalize();
            } catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }
        return super.onKeyDown(keyCode, event);
    }
}