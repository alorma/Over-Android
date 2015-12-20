package com.alorma.over.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.alorma.over.BuildConfig;
import com.alorma.over.R;

public class NotificationOverService extends Service {

    public NotificationOverService() {

    }

    private void showPlayNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("Over");
        builder.setContentText("Drawing image is not active");
        Intent intent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".play");
        PendingIntent pending = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(0, "Play", pending));
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Intent cancelIntent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".cancel");
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 1235, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(cancelPendingIntent);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void showStopNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("Over");
        builder.setContentText("Drawing image is active");
        Intent intent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".stop");
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        PendingIntent pending = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(0, "Stop", pending));
        builder.setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void hideNotification() {
        NotificationManagerCompat.from(this).cancel(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new NotificationOverBinder();
    }

    public class NotificationOverBinder extends Binder implements Stoppable{
        public void showPlay() {
            showPlayNotification();
        }

        public void showStop() {
            showStopNotification();
        }

        @Override
        public void stop() {
            hideNotification();
        }
    }
}
