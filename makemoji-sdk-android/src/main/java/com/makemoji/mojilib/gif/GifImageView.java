package com.makemoji.mojilib.gif;


        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Handler;
        import android.os.Looper;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;

        import com.makemoji.mojilib.Moji;
        import com.makemoji.mojilib.R;
        import com.makemoji.mojilib.Spanimatable;
        import com.makemoji.mojilib.Spanimator;

        import java.io.IOException;

        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.Request;
        import okhttp3.Response;

public class GifImageView extends ImageView implements GifConsumer,Spanimatable{

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
        Spanimator.subscribe(Spanimator.HYPER_PULSE,this);
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
    protected void onWindowVisibilityChanged (int visibility){
       // Log.d("GIF"," "+ visibility);
        if (visibility==View.VISIBLE)load();
        if (visibility==View.GONE) clear();
    }

    public synchronized void clear(){
        if (producer!=null){
            producer.unsubscribe(this);
            producer=null;
        }
        if (call!=null){
            call.cancel();
            call = null;
        }
    }

    String url;
    public void getFromUrl(final String url) {
        if (url!=null && !url.equals(this.url)){
            clear();
            setImageResource(R.drawable.mm_placeholder);
        }
        this.url = url;
        load();
    }

    Call call;
    public void load(){
        if (url==null) return;
        producer = GifProducer.getProducerAndSub(this,null,url);
        if (producer!=null)return;
        call =Moji.okHttpClient.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call = null;
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                call = null;
                setBytes(url,response.body().bytes());
            }
        });
    }

    @Override
    public void onAnimationUpdate(@Spanimator.Spanimation int spanimation, float progress, float min, float max) {

    }

    @Override
    public void onPaused() {
        clear();

    }

    public int hostActHash= 0;
    public boolean useKbLifecycle;
    @Override
    public void onSubscribed(int actHash) {
        if (useKbLifecycle)return;
        if (hostActHash==0) hostActHash = actHash;
        if (actHash==hostActHash)
            load();

    }

    @Override
    public void onUnsubscribed(int actHash) {
        if (useKbLifecycle)return;
        if (actHash==hostActHash)
            clear();

    }

    @Override
    public void onKbStart() {
        if (useKbLifecycle)load();
    }

    @Override
    public void onKbStop() {
       if (useKbLifecycle) clear();
    }
}