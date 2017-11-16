package com.cocrux.m.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        {

        }
        @Override
        public void onReceive(Context context, Intent intent) {
            assert intent != null;
            assert intent.getAction() != null;
            switch (intent.getAction()){
                case Service_IntentService.ACTION_DOWNLOAD_COMPLETE:
                    Log.d("msg", intent.getStringExtra("data"));
                    ((TextView)findViewById(R.id.i_service_resp)).setText(intent.getStringExtra("data"));
                    Toast.makeText(MainActivity.this, "Download Complete!", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Service_Bound._MBinder mBinder = (Service_Bound._MBinder) service;
            boundServiceWorker = mBinder.getService();
            isServiceBound = true;
            ((TextView)findViewById(R.id.connect_service)).setText("CONNECTED");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            boundServiceWorker = null;
            ((TextView)findViewById(R.id.connect_service)).setText("CONNECT");
        }
    };

    private String DOWNLOAD_URL = "http://192.168.43.176:3000/";
    private boolean isServiceBound = false;
    private Service_Bound boundServiceWorker = null;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(Service_IntentService.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mReceiver, filter);
        initialise();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        unregisterReceiver(mReceiver);
        isServiceBound = false;
    }

    private void initialise() {
        findViewById(R.id.s_basic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, Service_Basic.class));
            }
        });

        findViewById(R.id.s_intent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service_IntentService.startActionSleep(MainActivity.this, "5");
                Service_IntentService.startActionFetch(MainActivity.this, DOWNLOAD_URL);
            }
        });

        findViewById(R.id.s_foreground).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.connect_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isServiceBound){
                    Intent intent = new Intent(MainActivity.this, Service_Bound.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });

        findViewById(R.id.random_num).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceBound){
                    int result = boundServiceWorker.getRandomNumber(100);
                    ((TextView)findViewById(R.id.random_num)).setText(result +"");
                }else{
                    Toast.makeText(MainActivity.this, "Please bind to service first!", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        findViewById(R.id.down_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceBound){
                    boundServiceWorker.startDownload(MainActivity.this, DOWNLOAD_URL);
                    findViewById(R.id.down_text).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            ((TextView)findViewById(R.id.down_text)).setText(preferences.getString("data", "..."));
                        }
                    }, 5000);
                }else{
                    Toast.makeText(MainActivity.this, "Please bind to service first!", Toast.LENGTH_SHORT)
                            .show();
                    ((TextView)findViewById(R.id.down_text)).setText("Download Text");
                }
            }
        });
    }
}
