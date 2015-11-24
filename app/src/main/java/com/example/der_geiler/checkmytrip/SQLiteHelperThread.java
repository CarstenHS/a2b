package com.example.der_geiler.checkmytrip;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Looper;

import java.util.List;

/**
 * Created by der_geiler on 20-11-2015.
 */
public class SQLiteHelperThread extends AsyncTask<Object, Object, Object>
{
    static final int ACTION_INSERT = 0;
    static final int ACTION_SELECT = 1;
    static final int ACTION_DELETE = 2;

    onDBCursorReadyCallback cb = null;
    Cursor c = null;

    public SQLiteHelperThread(){}

    protected Object doInBackground(Object... objs)
    {
        Thread t = Looper.getMainLooper().getThread();
        switch((int) objs[0])
        {
            case ACTION_INSERT:
            {
                Globals.GetInstance(null).getDbHelper().insertTrip((Trip) objs[1]);
                break;
            }
            case ACTION_SELECT:
            {
                cb = (onDBCursorReadyCallback) objs[1];
                c = Globals.GetInstance(null).getDbHelper().select(null, null);
                cb.onDBCursorReady(c);
                break;
            }
            default: break;
        }
        return c;
    }

    protected void onPostExecute(Long result)
    {
        cb.onDBCursorReady(c);
    }
}