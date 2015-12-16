package csv.a2b;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

// https://developers.google.com/android/reference/com/google/android/gms/location/Geofence.Builder
// https://developer.android.com/training/location/geofencing.html#HandleGeofenceTransitions

public class Activity_main extends Activity
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

    private void startActivity(int id)
    {
        Intent i = null;
        switch (id)
        {
            case R.id.textView_newTrip:
            {
                i = new Intent(Activity_main.this, Activity_newTrip.GetInstance().getClass());
                i.putExtra("class", Activity_main.class.getPackage().getName() + "." + this.getClass().getSimpleName());
                break;
            }
            case R.id.textView_tripGroups:
            {
                i = new Intent(Activity_main.this, Activity_tripGroups.class);
                break;
            }
            case R.id.textView_about:
            {
                i = new Intent(Activity_main.this, Activity_about.class);
                break;
            }
            case R.id.textView_help:
            {
                i = new Intent(Activity_main.this, Activity_help.class);
                break;
            }
            case R.id.textView_settings:
            {
                i = new Intent(Activity_main.this, Activity_settings.class);
                break;
            }
            default:
                break;
        }
        startActivity(i);
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
                                startActivity(((TextView) v).getId());
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
        TextView tv = (TextView) findViewById(R.id.textView_newTrip);
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

        new SimpleEula(this).show();

        FileHandler fileHandler = FileHandler.GetInstance();
        fileHandler.Init(getApplicationContext());
        /*
        fileHandler.listFiles();
        String fuck = fileHandler.readStackFile();
        */
        Globals.GetInstance(getApplicationContext());
        Activity_newTrip.GetInstance();
        setNewTripText();
        touchRect = new Rect();
        lastUiAction = new uiAction();
        setUiActions();
    }

    @Override
    protected void onDestroy()
    {
        Globals g = Globals.GetInstance(null);
        if(g.GetCurrentTrip() == null)
            g.cleanUp();
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
}
