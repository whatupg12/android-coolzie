package com.coolzie.coolzie;

import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BottleSelectionActivity extends AppCompatActivity {

    private static String TAG = "BottleSelection";

    private TempChart tempChart = null;
    private int chillTimeSeconds = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottle_selection);

        tempChart = new TempChart(this);

        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButtonClicked();
            }
        });

        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, tempChart.getBeverages());
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.beverageAutoCompleteTextView);
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        Log.i(TAG, "Loaded " + autoCompleteAdapter.getCount() + " beverages into autocomplete");

        final EditText roomTempEditText = (EditText) findViewById(R.id.roomTempatureEditText);
        final EditText targetTempEditText = (EditText) findViewById(R.id.targetTempEditText);

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beverageOrStartingTempSelected(autoCompleteTextView, roomTempEditText, targetTempEditText, true);
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                beverageOrStartingTempSelected(autoCompleteTextView, roomTempEditText, targetTempEditText, true);
            }
        });

        roomTempEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                beverageOrStartingTempSelected(autoCompleteTextView, roomTempEditText, targetTempEditText, false);
                return true;
            }
        });

        targetTempEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                beverageOrStartingTempSelected(autoCompleteTextView, roomTempEditText, targetTempEditText, false);
                return true;
            }
        });
    }

    private void startButtonClicked() {
        Log.d(TAG, "Start button clicked.");

        if (chillTimeSeconds <= 0) {
            Log.d(TAG, "No chill time established; showing toast.");

            Toast.makeText(
                    BottleSelectionActivity.this,
                    R.string.no_time_set_message,
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            String beverage = ((AutoCompleteTextView) findViewById(R.id.beverageAutoCompleteTextView))
                    .getText().toString();

            Log.i(TAG, "Starting timer for " + beverage + ": " + chillTimeSeconds);

            Intent i = new Intent(AlarmClock.ACTION_SET_TIMER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, beverage);
            i.putExtra(AlarmClock.EXTRA_LENGTH, chillTimeSeconds);
            i.putExtra(AlarmClock.EXTRA_VIBRATE, true);
            startActivity(i);
        }
    }

    private void beverageOrStartingTempSelected(AutoCompleteTextView autoCompleteTextView,
                                                EditText roomTempEditText,
                                                EditText targetTempEditText,
                                                boolean beverageSet) {

        String beverage = autoCompleteTextView.getText().toString();
        Double startTemp = getDoubleOrNull(roomTempEditText.getText().toString());
        Double endTemp = null;
        if (!beverageSet || !tempChart.hasBeverage(beverage)) {
            endTemp = getDoubleOrNull(targetTempEditText.getText().toString());
        }

        setTimeToChill(beverage, startTemp, endTemp);
    }

    private Double getDoubleOrNull(String numberStr) {
        Double rtn = null;
        if (numberStr != null && numberStr.length() >= 0) {
            try {
                rtn = Double.parseDouble(numberStr);
            } catch (NumberFormatException ex) {
                Log.d(TAG, "Could not parse Double from '" + numberStr + "'");
            }
        }
        return rtn;
    }

    private void clearChillTime() {
        setTimeToChill(null, null, null);
    }

    private void setTimeToChill(String beverage, Double startTemp, Double targetTemp) {
        if (beverage != null && targetTemp == null) {
            Log.i(TAG, "Target temp was empty, but a beverage was specified; " +
                    "Looking up target temp for " + beverage);
            targetTemp = tempChart.getTargetTemp(beverage);

            if (targetTemp != null) {
                String targetTempStr = targetTemp.toString();
                Log.i(TAG, "Found a target temp for " + beverage + "; setting widget to " + targetTempStr);
                ((EditText) findViewById(R.id.targetTempEditText)).setText(targetTempStr);
            }
        }

        TextView timeToChill = (TextView) findViewById(R.id.timeToChillTextView);
        if (startTemp == null || targetTemp == null) {
            Log.i(TAG, "Clearing chill time");
            timeToChill.setText("");
            chillTimeSeconds = -1;

        } else if (startTemp <= targetTemp) {
            Log.i(TAG, "Start temp, " + startTemp + ", is greater than target temp, " + targetTemp + "; Setting message.");
            timeToChill.setText(getString(R.string.ready_to_serve));
            chillTimeSeconds = -1;

        } else {
            // its about 1 minute per degree
            chillTimeSeconds = (int) Math.ceil((startTemp - targetTemp) * 60);

            // extra minute for target temperatures greater than 60 degrees
            if (targetTemp > 60) chillTimeSeconds += 60;

            int seconds = chillTimeSeconds % 60;
            int minutes = chillTimeSeconds / 60;
            String msg = "";
            if (minutes > 0) msg += minutes + " mins ";
            if (seconds > 0) msg += seconds + " secs";

            Log.i(TAG, "Setting chill time to '" + msg + "'");
            timeToChill.setText(msg);
        }
    }

}
