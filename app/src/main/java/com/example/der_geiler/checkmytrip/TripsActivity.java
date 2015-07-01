package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by der_geiler on 03-06-2015.
 */
public class TripsActivity extends Activity
{
    private Context context;
    private TableLayout tripsTableLayout = null;
    private FileHandler fileHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_groups);
        context = getApplicationContext();
        tripsTableLayout = (TableLayout) findViewById(R.id.tripsTableLayout);
        fileHandler = FileHandler.GetInstance();
        Intent intent = getIntent();
        if (null != intent)
        {
            ShowTrips(intent.getStringExtra("group"));
        }
    }

    private int Dp2Px(float dp)
    {
        return (int)(context.getResources().getDisplayMetrics().density * dp);
    }

    private float Px2Dp(float px)
    {
        return px / context.getResources().getDisplayMetrics().density;
    }

    private void ShowTrips(String group)
    {
        setContentView(R.layout.trips);
        tripsTableLayout = (TableLayout) findViewById(R.id.tripsTableLayout);
        List<String> trips = new ArrayList<String>();
        if (tripsTableLayout.getChildCount() != 0)
        {
            tripsTableLayout.removeAllViewsInLayout();
        }
        trips = fileHandler.LoadTrips(group);
        if (trips.size() != 0)
        {
            for (String trip : trips)
            {
                Context context = getApplicationContext();

                TableRow tr = new TableRow(context);
                tr.setGravity(Gravity.CENTER_HORIZONTAL);
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                tr.setLayoutParams(params);

                TextView tv = new TextView(context);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                int px = Dp2Px(18);
                tv.setPadding(px, px, px, px);
                tv.setText(trip);
                tv.setTextColor(getResources().getColor(android.R.color.black));
                tv.setBackgroundColor(Color.parseColor("#b0b0b0"));
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                px = Dp2Px(1);
                params.setMargins(px, px, px, px);
                tv.setLayoutParams(params);

                tr.addView(tv);
                tripsTableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }
}