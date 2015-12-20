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
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.alorma.over.service.OverService;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.WebSocket;

import java.util.concurrent.ExecutionException;


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
    private EditText ipText;
    private EditText portText;
    private String TAG = "ALORMAWS";
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activate = (Switch) findViewById(R.id.activate);
        activate.setOnCheckedChangeListener(this);


        ipText = (EditText) findViewById(R.id.ip);
        portText = (EditText) findViewById(R.id.port);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.EMPTY.buildUpon()
                        .scheme("ws")
                        .encodedAuthority(ipText.getText().toString() + ":" + portText.getText().toString())
                        .build();

                connect(uri);
            }
        });

        startOverService();
    }

    private void connect(Uri uri) {
        Future<WebSocket> socketFuture = AsyncHttpClient.getDefaultInstance().websocket(uri.toString(), "", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.send("a string");
                webSocket.send(new byte[10]);
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        Log.i(TAG, s);
                    }
                });
                webSocket.setDataCallback(new DataCallback() {
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                        Log.i(TAG, "I got some bytes!");
                        byteBufferList.recycle();
                    }
                });
            }
        });

        socketFuture.setCallback(new FutureCallback<WebSocket>() {
            @Override
            public void onCompleted(Exception e, WebSocket result) {
                if (e != null) {
                    e.printStackTrace();
                } else if (result != null) {
                    webSocket = result;
                    webSocket.send("Hi! from Android");
                }
            }
        });
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

        if (webSocket != null) {
            webSocket.close();
        }

        super.onDestroy();
    }
}
