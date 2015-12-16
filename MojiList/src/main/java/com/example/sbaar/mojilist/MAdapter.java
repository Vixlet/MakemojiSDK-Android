package com.example.sbaar.mojilist;

import android.content.Context;
import android.os.Handler;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mojilib.Moji;

import org.ccil.cowan.tagsoup2.HTMLSchema;
import org.ccil.cowan.tagsoup2.Parser;

import java.util.List;

/**
 * Created by Scott Baar on 12/3/2015.
 */
public class MAdapter extends ArrayAdapter<MojiMessage> {
    Context context;
    List<MojiMessage> messages;
    private float mTextSize = -1;
    private boolean mSimple = true;
    public MAdapter (Context context, List<MojiMessage> messages, boolean simple){
        super(context,R.layout.message_item,messages);
        this.context = context;
        this.messages = messages;
        mSimple = simple;
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

            if (mTextSize== -1) mTextSize = holder.messageTV.getTextSize()/getContext().getResources().getDisplayMetrics().density;
        }
        holder = (Holder) convertView.getTag();
        if (!message.id.equals(holder.id) || mSimple!=holder.simple){
            holder.id = message.id;
            Moji.setText(message.messageRaw,holder.messageTV,mSimple);
            MainActivity.picasso.load(message.fromImg).centerCrop().resize(holder.fromIV.getMaxWidth(),holder.fromIV.getMaxHeight())
            .into(holder.fromIV);
            MainActivity.picasso.load(message.toImg).centerCrop().resize(holder.toIV.getMaxWidth(),holder.toIV.getMaxHeight())
            .into(holder.toIV);
            holder.simple = mSimple;
        }
        else if (holder.messageTV.getTextSize()!= mTextSize){
            holder.messageTV.setTextSize(mTextSize);
            Moji.setText(message.messageRaw,holder.messageTV,mSimple);
        }
        if (!mSimple){
            holder.messageTV.setGravity(position%2==0? Gravity.LEFT:Gravity.RIGHT);
        }
        else
            holder.messageTV.setGravity(Gravity.LEFT);

        return convertView;
    }

    public void changeTextSize(float increase){
        mTextSize+=increase;
        notifyDataSetChanged();
    }
    public void setSimple(boolean simple){
        mSimple = simple;
        notifyDataSetChanged();
    }
    private static class Holder{
        public TextView messageTV;
        public String id;
        public ImageView fromIV;
        public ImageView toIV;
        public boolean simple;
    }
}
