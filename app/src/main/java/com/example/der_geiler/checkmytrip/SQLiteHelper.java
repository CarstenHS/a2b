package com.example.der_geiler.checkmytrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.der_geiler.checkmytrip.TripsContract.*;

import java.io.File;

/**
 * Created by der_geiler on 19-11-2015.
 */
public class SQLiteHelper extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Trips.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String strAND = "AND";
    private static final String strQST = "=?";
    private static final String strGreaterThan = ">?";

    private static final String TRIPS_TABLE_CREATE =
            "CREATE TABLE " + TripsTableEntry.TRIPS_TABLE_NAME + " (" +
                    TripsTableEntry._ID + " INTEGER PRIMARY KEY, " +
                    TripsTableEntry.COLUMN_NAME_TRIPS_ID + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_TOP_SPEED + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DIRECTORY + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DISTANCE + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TripsTableEntry.TRIPS_TABLE_NAME;

    public SQLiteHelper(Context context)
    {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory()
                + File.separator+ DATABASE_NAME,null);
        int i = 0;
    }
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TRIPS_TABLE_CREATE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertTrip(Trip ct)
    {
        Log.d("CHS", "wtfwtfwtfwtfwtfwtfw");
        Log.d("CHS", ct.toString());
        ContentValues values = new ContentValues();
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_ID, ct.getStartTimestamp());
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_DURATION, ct.getTicks());
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_TOP_SPEED, ct.getTopSpeed());
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO, ct.getStartGeo());
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO, ct.getEndGeo());
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_DISTANCE, ct.getDistance());

        long newRowId;
        newRowId = Globals.GetInstance(null).getWritableDB().insert(
                TripsTableEntry.TRIPS_TABLE_NAME,
                null,
                values);
        Log.d("CHS", "Row ID: " + String.valueOf(newRowId));
    }

    public Cursor select(String startGeo, String endGeo)
    {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.

        /*
        Example on iterating through gotten selection
        cursor.moveToFirst();
        long itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(FeedEntry._ID)
        );
        */
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] projection =
        {
            TripsTableEntry.COLUMN_NAME_TRIPS_ID,
            TripsTableEntry.COLUMN_NAME_TRIPS_DURATION
        };

        /*
        String selection =  TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO + strQST + strAND +
                            TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO + strQST;
*/
        // String selection =  "*";  WHERE *

        String selection = TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + strGreaterThan;
/*
        String[] selectionArgs =
        {
            "0",
            endGeo
        };
*/
        String[] selectionArgs =
                {
                        "0"
                };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + " ASC";

        //qb.buildQuery(projection, selection, null, null, sortOrder, null);

        /*
        return rawQueryWithFactory(cursorFactory, sql, selectionArgs,
                findEditTable(table), cancellationSignal);
*/

        Cursor c = Globals.GetInstance(null).getReadableDB().query(
                TripsTableEntry.TRIPS_TABLE_NAME,       // The table to query
                projection,                             // The columns to return
                selection,                              // The columns for the WHERE clause
                selectionArgs,                          // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                sortOrder                               // The sort order
        );

        return c;
    }
/*
    public void delete()
    {
        // Define 'where' part of query.
        String selection = FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) };
        // Issue SQL statement.
        Globals.GetInstance(null).getDB().delete(table_name, selection, selectionArgs);
    }

    public void update()
    {
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);

        // Which row to update, based on the ID
        String selection = FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
                FeedReaderDbHelper.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
    */
}
