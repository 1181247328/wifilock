package com.deelock.wifilock.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.LockRecord;
import com.deelock.wifilock.overwrite.ScrollItem;

import java.util.List;

/**
 * Created by forgive for on 2018\5\21 0021.
 */
public class MagHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<LockRecord> data;
    final int ITEM = 1;
    final int DATE = 2;
    int layoutId[];
    Context context;
    private String deviceId;

    public MagHistoryAdapter(Context context, List<LockRecord> data, String deviceId) {
        this.context = context;
        this.data = data;
        layoutId = new int[2];
        layoutId[0] = R.layout.item_mag_item;
        layoutId[1] = R.layout.item_mag_date;
        this.deviceId = deviceId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM) {
            return new ItemHolder(inflater.inflate(layoutId[0], parent, false));
        } else {
            return new DateHolder(inflater.inflate(layoutId[1], parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DateHolder) {
            ((DateHolder) holder).date_tv.setText(data.get(position).getTime().substring(0, 10));
            ((DateHolder) holder).delete_ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event != null) {
                        ((DateHolder) holder).si.scroll(0);
                        event.deleteDay(position);
                    }
                }
            });
        } else {
            ((ItemHolder) holder).time_tv.setText(data.get(position).getTime().substring(10, 15));
            if (data.get(position).getHistoryState() == 10) {
                ((ItemHolder) holder).icon_iv.setImageResource(R.mipmap.mag_item_green);
                if ("B002".equals(deviceId.substring(0, 4))) {
                    ((ItemHolder) holder).state_tv.setText("有人通过");
                } else {
                    ((ItemHolder) holder).state_tv.setText("关闭");
                }
            } else {
                ((ItemHolder) holder).icon_iv.setImageResource(R.mipmap.mag_item_red);
                if ("B002".equals(deviceId.substring(0, 4))) {
                    ((ItemHolder) holder).state_tv.setText("有人通过");
                } else {
                    ((ItemHolder) holder).state_tv.setText("打开");
                }
            }

            if (position == data.size() - 1 || data.get(position + 1).getType() == 2) {
                ((ItemHolder) holder).divider_v.setVisibility(View.GONE);
            } else {
                ((ItemHolder) holder).divider_v.setVisibility(View.VISIBLE);
            }

            ((ItemHolder) holder).delete_ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event != null) {
                        ((ItemHolder) holder).si.scroll(0);
                        event.deleteOne(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getType() == DATE) {
            return DATE;
        }
        return ITEM;
    }

    public class DateHolder extends RecyclerView.ViewHolder {
        ScrollItem si;
        TextView date_tv;
        ImageButton delete_ib;

        public DateHolder(View itemView) {
            super(itemView);
            si = itemView.findViewById(R.id.si);
            date_tv = itemView.findViewById(R.id.date_tv);
            delete_ib = itemView.findViewById(R.id.delete_ib);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ScrollItem si;
        ImageView icon_iv;
        TextView time_tv;
        TextView state_tv;
        ImageButton delete_ib;
        View divider_v;

        public ItemHolder(View itemView) {
            super(itemView);
            si = itemView.findViewById(R.id.si);
            icon_iv = itemView.findViewById(R.id.icon_iv);
            time_tv = itemView.findViewById(R.id.time_tv);
            state_tv = itemView.findViewById(R.id.state_tv);
            delete_ib = itemView.findViewById(R.id.delete_ib);
            divider_v = itemView.findViewById(R.id.divider_v);
        }
    }

    public interface Event {
        void deleteOne(int position);

        void deleteDay(int position);
    }

    public HistoryAdapter.Event event;

    public void setEvent(HistoryAdapter.Event event) {
        this.event = event;
    }
}
