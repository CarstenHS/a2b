package com.example.der_geiler.checkmytrip;

/**
 * Created by der_geiler on 11-04-2015.
*/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewTripActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener
{
    public GoogleMap map;
    int MAX_ZOOM = 15;
    public static final int UI_ELEMENT_SPEED = 0;
    public static final int UI_ELEMENT_DISTANCE = 1;
    private Globals globals = null;
    private boolean removeGeoCircle = true;

    public void SetMap(LatLng ll)
    {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        AddMarkerToMap(ll, 0);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, MAX_ZOOM);
        map.animateCamera(cameraUpdate);
    }

    public GoogleMap getMap(){return map;}

    private void EndTrip()
    {
        Trip currentTrip = globals.GetCurrentTrip();
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
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, MAX_ZOOM);
                map.animateCamera(cameraUpdate);
                //UpdateBounds();
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
                    .snippet(fmt.format(currentTrip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            marker.showInfoWindow();
        }
        else if(num == currentTrip.A2BMarkers.size() - 1)
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("End")
                    .snippet(fmt.format(currentTrip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            marker.showInfoWindow();
        }
        else
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title(fmt.format(currentTrip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    public void onMapReady(GoogleMap map)
    {
        this.map = map;
        this.map.setOnMapLongClickListener(this);
        this.map.setOnMapClickListener(this);
        globals.setMap(map);
        int i = 0;
        List<LatLng> lls = globals.GetLatLngs();
        for(LatLng ll : lls)
        {
            AddMarkerUI(ll, i);
            ++i;
        }
    }

    private void InitMap()
    {
        globals.SetMapVisible(this);
        Trip currentTrip = globals.GetCurrentTrip();
        if(currentTrip == null)
        {
            currentTrip = new Trip();
            globals.buildGoogleApiClient();
            currentTrip.A2BMarkers = new ArrayList<A2BMarker>();
            currentTrip.SetTimeStart(new Date());
            Date d = new Date();
            globals.setCurrentTrip(currentTrip);
        }
        else
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_trip);
        globals = ((Globals)this.getApplication());
        InitMap();
    }

    @Override
    protected void onPause()
    {
        globals.SetMapVisible(null);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        FileHandler fileHandler = FileHandler.GetInstance();
        try
        {
            fileHandler.SaveTrip(globals.GetCurrentTrip());
            System.exit(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

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
            public void onClick(DialogInterface dialog, int which){}
        });
        alert.show();
    }

    @Override
    public void onMapLongClick(final LatLng latLng)
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

    private void ShowDialogGeofenceKill(final A2BGeofence gf)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete Start or End point \"" + gf.getName() + "\"?");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which){globals.getCircle(gf.getName()).setFillColor(Globals.COLOR_BASIC_GEOFENCE);}
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
        A2BGeofence hitGeofence = Globals.anyGeofenceHit(latLng);
        if(hitGeofence != null)
        {
            globals.getCircle(hitGeofence.getName()).setFillColor(Color.RED);
            ShowDialogGeofenceKill(hitGeofence);
        }
    }

    /*
    public void UpdateBounds()
    {
        Trip currentTrip = ((Globals)this.getApplication()).GetCurrentTrip();

        CameraUpdate cameraUpdate;
        if(currentTrip.A2BMarkers.size() > 1)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (A2BMarker marker : currentTrip.A2BMarkers)
            {
                builder.include(marker.ll);
            }

            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        }
        else
        {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentTrip.A2BMarkers.get(0).ll, MAX_ZOOM);
        }
        map.animateCamera(cameraUpdate);
    }
*/
}