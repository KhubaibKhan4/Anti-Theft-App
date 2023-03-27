package com.example.androidantithefttask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button pocketRemovalButton;
    private Button chargerRemovalButton;
    private Button motionDetectionButton;
    private static final String CHANNEL_ID = "AntiTheftApp";
    private static final int NOTIFICATION_ID = 1;
    private boolean pocketServiceStarted = false;
    private boolean chargerServiceStarted = false;
    private boolean motionServiceStarted = false;

    private BroadcastReceiver userPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                // Change the button text back to the default text
                if (pocketRemovalButton.getText().toString().contentEquals("Stop Pocket Removal Detection")){
                    pocketRemovalButton.setText("Start Pocket Removal Detection");
                } else if (chargerRemovalButton.getText().toString().contentEquals("Stop Charger Removal Detection")){
                    chargerRemovalButton.setText("Start Charger Removal Detection");
                } else if (motionDetectionButton.getText().toString().contentEquals("Stop Motion Detection")){
                    motionDetectionButton.setText("Start Motion Detection");
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pocketRemovalButton = findViewById(R.id.pocketRemovalButton);
        chargerRemovalButton = findViewById(R.id.chargerRemovalButton);
        motionDetectionButton = findViewById(R.id.motionDetectionButton);

        pocketRemovalButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (!pocketServiceStarted) {
                    startPocketRemovalService();
                    pocketServiceStarted = true;
                    pocketRemovalButton.setText("Stop Pocket Removal Detection");
                } else {
                    stopPocketRemovalService();
                    pocketServiceStarted = false;
                    pocketRemovalButton.setText("Start Pocket Removal Detection");
                }
            }
        });

        chargerRemovalButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (!chargerServiceStarted) {
                    startChargerRemovalService();
                    chargerServiceStarted = true;
                    chargerRemovalButton.setText("Stop Charger Removal Detection");
                } else {
                    stopChargerRemovalService();
                    chargerServiceStarted = false;
                    chargerRemovalButton.setText("Start Charger Removal Detection");
                }
            }
        });

        motionDetectionButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (!motionServiceStarted) {
                    startMotionDetectionService();
                    motionServiceStarted = true;
                    motionDetectionButton.setText("Stop Motion Detection");
                } else {
                    stopMotionDetectionService();
                    motionServiceStarted = false;
                    motionDetectionButton.setText("Start Motion Detection");
                }
            }
        });

        createNotificationChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startPocketRemovalService() {
        Intent intent = new Intent(this, PocketRemovalService.class);
        startForegroundService(intent);
        showNotification("Pocket Removal Detection Service", "Service Started");
    }

    private void stopPocketRemovalService() {
        Intent intent = new Intent(this, PocketRemovalService.class);
        stopService(intent);
        showNotification("Pocket Removal Detection Service", "Service Stopped");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startChargerRemovalService() {
        Intent intent = new Intent(this, ChargerRemovalService.class);
        startForegroundService(intent);
        showNotification("Charger Removal Detection Service", "Service Started");
    }

    private void stopChargerRemovalService() {
        Intent intent = new Intent(this, ChargerRemovalService.class);
        stopService(intent);
        showNotification("Charger Removal Detection Service", "Service Stopped");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMotionDetectionService() {
        Intent intent = new Intent(this, MotionDetectionService.class);
        startForegroundService(intent);
        showNotification("Motion Detection Service", "Service Started");
    }

    private void stopMotionDetectionService() {
        Intent intent = new Intent(this, MotionDetectionService.class);
        stopService(intent);
        showNotification("Motion Detection Service", "Service Stopped");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Anti-Theft App";
            String description = "Notification channel for Anti-Theft App";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver for ACTION_USER_PRESENT
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(userPresentReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the broadcast receiver
        unregisterReceiver(userPresentReceiver);
    }


}