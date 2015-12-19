package com.alorma.over;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private View button;
    private int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = findViewById(R.id.button);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startOverSource();
            } else {
                openRequestPermission();
            }
        } else {
            startOverSource();
        }
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
                Snackbar.make(button, "Permission not granted :(", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void startOverSource() {
        Intent intent = new Intent(this, OverService.class);
        startService(intent);
    }

}
