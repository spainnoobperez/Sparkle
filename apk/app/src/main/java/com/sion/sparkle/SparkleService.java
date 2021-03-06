package com.sion.sparkle;

// Basic
import androidx.annotation.Keep;
import android.os.Build;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import android.app.Service;
import android.os.IBinder;

// Notification
import androidx.core.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent; // Actions
import android.widget.RemoteViews; // Custom notification

// Window manager
import android.view.WindowManager;

// Display size
import android.util.DisplayMetrics;

// Broadcast receiver
import android.content.BroadcastReceiver;
import android.content.IntentFilter;


public class SparkleService extends Service
{
    public static final String ACTION_HIDE = "com.sion.sparkle.ACTION_HIDE";
    public static final String ACTION_SHOW = "com.sion.sparkle.ACTION_SHOW";
    public static final String ACTION_STOP = "com.sion.sparkle.ACTION_STOP";
    public static final String CHANNEL_ID = "SparkleChannel";

    @Override
    public void onDestroy()
    {
        Log.i("Sparkle", "Stopping SparkleService");

        unregisterReceiver(receiver_);

        native_destroy(native_);
    }

    @Override
    public void onCreate()
    {
        Log.i("Sparkle", "Starting SparkleService");

        WereApplication.getInstance(getApplicationContext());

        window_manager_ = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        receiver_ = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(ACTION_STOP))
                {
                    stopSelf();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP);
        registerReceiver(receiver_, filter);

        native_create();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        createNotificationChannel();

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);

        Intent intent1 = new Intent();
        intent1.setAction(ACTION_HIDE);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, 0);

        Intent intent2 = new Intent();
        intent2.setAction(ACTION_SHOW);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);

        Intent intent3 = new Intent();
        intent3.setAction(ACTION_STOP);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(this, 0, intent3, 0);

        notificationLayout.setOnClickPendingIntent(R.id.button1, pendingIntent1);
        notificationLayout.setOnClickPendingIntent(R.id.button2, pendingIntent2);
        notificationLayout.setOnClickPendingIntent(R.id.button3, pendingIntent3);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            //.setContentTitle("Title")
            //.setContentText("Sparkle")
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            //.setOngoing(true)
            //.addAction(R.drawable.notification_icon, "Hide", pendingIntent1)
            //.addAction(R.drawable.notification_icon, "Show", pendingIntent2)
            //.addAction(R.drawable.notification_icon, "Stop", pendingIntent3)
            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout);



        Notification notification = builder.build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Keep
    public void set_native(long native__)
    {
        native_ = native__;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = CHANNEL_ID;
            String description = CHANNEL_ID;
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Keep
    public int display_width()
    {
        DisplayMetrics display_metrics = new DisplayMetrics();
        window_manager_.getDefaultDisplay().getMetrics(display_metrics);
        return display_metrics.widthPixels;
    }

    @Keep
    public int display_height()
    {
        DisplayMetrics display_metrics = new DisplayMetrics();
        window_manager_.getDefaultDisplay().getMetrics(display_metrics);

        int status_bar_height = 0;
        int resource_id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resource_id > 0)
            status_bar_height = getResources().getDimensionPixelSize(resource_id);

        Log.i("Sparkle", String.format("status_bar_height = %d", status_bar_height));

        return display_metrics.heightPixels - status_bar_height;
    }

    private native void native_create();
    private native void native_destroy(long native__);

    private long native_ = 0;
    WindowManager window_manager_;
    private BroadcastReceiver receiver_;

    static
    {
        System.loadLibrary("sparkle");
    }
}
