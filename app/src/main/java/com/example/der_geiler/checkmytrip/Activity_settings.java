package com.example.der_geiler.checkmytrip;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Activity_settings extends Activity implements AdapterView.OnItemSelectedListener
{
    private static final String[] itemsAppMode      = {"Auto","Manual"};
    private static final String[] itemsSpeedUnit    = {"m/s","mph","kph"};
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_settings);

        Settings settings = Globals.GetInstance(null).getSettings();

        Spinner spinner = (Spinner) findViewById(R.id.tv_settings_AppMode_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsAppMode);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getAppMode());
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.tv_settings_unitSpeed_spinner);
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsSpeedUnit);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getSpeedUnit());
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.tv_settings_markerInterval_spinner);
        String[] itemsInterval = {"1","2","3","4","5","6","7","8","9","10"};
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsInterval);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(settings.getMarkerTimeout()-1);
        spinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        //parent.getItemAtPosition(pos)
        Settings settings = Globals.GetInstance(null).getSettings();
        switch(parent.getId())
        {
            case R.id.tv_settings_AppMode_spinner:
            {
                String selection = ((TextView) view).getText().toString();
                if (selection.equals(itemsAppMode[0]))
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
