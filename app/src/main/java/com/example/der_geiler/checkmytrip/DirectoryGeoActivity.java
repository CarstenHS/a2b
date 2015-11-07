package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;

/**
 * Created by der_geiler on 07-11-2015.
 */
public class DirectoryGeoActivity extends Activity
{
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
        super.onResume();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_geo);
        //TableLayout = (TableLayout) findViewById(R.id.tripGroupsTableLayout);
    }
}
