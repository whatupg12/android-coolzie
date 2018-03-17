package com.coolzie.coolzie;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempChart {

    private String TAG = "TempChart";

    private Map<String, Double> targetTemps = new HashMap<>();

    TempChart(Context ctx) {
        loadResourceTempChart(ctx);
    }

    private void copyResourceToLocalAndLoad(Context ctx) {
        File localChart = new File(ctx.getFilesDir(), "temps.csv");
        if (!localChart.exists()) {
            Log.i(TAG, "Local temp chart not found; loading from resources.");

            copyResourceTempChartToLocal(ctx, localChart);

        } else {
            Log.i(TAG, "Found local temp chart; loading it");
        }

        loadLocalTempChart(localChart);
    }

    private void loadResourceTempChart(Context ctx) {
        InputStream in = ctx.getResources().openRawResource(R.raw.temps);

        try {
            if (in == null) {
                String msg = "Cannot locate temp chart in resources.";
                Log.e(TAG, msg);
                throw new RuntimeException(msg);
            }

            loadTempChart(new BufferedReader(new InputStreamReader(in, "UTF-8")));

        } catch (Exception e) {
            String msg = "Could not load temp chart from resources.";
            Log.e(TAG, msg, e);

            Toast.makeText(ctx,msg + e.getMessage(), Toast.LENGTH_LONG).show();

        } finally {
            if (in != null) try { in.close(); } catch (IOException ioex) { /* ignore */ }
        }

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
        try {
            loadTempChart(in = new BufferedReader(new FileReader(localChart)));
        } catch (IOException ioex) {
            String msg = "Could not load temp chart from local file.";
            Log.e(TAG, msg, ioex);
            throw new RuntimeException(msg, ioex);

        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException ioex) { /* ignore */ }
        }
    }

    private void loadTempChart(BufferedReader in) {
        List<String> headings;
        try {
            String heading = in.readLine();
            headings = Arrays.asList(heading.split(","));

            String line;
            while ((line = in.readLine()) != null) {
                List<String> lineTemps = Arrays.asList(line.split(","));

                String beverage = lineTemps.get(0);
                Double targetTemp = Double.parseDouble(lineTemps.get(lineTemps.size()-1));
                targetTemps.put(beverage, targetTemp);
            }

            Log.i(TAG, "Loaded temp chart; " + targetTemps.size() + " beverages loaded");

        } catch (IOException ioex) {
            String msg = "Could not load temp chart.";
            Log.e(TAG, msg, ioex);
            throw new RuntimeException(msg, ioex);

        }
    }

    List<String> getBeverages() {
        List<String> bevs = new ArrayList<>(targetTemps.keySet());
        Collections.sort(bevs);
        return bevs;
    }

    boolean hasBeverage(String beverage) {
        return targetTemps.containsKey(beverage);
    }

    Double getTargetTemp(String beverage) {
        return targetTemps.get(beverage);
    }

}
