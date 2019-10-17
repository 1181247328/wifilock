package com.deelock.wifilock.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.Message;
import com.deelock.wifilock.utils.SPUtil;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    int layoutId;
    Handler handler;
    List<Message> data;
    SimpleDateFormat sf;
    private Context context;

    public MessageAdapter(List<Message> data) {
        layoutId = R.layout.item_message;
        handler = new Handler();
        this.data = data;
        sf = new SimpleDateFormat("HH:mm  MM/dd");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = new ViewHolder(inflater.inflate(layoutId, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.time_tv.setText(sf.format(data.get(position).getTimeUpdate() * 1000l));
        Message message = data.get(position);
        String nickname = SPUtil.getSdlName(context, message.getDeviceId());
        if (TextUtils.isEmpty(nickname)) {
            nickname = SPUtil.getSdlName(context, message.getDevId());
        }
        switch (data.get(position).getType()) {
            case 216:
                switch (data.get(position).getDealtCode()) {
                    case 10001004:
                        holder.data_tv.setText(nickname + ":物业处理-多次指纹输错");
                        break;
                    case 10001002:
                    case 10001003:
                        holder.data_tv.setText(nickname + ":物业处理-多次密码输错");
                        break;
                    case 10001005:
                        holder.data_tv.setText(nickname + ":物业处理-暴力撬锁");
                        break;
                    default:
                        holder.data_tv.setText(nickname + ":物业处理-防尾随");
                        break;
                }
                holder.icon_iv.setImageResource(R.mipmap.message_lock);
                break;
            case 215:
                holder.data_tv.setText(nickname + ":异常开锁");
                holder.icon_iv.setImageResource(R.mipmap.message_lock);
                break;
            case 214:
                holder.data_tv.setText(nickname + ":暴力开锁");
                holder.icon_iv.setImageResource(R.mipmap.message_lock);
                break;
            case 213:
                holder.data_tv.setText(nickname + ":设备已离线");
                holder.icon_iv.setImageResource(R.mipmap.message_offline);
                break;
            case 212:
                holder.data_tv.setText(nickname + ":密码多次输入错误");
                holder.icon_iv.setImageResource(R.mipmap.message_password);
                break;
            case 211:
                holder.data_tv.setText(nickname + ":指纹多次输入错误");
                holder.icon_iv.setImageResource(R.mipmap.message_print);
                break;
            case 301:
            case 201:
                holder.data_tv.setText(nickname + ":设备低电量");
                holder.icon_iv.setImageResource(R.mipmap.message_lock);
                break;
            case 311:
                holder.data_tv.setText(nickname + ":警戒模式开门");
                break;
            case 312:
                holder.data_tv.setText(nickname + ":忘关提醒");
            default:
                holder.icon_iv.setImageResource(R.mipmap.message_password);
                break;
        }
        holder.ensure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItemCount() <= position) {
                    return;
                }
                String id = data.get(position).getAlertId();
                data.remove(position);
                notifyItemRemoved(position);
                //必须刷新数据，否则会出现指针异常
                if (position != data.size()) {
                    notifyItemRangeChanged(position, 1);
                }
                if (event != null) {
                    event.delete(id);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon_iv;
        TextView time_tv;
        TextView data_tv;
        TextView ensure_tv;

        public ViewHolder(View itemView) {
            super(itemView);
            icon_iv = itemView.findViewById(R.id.icon_iv);
            time_tv = itemView.findViewById(R.id.time_tv);
            data_tv = itemView.findViewById(R.id.data_tv);
            ensure_tv = itemView.findViewById(R.id.ensure_tv);
        }
    }

    public interface Event {
        void delete(String id);
    }

    Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
