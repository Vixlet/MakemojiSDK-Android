package com.makemoji.mojilib.gif;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

final class SimpleBitmapProvider implements GifDecoder.BitmapProvider {
    @NonNull @Override public Bitmap obtain(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    @Override public void release(Bitmap bitmap) {
        bitmap.recycle();
    }

}