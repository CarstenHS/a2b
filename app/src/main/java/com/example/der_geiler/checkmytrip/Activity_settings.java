package com.example.der_geiler.checkmytrip;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Activity_settings extends Activity implements AdapterView.OnItemSelectedListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_settings);

        Spinner spinner = (Spinner) findViewById(R.id.tv_settings_AppMode_spinner);
        spinner.setOnItemSelectedListener(this);
        String[] items = {"Auto","Manual"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner = (Spinner) findViewById(R.id.tv_settings_unitSpeed_spinner);
        spinner.setOnItemSelectedListener(this);
        String[] itemsSpeedUnit = {"m/s","mph","kph"};
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsSpeedUnit);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner = (Spinner) findViewById(R.id.tv_settings_markerInterval_spinner);
        spinner.setOnItemSelectedListener(this);
        String[] itemsInterval = {"1","2","3","4","5","6","7","8","9","10"};
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsInterval);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);



    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        //parent.getItemAtPosition(pos)
        switch(view.getId())
        {
            case R.id.tv_settings_AppMode_spinner:
                break;
            case R.id.tv_settings_unitSpeed_spinner:
                break;
            case R.id.tv_settings_markerInterval_spinner:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

}
