package com.alorma.over.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ImageOverService extends Service {

    private WindowManager windowManager;
    private ImageView image;

    public ImageOverService() {

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
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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

    private void hideImage() {
        try {
            if (image != null && windowManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (image.isAttachedToWindow()) {
                        windowManager.removeView(image);
                    }
                } else {
                    windowManager.removeView(image);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        hideImage();
        image = null;
        windowManager = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ImageOverBinder();
    }

    public class ImageOverBinder extends Binder implements Stoppable {
        public void show() {
            showImage();
        }

        public void hide() {
            hideImage();
        }

        @Override
        public void stop() {
            hideImage();
            stopSelf();
        }
    }
}
