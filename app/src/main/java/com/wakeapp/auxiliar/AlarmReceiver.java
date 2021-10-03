package com.wakeapp.auxiliar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.wakeapp.R;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;
    private static final String ALARM_CHANNEL_ID = "ALARM_NOTIFICATION_CHANNEL";
    private static final long[] VIBRATION_PATTERN = { 0, 100, 200, 300 };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        loadLocalization(context);
        if (action.equals("ALARM_TRIGGER"))
        {
            if (mediaPlayer == null) {
                triggerNotification(context, intent);
            } else {
                if (!mediaPlayer.isPlaying()) {
                    triggerNotification(context, intent);
                }
            }
        } else if (action.equals("DISMISS_NOTIFICATION")) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void triggerNotification(Context context, Intent intent) {
        mediaPlayer = MediaPlayer.create(context, loadRingtone(context));
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        buildNotification(context, intent);
    }

    private void buildNotification(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        checkAlarmChannel(notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                ALARM_CHANNEL_ID
        );

        builder.setSmallIcon(R.drawable.ic_wakeapp_icon)
            .setColor(context.getResources().getColor(R.color.colorAccent))
            .setContentText(intent.getExtras().getString("ALARM_NAME"))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setVibrate(VIBRATION_PATTERN)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDeleteIntent(getDeleteIntent(context));

        String alarmType = intent.getExtras().getString("ALARM_TYPE");
        if ("GEO_ALARM".equals(alarmType)) {
            builder.setContentTitle(context.getString(R.string.geoalarm_trigger));
        } else {
            builder.setContentTitle(context.getString(R.string.alarm_trigger));
        }

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

    private void checkAlarmChannel(NotificationManager notificationManager) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(ALARM_CHANNEL_ID) == null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                NotificationChannel notificationChannel = new NotificationChannel(
                        ALARM_CHANNEL_ID,
                        "Alarm Channel",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by the geo-alarms");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
                notificationChannel.setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        audioAttributes);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    protected PendingIntent getDeleteIntent(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("DISMISS_NOTIFICATION");
        return PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void loadLocalization(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String appLanguage = sp.getString("appLanguage", "en");
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Uri loadRingtone(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String alarmRingtone = sp.getString("alarmRingtone", null);
        Uri alarmUri = alarmRingtone != null ? Uri.parse(alarmRingtone) : null;

        if (alarmUri != null) {
            return alarmUri;
        } else {
            return Settings.System.DEFAULT_RINGTONE_URI;
        }
    }
}
