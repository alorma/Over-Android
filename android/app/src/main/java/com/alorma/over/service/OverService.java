package com.alorma.over.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by bernat.borras on 20/12/15.
 */
public class OverService extends Service implements BroadcastOverService.BroadcastOverCallback {

    private BroadcastOverService.BroadcastOverBinder broadcastOverBinder;
    private ServiceConnection broadcastConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            broadcastOverBinder = (BroadcastOverService.BroadcastOverBinder) service;
            broadcastOverBinder.setCallback(OverService.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ImageOverService.ImageOverBinder imageOverBinder;
    private ServiceConnection imageConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            imageOverBinder = (ImageOverService.ImageOverBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private NotificationOverService.NotificationOverBinder notificationOverBinder;
    private ServiceConnection notificationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationOverBinder = (NotificationOverService.NotificationOverBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void connectBroadcast() {
        Intent broadcastIntent = new Intent(this, BroadcastOverService.class);
        bindService(broadcastIntent, broadcastConnection, BIND_AUTO_CREATE);
    }

    private void connectNotification() {
        Intent broadcastIntent = new Intent(this, NotificationOverService.class);
        bindService(broadcastIntent, notificationConnection, BIND_AUTO_CREATE);
    }

    private void connectImage() {
        Intent imageIntent = new Intent(this, ImageOverService.class);
        bindService(imageIntent, imageConnection, BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        connectBroadcast();
        connectNotification();
        connectImage();
        return new OverServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnectReceivers();
        return super.onUnbind(intent);
    }

    @Override
    public void showImage() {
        if (imageOverBinder != null) {
            imageOverBinder.show();
        }

        if (notificationOverBinder != null) {
            notificationOverBinder.showStop();
        }
    }

    @Override
    public void hideImage() {
        if (imageOverBinder != null) {
            imageOverBinder.hide();
        }

        if (notificationOverBinder != null) {
            notificationOverBinder.showPlay();
        }
    }

    @Override
    public void cancel() {
        disconnectReceivers();

        stopSelf();
    }

    private void disconnectReceivers() {
        if (broadcastOverBinder != null) {
            broadcastOverBinder.stop();
        }
        if (imageOverBinder != null) {
            imageOverBinder.stop();
        }
        if (notificationOverBinder != null) {
            notificationOverBinder.stop();
        }
    }

    public class OverServiceBinder extends Binder {
        public void disconnect() {
            disconnectReceivers();
        }

        public void hide() {
            if (notificationOverBinder != null) {
                notificationOverBinder.stop();
            }
        }
    }
}
