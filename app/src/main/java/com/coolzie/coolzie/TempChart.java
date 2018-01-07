package com.coolzie.coolzie;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempChart {

    private String TAG = "TempChart";

    private Map<String, List<String>> timesTemps = new HashMap<>();
    Map<String, Integer> startingTempsPositions = new HashMap<>();

    TempChart(Context ctx) {
        File localChart = new File(ctx.getFilesDir(), "temps.csv");
        if (!localChart.exists()) {
            Log.i(TAG, "Local temp chart not found; loading from resources.");

            copyResourceTempChartToLocal(ctx, localChart);

        } else {
            Log.i(TAG, "Found local temp chart; loading it");
        }

        loadLocalTempChart(localChart);
    }

    private void copyResourceTempChartToLocal(Context ctx, File localChart) {
        InputStream in = ctx.getResources().openRawResource(R.raw.temps);
        OutputStream out = null;
        try {
            if (in == null) {
                String msg = "Cannot locate temp chart in resources.";
                Log.e(TAG, msg);
                throw new RuntimeException(msg);
            }
            out = new FileOutputStream(localChart);

            byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }

        } catch (IOException ioex) {
            String msg = "Could not load temp chart from resources.";
            Log.e(TAG, msg, ioex);
            throw new RuntimeException("Could not load temp chart from resources.", ioex);

        } finally {
            if (in != null) try { in.close(); } catch (IOException ioex) { /* ignore */ }
            if (out != null) try { out.close(); } catch (IOException ioex) { /* ignore */ }
        }
    }

    private void loadLocalTempChart(File localChart) {
        BufferedReader in = null;
        List<String> headings;
        try {
            in = new BufferedReader(new FileReader(localChart));

            String heading = in.readLine();
            headings = Arrays.asList(heading.split(","));

            String line;
            while ((line = in.readLine()) != null) {
                List<String> lineTemps = Arrays.asList(line.split(","));
                timesTemps.put(lineTemps.get(0), lineTemps.subList(1, lineTemps.size()-1));
            }

            Log.i(TAG, "Loaded temp chart; " + timesTemps.size() + " beverages loaded");

        } catch (IOException ioex) {
            String msg = "Could not load temp chart.";
            Log.e(TAG, msg, ioex);
            throw new RuntimeException(msg, ioex);

        } finally {
            if (in != null) try { in.close(); } catch (IOException ioex) { /* ignore */ }
        }

        List<String> startTemps = headings.subList(1, headings.size()-1);
        for (int i = 0; i < startTemps.size(); i++) {
            startingTempsPositions.put(startTemps.get(i), i);
        }
    }

    List<String> getBeverages() {
        List<String> bevs = new ArrayList<>(timesTemps.keySet());
        Collections.sort(bevs);
        return bevs;
    }

    List<String> getStartingTemperatures() {
        List<String> startTemps = new ArrayList<>(startingTempsPositions.keySet());
        Collections.sort(startTemps);
        return startTemps;
    }

    int getChillTime(String beverage, String startingTemp) {
        int tempPos = startingTempsPositions.get(startingTemp);
        List<String> beverageTimes = timesTemps.get(beverage);
        String chillTimeStr = beverageTimes.get(tempPos);

        Log.i(TAG, "Found chill time for '" + beverage + "' starting at '" + chillTimeStr + "': " + chillTimeStr);
        return Integer.parseInt(chillTimeStr);
    }

}
