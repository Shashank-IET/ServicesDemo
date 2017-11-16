package com.cocrux.m.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class Service_Bound extends Service {

    IBinder mBinder = new _MBinder();
    private final Random mGenerator = new Random();
    boolean aDownloadInProgress = false;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class _MBinder extends Binder {
        Service_Bound getService() {
            return Service_Bound.this;
        }
    }

    public int getRandomNumber(int within) {
        return mGenerator.nextInt(within);
    }

    public void startDownload(final Context context, final String url) {

        if(aDownloadInProgress){
            Log.d("msg", "Already In Process");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                aDownloadInProgress = true;
                String response = doNetworkTask(url);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("data", response);
                editor.apply();
                Log.d("msg", "Done! " + response);
            }
        }).start();
    }

    String error;

    private String doNetworkTask(String URL) {

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
        aDownloadInProgress = false;
        return result != null ? result : error;
    }
}
