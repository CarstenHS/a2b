package csv.a2b;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import java.util.List;

public class Activity_settings extends Activity implements AdapterView.OnItemSelectedListener
{
    private static final String strAuto = "Auto";
    private static final String strManual = "Manual";
    private static final String[] itemsSpeedUnit    = {"m/s","mph","kph"};
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_settings);

        Globals g = Globals.GetInstance(null);
        Settings settings = g.getSettings();
        List<String> itemsAppMode = new ArrayList<String>();
        Spinner spinner = (Spinner) findViewById(R.id.tv_settings_AppMode_spinner);
        spinner.setAdapter(null);

        itemsAppMode.add(strManual);
        if(g.getGeoFencesPersist().size() > 1)
            itemsAppMode.add(strAuto);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsAppMode);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getAppMode());
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.tv_settings_unitSpeed_spinner);
        spinner.setAdapter(null);
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsSpeedUnit);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getSpeedUnit());
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.tv_settings_markerInterval_spinner);
        spinner.setAdapter(null);
        String[] itemsInterval = {"1","2","3","4","5","6","7","8","9","10"};
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsInterval);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getMarkerTimeout() - 1);
        spinner.setOnItemSelectedListener(this);

        if(BuildConfig.FLAVOR.equals("free"))
        {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        Settings settings = Globals.GetInstance(null).getSettings();
        switch(parent.getId())
        {
            case R.id.tv_settings_AppMode_spinner:
            {
                String selection = ((TextView) view).getText().toString();
                if (selection.equals(strAuto))
                    settings.setAppMode(Settings.APP_MODE_AUTO);
                else
                    settings.setAppMode(Settings.APP_MODE_MANUAL);
                break;
            }
            case R.id.tv_settings_unitSpeed_spinner:
            {
                String selection = ((TextView) view).getText().toString();
                if (selection.equals(itemsSpeedUnit[0]))
                    settings.setSpeedUnit(Settings.SPEED_UNIT_MS);
                else if (selection.equals(itemsSpeedUnit[1]))
                    settings.setSpeedUnit(Settings.SPEED_UNIT_MPH);
                else
                    settings.setSpeedUnit(Settings.SPEED_UNIT_KPH);
                break;
            }
            case R.id.tv_settings_markerInterval_spinner:
                settings.setMarkerTimeout(Integer.valueOf(((TextView) view).getText().toString()));
                break;
        }
        FileHandler.GetInstance().saveSettings(settings);
        Globals.GetInstance(null).updateSettings(settings);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){}
}
