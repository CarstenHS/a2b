package csv.a2b;

import android.database.Cursor;
import android.os.AsyncTask;

/**
 * Created by der_geiler on 20-11-2015.
 */
public class SQLiteHelperThread extends AsyncTask<Object, Object, Object>
{
    static final int ACTION_INSERT = 0;
    static final int ACTION_SELECT = 1;
    static final int ACTION_DELETE_DIR = 2;
    static final int ACTION_DELETE_TRIP = 3;
    static final int ACTION_UPDATE = 4;

    public SQLiteHelperThread(){}

    protected Object doInBackground(Object... objs)
    {
        switch((int) objs[0])
        {
            case ACTION_INSERT:
            {
                Globals.GetInstance(null).getDbHelper().insertTrip((Trip)objs[1], (String)objs[2]);
                ((OnDBInsertDoneCallback)objs[3]).onDBInsertDone();
                break;
            }
            case ACTION_SELECT:
            {
                Cursor c = Globals.GetInstance(null).getDbHelper().select((String)objs[2]);
                ((onDBCursorReadyCallback) objs[1]).onDBCursorReady(c);
                break;
            }
            case ACTION_DELETE_DIR: Globals.GetInstance(null).getDbHelper().deleteDir((String)objs[1]); break;
            case ACTION_DELETE_TRIP:
                Globals.GetInstance(null).getDbHelper().deleteTrip((String)objs[1]);break;
            case ACTION_UPDATE: Globals.GetInstance(null).getDbHelper().updateDir((String) objs[1], (String) objs[2]); break;
            default: break;
        }
        return null; // not used
    }
}
