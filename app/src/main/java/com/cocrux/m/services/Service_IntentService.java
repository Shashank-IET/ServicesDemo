package com.cocrux.m.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;


public class Service_IntentService extends IntentService {

    public static final String ACTION_DOWNLOAD_COMPLETE = "com.cocrux.m.services.action.DownloadComplete";

    private static final String ACTION_SLEEP = "com.cocrux.m.services.action.SLEEP";
    private static final String ACTION_FETCH_JSON = "com.cocrux.m.services.action.FETCH_JSON";

    private static final String PARAM_TIME = "com.cocrux.m.services.extra.PARAM1";
    private static final String PARAM_URL = "com.cocrux.m.services.extra.URL";


    public Service_IntentService() {
        this("Service_IntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Service_IntentService(String name) {
        super(name);
    }

    /**
     * Starts this service to perform action SLEEP with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionSleep(Context context, String time_seconds) {
        Intent intent = new Intent(context, Service_IntentService.class);
        intent.setAction(ACTION_SLEEP);
        int time;
        try {
            time = Integer.parseInt(time_seconds);
        } catch (NumberFormatException ex) {
            return;
        }
        intent.putExtra(PARAM_TIME, time);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action SING with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionFetch(Context context, String url) {
        Intent intent = new Intent(context, Service_IntentService.class);
        intent.setAction(ACTION_FETCH_JSON);
        intent.putExtra(PARAM_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("msg", "Received Intent! " + intent.getAction());
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SLEEP.equals(action)) {
                final int timeSeconds = intent.getIntExtra(PARAM_TIME, 1);
                handleActionSleep(timeSeconds);
            } else if (ACTION_FETCH_JSON.equals(action)) {
                final String url = intent.getStringExtra(PARAM_URL);
                handleActionFetch(url);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     *
     * @param time seconds to sleep
     */
    private void handleActionSleep(int time) {
        Log.d("msg", "starting sleep");
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            Thread.interrupted();
            e.printStackTrace();
        }
        Log.d("msg", "Awake!");
    }

    String error = "";

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     *
     * @param URL Url to fetch data from
     */
    private void handleActionFetch(String URL) {

        Log.d("msg", "Starting Download!");
        String result = null;
        int resCode;
        InputStream in;
        try {
            URL url = new URL(URL);
            URLConnection urlConn = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setConnectTimeout(5000);
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                error += resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_DOWNLOAD_COMPLETE);
        if (result != null)
            broadcastIntent.putExtra("data", result);
        else
            broadcastIntent.putExtra("data", error);
        sendBroadcast(broadcastIntent);
//        return result;
    }
}
