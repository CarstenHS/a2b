package csv.a2b;

/**
 * Created by der_geiler on 06-11-2015.
 */
public class A2BdirInfo
{
    public A2BdirInfo(){}
    public A2BdirInfo(String dir)
    {
        this.dir = dir;
        this.geofenceStart = "Not set";
        this.geofenceStop = "Not set";
    }
    private String dir;
    private String geofenceStart;
    private String geofenceStop;
    public void setGeofenceStart(String start){geofenceStart = start;}
    public void setGeofenceEnd(String end){geofenceStop = end;}
    public void setDir(String dir){this.dir = dir;}
    public String getGeofenceStart(){return geofenceStart;}
    public String getGeofenceEnd(){return geofenceStop;}
    public String getDir(){return dir;}
}
