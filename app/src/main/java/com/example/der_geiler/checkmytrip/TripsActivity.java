package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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
    uiAction lastUiAction = null;
    Rect touchRect = null;
    final CharSequence[] groupProps = {"Delete", "noget1", "noget2"};
    String selectedGroup;
    String selectedTrip;

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
            selectedGroup = intent.getStringExtra("group");
            ShowTrips(selectedGroup);
        }
        lastUiAction = new uiAction();
        touchRect = new Rect();
    }

    private int Dp2Px(float dp)
    {
        return (int)(context.getResources().getDisplayMetrics().density * dp);
    }

    private float Px2Dp(float px)
    {
        return px / context.getResources().getDisplayMetrics().density;
    }

    private void SetDeleteAlert(final String group)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete: " + group + "? (All tripGroups will be deleted!");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //input.setText("canceled");
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //TODO: Delete trip instead of: fileHandler.DeleteGroup(group);
                fileHandler.deleteTrip(selectedGroup, selectedTrip);
                ShowTrips(selectedGroup);
            }
        });
        alert.show();
    }

    private void setTouchActions(TextView tv, String grp)
    {
        final String group = grp;
        tv.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                selectedTrip = ((TextView) v).getText().toString();

                int action = event.getAction();
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        lastUiAction.lastAction = action;
                        lastUiAction.view = v;
                        v.setBackgroundColor(Color.parseColor("#000000"));
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    {
                        lastUiAction.lastAction = action;
                        lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        if (lastUiAction.lastAction == MotionEvent.ACTION_DOWN)
                        {
                            lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));

                            Intent i = new Intent(getApplicationContext(), StaticMapActivity.class);
                            i.putExtra("trip", selectedTrip);
                            i.putExtra("group", selectedGroup);
                            startActivity(i);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
                        v.getHitRect(touchRect);
                        if (!touchRect.contains((int) event.getX(), (int) event.getY()))
                        {
                            lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                            v.cancelLongPress();
                            lastUiAction.lastAction = MotionEvent.ACTION_CANCEL;
                        }

                    }
                }
                return false;
            }
        });
        tv.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(TripsActivity.this);
                final String group = ((TextView) v).getText().toString();
                builder.setTitle(group);
                lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                lastUiAction.lastAction = uiAction.ACTION_LONG_CSV;
                builder.setItems(groupProps, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                    SetDeleteAlert(group);
                                else
                                    which = 2;
                                // The 'which' argument contains the index position
                                // of the selected item
                            }
                        }
                );
                AlertDialog groupPropDialog = builder.create();
                groupPropDialog.show();
                return true;
            }
        });
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

                setTouchActions(tv, group);

                tr.addView(tv);
                tripsTableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }
}