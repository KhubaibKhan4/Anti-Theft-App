package com.example.androidantithefttask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MotionDetectionService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private MediaPlayer mediaPlayer;

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private long lastShakeTime;

    private static final String CHANNEL_ID = "AntiTheftApp_Service";
    private static final int NOTIFICATION_ID = 1;

    private BroadcastReceiver mScreenUnlockedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_siren);
        mediaPlayer.setLooping(true);

        // Create a notification channel (if it hasn't been created already)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Anti-Theft App", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        // Build the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charger Removal Detection Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        // Put the service in the foreground
        startForeground(NOTIFICATION_ID, notification);

        // Register the screen unlocked receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenUnlockedReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Motion Detection Service Started", Toast.LENGTH_SHORT).show();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Motion Detection Service Stopped", Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            long currentTime = System.currentTimeMillis();

            if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                if (currentTime - lastShakeTime > 1000) {
                    lastShakeTime = currentTime;
                    mediaPlayer.start();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


