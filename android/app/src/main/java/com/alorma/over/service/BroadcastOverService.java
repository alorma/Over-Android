package com.alorma.over.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.alorma.over.BuildConfig;

public class BroadcastOverService extends Service {

    private static final String ACTION_PLAY = BuildConfig.APPLICATION_ID + ".play";
    private static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".stop";
    private static final String ACTION_CANCEL = BuildConfig.APPLICATION_ID + ".cancel";

    private BroadcastReceiver broadcastReceiver;
    private BroadcastOverBinder binder;

    public BroadcastOverService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_CANCEL);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (binder != null) {
                    handleIntent(intent);
                }
            }
        };

        registerReceiver(broadcastReceiver, filter);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_PLAY.equals(intent.getAction())) {
            binder.show();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            binder.hide();
        } else if (ACTION_CANCEL.equals(intent.getAction())) {
            binder.cancel();
        }
    }

    @Override
    public void onDestroy() {
        unregister();
        super.onDestroy();
    }

    private void unregister() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null) {
            binder = new BroadcastOverBinder();
        }
        return binder;
    }

    public class BroadcastOverBinder extends Binder implements Stoppable {

        private BroadcastOverCallback callback;

        public void show() {
            if (callback != null) {
                callback.showImage();
            }
        }

        public void hide() {
            if (callback != null) {
                callback.hideImage();
            }
        }

        public void cancel() {
            if (callback != null) {
                callback.cancel();
            }
        }

        @Override
        public void stop() {
            unregister();
            stopSelf();
        }

        public void setCallback(BroadcastOverCallback callback) {
            this.callback = callback;
        }

    }

    public interface BroadcastOverCallback {
        void showImage();

        void hideImage();

        void cancel();
    }
}
