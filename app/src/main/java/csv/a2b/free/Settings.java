package csv.a2b.free;

/**
 * Created by der_geiler on 04-12-2015.
 */
public class Settings
{
    private int speedUnit;
    private int markerTimeoutMins;
    private int appMode;
    public static final int SPEED_UNIT_MS       = 0;
    public static final int SPEED_UNIT_MPH      = 1;
    public static final int SPEED_UNIT_KPH      = 2;
    public static final int APP_MODE_MANUAL     = 0;
    public static final int APP_MODE_AUTO       = 1;

    public Settings()
    {
        speedUnit = SPEED_UNIT_KPH;
        markerTimeoutMins = 2;
        appMode = APP_MODE_MANUAL;
    }

    public int getSpeedUnit(){return speedUnit;}
    public int getMarkerTimeout(){return markerTimeoutMins;}
    public int getAppMode(){return appMode;}
    public void setSpeedUnit(int unit){speedUnit = unit;}
    public void setMarkerTimeout(int timeout){markerTimeoutMins = timeout;}
    public void setAppMode(int mode){appMode = mode;}
}
