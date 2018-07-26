package com.sadashivsinha.sadashiv;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sadashivsinha on 26/07/18.
 */

public class TimerService extends Service {

    static final String TIMER_COUNT = "countdown";
    static final String TIMER_RUNNING_STATUS = "status";
    final static int id = 100;

    Calendar calendar;
    SharedPreferences mpref;
    static SharedPreferences.Editor mEditor;

    private static Timer mTimer = null;
    public static final long NOTIFY_INTERVAL = 1000;
    Intent intent;

    private Handler mHandler = new Handler();

    public static String str_receiver = "sadashiv_sinha";

    static int count = 0;

    static NotificationCompat.Builder builder;
    static NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TimerTask onCreate : ", "start");

        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();

        count = mpref.getInt(TIMER_COUNT, 0);
        Log.d("Val : ", String.valueOf(count));

        calendar = Calendar.getInstance();

        Boolean timerRunningStatus = mpref.getBoolean(TIMER_RUNNING_STATUS, false);
        mTimer = new Timer();

        Log.d("STATUS ", String.valueOf(timerRunningStatus));
        if (timerRunningStatus) {

            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), NOTIFY_INTERVAL, NOTIFY_INTERVAL);
            intent = new Intent(str_receiver);
        }

        createAndDisplayNotification("Timer : " + String.valueOf(count));
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    calendar = Calendar.getInstance();

                    mEditor.putInt(TIMER_COUNT, ++count);
                    mEditor.apply();
                    mEditor.commit();
                    updateTimer(String.valueOf(count));

                    Log.d("TimerTask : ", "run");

                }

            });
        }

    }

    public void createAndDisplayNotification(String title) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            if (builder == null) {
                builder = new NotificationCompat.Builder(getApplicationContext())
                        .setContentText("Sadashiv Timer App")
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setAutoCancel(false)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(
                                PendingIntent.getActivity(getApplicationContext(), 10,
                                        new Intent(getApplicationContext(), MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                                        0)
                        );

                notificationManager.notify(id, builder.build());
            }
        }
    }


    public static void pauseTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    public static void stopAllServices() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        //todo : notification dismiss
    }

    public static void resetTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        if (mTimer != null)
            mTimer.cancel();

        super.onDestroy();
        Log.e("Service finish", "Finish");
    }

    private void updateTimer(String timeString) {

        builder.setContentTitle("Timer : " + timeString);
        notificationManager.notify(id, builder.build());

        Log.d("updateTimer : ", "run");
        intent.putExtra("time", timeString);
        sendBroadcast(intent);
    }
}
