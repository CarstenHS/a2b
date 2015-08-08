package com.example.der_geiler.checkmytrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by der_geiler on 08-08-2015.
 */
public class StaticMapActivity extends FragmentActivity implements OnMapReadyCallback
{
    public GoogleMap map;
    int MAX_ZOOM = 15;
    private Trip trip;

    public void AddMarkerToMap(LatLng ll, int num)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        if((num == 0))
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("Start")
                    .snippet(fmt.format(trip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            marker.showInfoWindow();
        }
        else if(num == trip.A2BMarkers.size() - 1)
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("End")
                    .snippet(fmt.format(trip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            marker.showInfoWindow();
        }
        else
        {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .title(fmt.format(trip.A2BMarkers.get(num).date))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    public void UpdateBounds()
    {
        CameraUpdate cameraUpdate;
        List<A2BMarker> a2bMarkers = trip.getA2bMarkers();
        if(a2bMarkers.size() > 1)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (A2BMarker marker : a2bMarkers)
            {
                builder.include(new LatLng(marker.GetLat(), marker.GetLon()));
            }

            LatLngBounds bounds = builder.build();
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        }
        else
        {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(a2bMarkers.get(0).GetLat(), a2bMarkers.get(0).GetLon()), MAX_ZOOM);
        }
        try
        {
            map.moveCamera(cameraUpdate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onMapReady(GoogleMap map)
    {
        this.map = map;
        UpdateBounds();
        List<A2BMarker> A2BMarkers = trip.getA2bMarkers();

        String dist = trip.GetDistance();
        ((TextView) findViewById(R.id.distance)).setText(dist);

        String duration = ((Globals)this.getApplication()).ExtractDurationFromTicks(trip.getTicks());
        ((TextView) findViewById(R.id.duration)).setText(duration);

        int i = 0;
        for(A2BMarker mkr : A2BMarkers)
        {
            LatLng ll = new LatLng(mkr.GetLat(), mkr.GetLon());
            AddMarkerToMap(ll, i);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, MAX_ZOOM);
            map.animateCamera(cameraUpdate);
            ++i;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_trip);

        FileHandler fileHandler;

        fileHandler = FileHandler.GetInstance();
        Intent intent = getIntent();
        if (null != intent)
        {
            String selectedGroup;
            String selectedTrip;
            selectedGroup = intent.getStringExtra("group");
            selectedTrip = intent.getStringExtra("trip");
            trip = fileHandler.LoadTrip(selectedGroup, selectedTrip);

        }

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

/*
        Intent i = new Intent(getApplicationContext(), TripsActivity.class);
        i.putExtra("group", pressedGroup);
        startActivity(i);
                */
    }
}
