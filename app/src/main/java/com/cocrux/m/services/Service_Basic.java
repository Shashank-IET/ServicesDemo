package com.cocrux.m.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Service_Basic extends Service {

    boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        Log.d("msg", "Started Basic Service!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    Log.d("msg", "Exception while trying asleep!");
                    e.printStackTrace();
                }
                if (isRunning)
                    stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.d("msg", "now destroying");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
