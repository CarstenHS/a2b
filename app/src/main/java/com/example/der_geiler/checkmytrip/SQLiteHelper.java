package com.example.der_geiler.checkmytrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.example.der_geiler.checkmytrip.TripsContract.*;

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

    private static final String TRIPS_TABLE_CREATE =
            "CREATE TABLE " + TripsTableEntry.TRIPS_TABLE_NAME + " (" +
                    TripsTableEntry._ID + " INTEGER PRIMARY KEY, " +
                    TripsTableEntry.COLUMN_NAME_TRIPS_ID + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_START_TIME + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_TOP_SPEED + INT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DISTANCE + INT_TYPE + COMMA_SEP +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TripsTableEntry.TRIPS_TABLE_NAME;

    public SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    public void insertTrip(String id, String startGeo, String endGeo,
                           int duration, float speed, float distance,
                            long timestampStart)
    {
        ContentValues values = new ContentValues();
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_ID, id);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_DURATION, duration);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_START_TIME, timestampStart);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_TOP_SPEED, speed);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO, startGeo);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO, endGeo);
        values.put(TripsTableEntry.COLUMN_NAME_TRIPS_DISTANCE, distance);

        long newRowId;
        newRowId = Globals.GetInstance(null).getDB().insert(
                TripsTableEntry.TRIPS_TABLE_NAME,
                null,
                values);
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
        String[] projection =
        {
            TripsTableEntry.COLUMN_NAME_TRIPS_ID,
            TripsTableEntry.COLUMN_NAME_TRIPS_DURATION
        };

        String selection =  TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO + strQST + strAND +
                            TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO + strQST;

        String[] selectionArgs =
        {
            startGeo,
            endGeo
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + " DESC";

        Cursor c = Globals.GetInstance(null).getDB().query(
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
