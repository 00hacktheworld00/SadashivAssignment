package com.sadashivsinha.sadashiv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.sadashivsinha.sadashiv.TimerService.builder;
import static com.sadashivsinha.sadashiv.TimerService.notificationManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView btnStart, btnPause, btnStop, btnReset;
    TextView textTimer;

    SharedPreferences mpref;
    static SharedPreferences.Editor mEditor;

    Boolean isPauseMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewsById();
        setupListeners();
        getStoredData();
    }

    public void findViewsById() {
        textTimer = findViewById(R.id.text_timer);

        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnReset = findViewById(R.id.btn_reset);

    }

    public void setupListeners() {
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    public void getStoredData() {
        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();

        int count = mpref.getInt(TimerService.TIMER_COUNT, 0);
        textTimer.setText(String.valueOf(count));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str_time = intent.getStringExtra("time");
            textTimer.setText(str_time);

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(TimerService.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnStart)) {
            Log.d("Button Pressed : ", "START");
            mEditor.putBoolean(TimerService.TIMER_RUNNING_STATUS, true);
            mEditor.apply();
            mEditor.commit();
            startService(new Intent(MainActivity.this, TimerService.class));

            isPauseMode = false;
        }

        if (view.equals(btnPause)) {
            Log.d("Button Pressed : ", "PAUSE");
            mEditor.putBoolean(TimerService.TIMER_RUNNING_STATUS, false);
            mEditor.apply();
            mEditor.commit();
            TimerService.pauseTimer();
            stopService(new Intent(MainActivity.this, TimerService.class));
            isPauseMode = true;

        }

        if (view.equals(btnStop)) {
            Log.d("Button Pressed : ", "STOP");
            mEditor.putBoolean(TimerService.TIMER_RUNNING_STATUS, false);
            mEditor.apply();
            mEditor.commit();
            TimerService.stopAllServices();
            stopService(new Intent(MainActivity.this, TimerService.class));

            clearData();
            if(notificationManager!=null)
                notificationManager.cancel(TimerService.id);

        }

        if (view.equals(btnReset)) {
            Log.d("Button Pressed : ", "RESET");
            mEditor.putBoolean(TimerService.TIMER_RUNNING_STATUS, false);
            mEditor.apply();
            mEditor.commit();
            resetTimerAndStopService();
        }
    }

    public void resetTimerAndStopService() {

        if (!isPauseMode)
            Toast.makeText(this, "Timer is not in PAUSE mode.", Toast.LENGTH_SHORT).show();
        else {
            clearData();
            if(TimerService.notificationManager!=null){
                builder.setContentTitle("Timer : 0");
                notificationManager.notify(TimerService.id, builder.build());
            }
            TimerService.resetTimer();
            stopService(new Intent(MainActivity.this, TimerService.class));
        }
    }

    private void clearData(){

        mEditor.putInt(TimerService.TIMER_COUNT, 0);
        textTimer.setText("0");
    }
}
