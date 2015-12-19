package com.alorma.over;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class OverService extends Service {
    private WindowManager windowManager;
    private ImageView image;

    public OverService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        execute();
    }

    private void execute() {
        notifyOver();

        showImage();
    }

    private void notifyOver() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("Over");
        builder.setContentText("Drawing image is active");
        Intent intent = new Intent();
        intent.setAction(BuildConfig.APPLICATION_ID + ".stop");
        PendingIntent pending = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(0, "Stop", pending));
        builder.setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void showImage() {
        try {
            // get input stream
            InputStream ims = getAssets().open("screen.png");

            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            image = new ImageView(this);
            image.setImageBitmap(bitmap);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.alpha = 0.5f;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;

            windowManager.addView(image, params);
        } catch (IOException ex) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        if (image != null && windowManager != null) {
            windowManager.removeView(image);
        }
        NotificationManagerCompat.from(this).cancel(0);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new Binder();
    }
}
