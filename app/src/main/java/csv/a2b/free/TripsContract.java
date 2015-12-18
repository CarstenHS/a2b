package csv.a2b.free;

import android.provider.BaseColumns;

/**
 * Created by der_geiler on 19-11-2015.
 */
public final class TripsContract
{
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public TripsContract() {}

    /* Inner class that defines the table contents */
    public static abstract class TripsTableEntry implements BaseColumns
    {
        public static final String TRIPS_TABLE_NAME             = "tripsTable";
        public static final String COLUMN_NAME_TRIPS_ID         = "id";
        public static final String COLUMN_NAME_TRIPS_DURATION   = "duration";
        public static final String COLUMN_NAME_TRIPS_DIRECTORY  = "directory";
        public static final String COLUMN_NAME_TRIPS_START_GEO  = "startGeo";
        public static final String COLUMN_NAME_TRIPS_END_GEO    = "endGeo";
        public static final String COLUMN_NAME_TRIPS_TOP_SPEED  = "topSpeed";
        public static final String COLUMN_NAME_TRIPS_DISTANCE   = "distance";
        public static final String COLUMN_NAME_TRIPS_TRIP_TITLE = "title";
    }
}
