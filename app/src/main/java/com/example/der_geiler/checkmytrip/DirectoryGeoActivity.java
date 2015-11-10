package com.example.der_geiler.checkmytrip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by der_geiler on 07-11-2015.
 */
public class DirectoryGeoActivity extends Activity
{
    uiAction lastUiAction = null;
    Rect touchRect = null;
    private int bgColor = android.R.color.background_light;

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
        if (null != intent)
        {
            String dir  = intent.getStringExtra("dir");
            this.setTitle(dir);
        }
        LinearLayout lo = (LinearLayout) findViewById(R.id.dirGeoLayout);
        for(int i = 0; i<lo.getChildCount(); i++)
        {
            setTouchActions((TextView)lo.getChildAt(i));
        }
    }

    private void setDoubleView(View v, int color)
    {
        switch(v.getId())
        {
            case R.id.endPointSub:      /* Fall-through */
            case R.id.endPointSuper:
            {
                ((TextView)findViewById(R.id.endPointSub)).setBackgroundColor(color);
                ((TextView)findViewById(R.id.endPointSuper)).setBackgroundColor(color);
                break;
            }

            case R.id.startPointSuper:  /* Fall-through */
            case R.id.startPointSub:
            {
                ((TextView) findViewById(R.id.startPointSuper)).setBackgroundColor(color);
                ((TextView) findViewById(R.id.startPointSub)).setBackgroundColor(color);
                break;
            }
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
                Log.d("onTouch dirGeo: ", String.valueOf(action));
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
                            setDoubleView(lastUiAction.view, bgColor);
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
