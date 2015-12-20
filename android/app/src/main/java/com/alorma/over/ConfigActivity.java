package com.alorma.over;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.alorma.over.service.OverService;


public class ConfigActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Switch activate;
    private int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private OverService.OverServiceBinder serviceBinder;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBinder = (OverService.OverServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activate = (Switch) findViewById(R.id.activate);
        activate.setOnCheckedChangeListener(this);

        startOverService();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void openRequestPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Snackbar.make(activate, "Permission not granted :(", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void startOverService() {
        Intent intent = new Intent(this, OverService.class);
        bindService(intent, serviceConn, BIND_AUTO_CREATE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        handleCheck(isChecked);
    }

    private void handleCheck(boolean isChecked) {
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    showImage();
                } else {
                    openRequestPermission();
                }
            } else {
                showImage();
            }
        } else {
            hideImage();
            if (serviceBinder != null) {
                serviceBinder.hide();
            }
        }
    }

    private void showImage() {
        Intent intent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".play");
        sendBroadcast(intent);
    }

    private void hideImage() {
        Intent intent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".stop");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        if (serviceBinder != null) {
            serviceBinder.disconnect();
        }
        unbindService(serviceConn);
        super.onDestroy();
    }
}
