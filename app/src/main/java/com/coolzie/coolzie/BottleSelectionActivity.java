package com.coolzie.coolzie;

import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BottleSelectionActivity extends AppCompatActivity {

    private static String TAG = "BottleSelection";

    private TempChart tempChart = null;
    private int chillTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottle_selection);

        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chillTime <= 0) {
                    Toast.makeText(
                            BottleSelectionActivity.this,
                            R.string.no_time_set_message,
                            Toast.LENGTH_SHORT
                    ).show();

                } else {
                    Intent i = new Intent(AlarmClock.ACTION_SET_TIMER);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.timer_message));
                    i.putExtra(AlarmClock.EXTRA_LENGTH, chillTime);
                    i.putExtra(AlarmClock.EXTRA_VIBRATE, true);
                    startActivity(i);
                }
            }
        });

        tempChart = new TempChart(this);

        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, tempChart.getBeverages());
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.beverageAutoCompleteTextView);
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        Log.i(TAG, "Loaded " + autoCompleteAdapter.getCount() + " beverages into autocomplete");

        ArrayAdapter<String> spinnerAdapter =  new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tempChart.getStartingTemperatures());
        final Spinner spinner = (Spinner) findViewById(R.id.roomTempSpinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Log.i(TAG, "Loaded " + spinnerAdapter.getCount() + " starting temps into spinner");

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String beverage = autoCompleteTextView.getText().toString();
                String startTemp = (String) spinner.getSelectedItem();
                Log.i(TAG, "Selected beverage, '" + beverage + "', and starting temp, '" + startTemp + "'");
                setTimeToChill(beverage, startTemp);
            }
        });

        spinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String beverage = autoCompleteTextView.getText().toString();
                String startTemp = (String) spinner.getSelectedItem();
                Log.i(TAG, "Selected beverage, '" + beverage + "', and starting temp, '" + startTemp + "'");
                setTimeToChill(beverage, startTemp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                clearChillTime();
            }
        });
    }

    private void clearChillTime() {
        setTimeToChill(null, null);
    }

    private void setTimeToChill(String beverage, String startTemp) {
        TextView timeToChill = (TextView) findViewById(R.id.timeToChillTextView);
        if (beverage == null || beverage.length() <= 0 || startTemp == null) {
            Log.i(TAG, "Clearing chill time");
            timeToChill.setText("");
            chillTime = -1;

        } else {
            chillTime = tempChart.getChillTime(beverage, startTemp);

            int seconds = chillTime % 60;
            int minutes = chillTime / 60;
            String msg = "";
            if (minutes > 0) msg += minutes + " mins ";
            if (seconds > 0) msg += seconds + " secs";

            Log.i(TAG, "Setting chill time to '" + msg + "'");
            timeToChill.setText(msg);
        }
    }

}
