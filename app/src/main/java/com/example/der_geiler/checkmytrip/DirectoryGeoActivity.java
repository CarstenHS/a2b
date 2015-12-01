package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by der_geiler on 07-11-2015.
 */
public class DirectoryGeoActivity extends Activity
{
    uiAction lastUiAction = null;
    Rect touchRect = null;
    private int bgColor = android.R.color.background_light;
    static final String END_GEO = "End point";
    static final String START_GEO = "Start point";
    private String currentDir = null;

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
        touchRect = new Rect();
        Intent intent = getIntent();
        lastUiAction = new uiAction();

        String dir  = intent.getStringExtra("dir");
        this.setTitle(dir);
        currentDir = dir;

        A2BdirInfo di = Globals.GetInstance(null).getDir(dir);
        if(di != null)
        {
            ((TextView) findViewById(R.id.endPointSub)).setText(di.getGeofenceEnd());
            ((TextView) findViewById(R.id.startPointSub)).setText(di.getGeofenceStart());
        }

        LinearLayout lo = (LinearLayout) findViewById(R.id.dirGeoLayout);
        for(int i = 0; i<lo.getChildCount(); i++)
        {
            setTouchActions((TextView)lo.getChildAt(i));
        }
    }

    private String setDoubleView(View v, int color)
    {
        String ret = null;
        switch(v.getId())
        {
            case R.id.endPointSub:      /* Fall-through */
            case R.id.endPointSuper:
            {
                ((TextView)findViewById(R.id.endPointSub)).setBackgroundColor(color);
                ((TextView)findViewById(R.id.endPointSuper)).setBackgroundColor(color);
                ret = END_GEO;
                break;
            }

            case R.id.startPointSuper:  /* Fall-through */
            case R.id.startPointSub:
            {
                ((TextView) findViewById(R.id.startPointSuper)).setBackgroundColor(color);
                ((TextView) findViewById(R.id.startPointSub)).setBackgroundColor(color);
                ret = START_GEO;
                break;
            }
        }
        return ret;
    }

    private void selectGeofence(final String point)
    {
        List<A2BGeofence> geofences =  Globals.GetInstance(null).getGeoFencesPersist();
        if(geofences != null)
        {
            List<String> geoNames = new ArrayList<>();
            for (A2BGeofence gf : geofences)
                geoNames.add(gf.getName());

            CharSequence[] cs = geoNames.toArray(new CharSequence[geoNames.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set " + point);
            builder.setItems(cs, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    TextView tv;
                    Globals globalsInstance = Globals.GetInstance(null);
                    String selectedGeofence = ((AlertDialog) dialog).getListView().getItemAtPosition(which).toString();
                    A2BdirInfo di = globalsInstance.getDir(currentDir);
                    if (di == null)
                        di = new A2BdirInfo(currentDir);

                    if (point.equals(END_GEO))
                    {
                        tv = (TextView) findViewById(R.id.endPointSub);
                        di.setGeofenceEnd(selectedGeofence);
                    }
                    else
                    {
                        tv = (TextView) findViewById(R.id.startPointSub);
                        di.setGeofenceStart(selectedGeofence);
                    }
                    globalsInstance.setDir(di);
                    tv.setText(selectedGeofence);
                }
            });
            builder.show();
        }
    }

    private void setTouchActions(TextView tv)
    {
        tv.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getAction();
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        lastUiAction.lastAction = action;
                        lastUiAction.view = v;
                        setDoubleView(v, Color.BLACK);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    {
                        lastUiAction.lastAction = action;
                        setDoubleView(v, bgColor);
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        if (lastUiAction.lastAction == MotionEvent.ACTION_DOWN)
                        {
                            String point = setDoubleView(lastUiAction.view, bgColor);
                            selectGeofence(point);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
                        v.getHitRect(touchRect);
                        if (!touchRect.contains((int) event.getX(), (int) event.getY()))
                        {
                            setDoubleView(lastUiAction.view, bgColor);
                            v.cancelLongPress();
                            lastUiAction.lastAction = MotionEvent.ACTION_CANCEL;
                        }
                    }
                }
                return true;
            }
        });
    }
}
