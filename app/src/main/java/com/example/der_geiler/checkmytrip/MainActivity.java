package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

// https://developers.google.com/android/reference/com/google/android/gms/location/Geofence.Builder
// https://developer.android.com/training/location/geofencing.html#HandleGeofenceTransitions

public class MainActivity extends Activity
{
    static final private String strNewTrip = "New trip";
    static final private String strCurrentTrip = "Current trip";

    private class uiAction
    {
        public int lastAction = 0;
        public View view;
    }

    private Rect touchRect = null;
    private uiAction lastUiAction = null;

    private void startActivity(String act)
    {
        switch (act)
        {
            case strNewTrip:
            case strCurrentTrip:    /* fall-through */
            {
                Intent i = new Intent(MainActivity.this, NewTripActivity.GetInstance().getClass());
                i.putExtra("class", MainActivity.class.getPackage().getName() + "." + this.getClass().getSimpleName());
                startActivity(i);
                break;
            }
            case "Trip groups":
            {
                Intent i = new Intent(MainActivity.this, TripGroupsActivity.class);
                startActivity(i);
                break;
            }
            default:
                break;
        }
    }

    private void setUiActions()
    {
        TableLayout tl = (TableLayout) findViewById(R.id.activityMainTableLayout);
        for(int i = 0; i <tl.getChildCount(); i++)
        {
            TextView tv = (TextView)((TableRow)tl.getChildAt(i)).getChildAt(0);
            tv.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    int action = event.getAction();
                    Log.d("action::", String.valueOf(action));
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
                                startActivity(((TextView) v).getText().toString());
                            }
                            break;
                        }
                        case MotionEvent.ACTION_MOVE:
                        {
                            v.getHitRect(touchRect);
                            if (!touchRect.contains((int) event.getX(), (int) event.getY()))
                            {
                                lastUiAction.view.setBackgroundColor(Color.parseColor("#b0b0b0"));
                                lastUiAction.lastAction = MotionEvent.ACTION_CANCEL;
                            }
                            break;
                        }
                    }
                    return true; //TODO: Problem is here.
                }
            });
        }
    }

    private void setNewTripText()
    {
        TextView tv = (TextView) findViewById(R.id.NewTripView);
        if(Globals.GetCurrentTrip() != null)
        {
            tv.setText(strCurrentTrip);
        }
        else
        {
            tv.setText(strNewTrip);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NewTripActivity.GetInstance();
        setNewTripText();
        FileHandler fileHandler = FileHandler.GetInstance();
        fileHandler.Init(getApplicationContext());
        touchRect = new Rect();
        lastUiAction = new uiAction();
        setUiActions();
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
        super.onResume();
        setNewTripText();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
