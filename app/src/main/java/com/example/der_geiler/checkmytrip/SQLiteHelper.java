package com.example.der_geiler.checkmytrip;

import android.content.Context;
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
    private static final String COMMA_SEP = ",";

    private static final String TRIPS_TABLE_CREATE =
            "CREATE TABLE " + TripsTableEntry.TRIPS_TABLE_NAME + " (" +
                    TripsTableEntry._ID + " INTEGER PRIMARY KEY, " +
                    TripsTableEntry.COLUMN_NAME_TRIPS_ID + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_DURATION + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_START_TIME + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_TOP_SPEED + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_START_GEO + TEXT_TYPE + COMMA_SEP +
                    TripsTableEntry.COLUMN_NAME_TRIPS_END_GEO + TEXT_TYPE + COMMA_SEP +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TripsTableEntry.TRIPS_TABLE_NAME;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRIPS_TABLE_CREATE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
