package csv.a2b.free;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.io.IOException;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService
{
    public GeofenceTransitionsIntentService (){super("GeofenceTransitionsIntentService");};

    protected void onHandleIntent(Intent intent)
    {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){return;}

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            Globals g = Globals.GetInstance(null);
            Trip ct = g.GetCurrentTrip();

            if(g.getGeofenceActivated())
            {
                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                String[] triggerIds = new String[triggeringGeofences.size()];

                for (int i = 0; i < triggerIds.length; i++)
                    triggerIds[i] = ((Geofence) triggeringGeofences.get(i)).getRequestId();

                if (triggerIds.length != 0)
                {
                    if (ct != null && geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    {
                        try
                        {
                            if(ct.getStartGeo().equals(triggerIds[0]) == false) // no re-entrent
                            {
                                if (triggerIds[0].length() != 0)
                                    ct.setEndGeo(triggerIds[0]);

                                FileHandler fh = FileHandler.GetInstance();
                                g.setEndTimestamp();
                                List dirsToSaveIn = g.resolveGeoDir(triggerIds[0]);
                                FileHandler.GetInstance().SaveTrip(dirsToSaveIn, ct);
                                String saveDir = null;
                                if (dirsToSaveIn.size() == 0)
                                {
                                    saveDir = fh.getUncategorizedString();
                                    g.setInsertCount(1);
                                    g.insertInDB(ct, saveDir);
                                } else
                                {
                                    g.setInsertCount(dirsToSaveIn.size());
                                    for (String s : (List<String>) dirsToSaveIn)
                                        g.insertInDB(ct, s); // this results in end on callback
                                }
                            }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                    {
                        if(g.getSettings().getAppMode() == Settings.APP_MODE_AUTO)
                        {
                            g.startTrip();
                            g.GetCurrentTrip().setStartGeo(triggerIds[0]);
                        }
                    }
                }
            }
        }
    }
}
