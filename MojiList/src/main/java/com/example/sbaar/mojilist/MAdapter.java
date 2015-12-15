package com.example.sbaar.mojilist;

import android.content.Context;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ccil.cowan.tagsoup2.HTMLSchema;
import org.ccil.cowan.tagsoup2.Parser;

import java.util.List;

/**
 * Created by DouglasW on 12/3/2015.
 */
public class MAdapter extends ArrayAdapter<MojiMessage> {
    Context context;
    List<MojiMessage> messages;
    Parser parser = new Parser();
    Handler handler = new Handler();
    public MAdapter (Context context, List<MojiMessage> messages){
        super(context,R.layout.message_item,messages);
        this.context = context;
        this.messages = messages;
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        }catch (Exception e){}

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Holder holder;
        MojiMessage message = getItem(position);
        if (convertView==null){
           convertView = LayoutInflater.from(context).inflate(R.layout.message_item,parent,false);
            holder = new Holder();
            holder.messageTV = (TextView)convertView.findViewById(R.id.item_message_tv);
            holder.fromIV = (ImageView) convertView.findViewById(R.id.from_iv);
            holder.toIV = (ImageView) convertView.findViewById(R.id.to_iv);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        if (!message.id.equals(holder.id)){
            holder.id = message.id;
            Spanned spanned = new SpanBuilder(message.messageRaw,null,null,parser,holder.messageTV).convert();
            holder.messageTV.setText(spanned);
            MainActivity.picasso.load(message.fromImg).centerCrop().resize(holder.fromIV.getMaxWidth(),holder.fromIV.getMaxHeight())
            .into(holder.fromIV);
            MainActivity.picasso.load(message.toImg).centerCrop().resize(holder.toIV.getMaxWidth(),holder.toIV.getMaxHeight())
            .into(holder.toIV);
        }
        else
        {
            holder.messageTV.postInvalidate();
        }
      /*  Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        });
       builder.build().load("http://s3.amazonaws.com/me-source/emoji/22@2x.png").error(R.drawable.unknown_image).
                into(holder.fromIV);*/
        return convertView;
    }

    private static class Holder{
        public TextView messageTV;
        public String id;
        public ImageView fromIV;
        public ImageView toIV;
    }
}
