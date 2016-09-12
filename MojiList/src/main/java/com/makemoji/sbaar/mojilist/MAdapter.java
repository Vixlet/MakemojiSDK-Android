package com.makemoji.sbaar.mojilist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makemoji.mojilib.HyperMojiListener;
import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.ParsedAttributes;

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

            holder.messageTV.setTag(R.id._makemoji_hypermoji_listener_tag_id, new HyperMojiListener() {
                @Override
                public void onClick(String url) {
                    Toast.makeText(getContext(),"hypermoji clicked from adapter url " + url,Toast.LENGTH_SHORT).show();
                }
            });

            if (mTextSize== -1) mTextSize = holder.messageTV.getTextSize()/getContext().getResources().getDisplayMetrics().density;
        }
        holder = (Holder) convertView.getTag();
        if (holder.simple!=mSimple){//simple has changed, destroy cached spanned
            message.parsedAttributes = null;
        }
        if (!message.id.equals(holder.id)){
            holder.id = message.id;
            ParsedAttributes parsedAttributes = message.parsedAttributes;
            if (parsedAttributes==null) {
                parsedAttributes = Moji.parseHtml(message.messageRaw, holder.messageTV, mSimple);
                message.parsedAttributes =parsedAttributes;
            }
            Moji.setText(message.parsedAttributes.spanned,holder.messageTV);
            holder.simple = mSimple;
        }
        if (holder.messageTV.getTextSize()!= mTextSize){
            holder.messageTV.setTextSize(mTextSize);
            Moji.setText(message.messageRaw,holder.messageTV,mSimple);
        }

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
    static class Holder{
        public TextView messageTV;
        public String id;
        public ImageView fromIV;
        public ImageView toIV;
        public boolean simple;
    }
}
