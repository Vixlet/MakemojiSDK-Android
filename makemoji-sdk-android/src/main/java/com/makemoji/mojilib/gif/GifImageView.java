package com.makemoji.mojilib.gif;


        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Handler;
        import android.os.Looper;
        import android.util.AttributeSet;
        import android.view.View;
        import android.widget.ImageView;

        import com.makemoji.mojilib.Moji;

        import java.io.IOException;

        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.Request;
        import okhttp3.Response;

public class GifImageView extends ImageView implements GifConsumer {

    private static final String TAG = "GifDecoderView";
    private GifDecoder gifDecoder;
    private Bitmap tmpBitmap;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long framesDisplayDuration = -1L;

    public GifImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GifImageView(final Context context) {
        super(context);
    }

    GifProducer producer;
    public void setBytes(String url,final byte[] bytes) {
        clear();
        producer = GifProducer.getProducerAndSub(this,bytes,url);

    }

    @Override
    public void onFrameAvailable(final Bitmap b) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                setImageBitmap(b);
            }
        };
        handler.post(r);
    }

    @Override
    public void onStopped() {
        Runnable stop = new Runnable() {
            @Override
            public void run() {

                if (producer!=null) producer.unsubscribe(GifImageView.this);
                producer=null;
            }
        };
        Moji.handler.post(stop);

    }
    @Override
    public void onStarted(GifProducer producer){
        this.producer=producer;
        producer.subscribe(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clear();
    }
    public void clear(){
        if (producer!=null){
            producer.unsubscribe(this);
            producer=null;
        }
    }

    public void getFromUrl(final String url){
        producer = GifProducer.getProducerAndSub(this,null,url);
        if (producer!=null)return;
        Moji.okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                setBytes(url,response.body().bytes());
            }
        });
    }

}