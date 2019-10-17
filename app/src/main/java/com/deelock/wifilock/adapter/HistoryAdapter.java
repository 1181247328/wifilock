package com.deelock.wifilock.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemHistoryDateBinding;
import com.deelock.wifilock.databinding.ItemHistoryItemBinding;
import com.deelock.wifilock.entity.LockRecord;

import java.io.File;
import java.util.List;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<LockRecord> data;
    final int ITEM = 1;
    final int DATE = 2;
    int layoutId[];
    Context context;
    String sdlId;
    String path;

    public HistoryAdapter(Context context, List<LockRecord> data, String sdlId) {
        this.context = context;
        this.data = data;
        layoutId = new int[2];
        layoutId[0] = R.layout.item_history_item;
        layoutId[1] = R.layout.item_history_date;
        this.sdlId = sdlId;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/local_cache";
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM) {
            ItemHistoryItemBinding binding1 = DataBindingUtil.inflate(inflater, layoutId[0], parent, false);
            return new ItemViewHolder(binding1);
        } else {
            ItemHistoryDateBinding binding2 = DataBindingUtil.inflate(inflater, layoutId[1], parent, false);
            return new DateViewHolder(binding2);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            if (data.get(position).getHistoryState() == 22 || data.get(position).getHistoryState() == 23) {
                ((ItemViewHolder) holder).getBinding().nameTv.setVisibility(View.VISIBLE);
                String name = data.get(position).getName();
                if (TextUtils.isEmpty(name) || name.equals("本地用户")) {
                    name = data.get(position).getOpenName();
                }
                ((ItemViewHolder) holder).getBinding().nameTv.setText(name);
            } else {
                ((ItemViewHolder) holder).getBinding().nameTv.setVisibility(View.GONE);
            }

            String url = "123";
            String authId = data.get(position).getAuthId();
            if (authId != null && authId.equals("11111111111111111111111111111111")) {
                File file = new File(path, url);
                Glide.with(context)
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.mipmap.history_head)
                        .into(((ItemViewHolder) holder).getBinding().headCiv);
            } else if (authId != null) {
                url = sdlId + authId;
                File file = new File(path, url);
                Glide.with(context)
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.mipmap.history_head)
                        .into(((ItemViewHolder) holder).getBinding().headCiv);
            }

            if (!TextUtils.isEmpty(data.get(position).getTime())) {
                ((ItemViewHolder) holder).getBinding().timeTv.setText(data.get(position).getTime().substring(10, 15));
            }
            String state = "";
            switch (data.get(position).getHistoryState()) {
                case 10:
                    state = "关门未反锁";
                    break;
                case 11:
                    state = "关门已反锁";
                    break;
                case 20:
                    state = "里面手动开锁";
                    break;
                case 21:
                    state = "钥匙开锁";
                    break;
                case 22:
                    state = "指纹开锁";
                    break;
                case 23:
                    state = "密码开锁";
                    break;
                case 30:
                    state = "异常开锁";
                    break;
                case 0:
                    state = "出厂设置";
                    break;
                case -1:
                    state = "离线";
                    break;
                default:
                    state = "开锁成功";
                    break;
            }
            ((ItemViewHolder) holder).getBinding().stateTv.setText(state);
            if (position == data.size() - 1 || data.get(position + 1).getType() == 2) {
                ((ItemViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
            } else {
                ((ItemViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
            }
            ((ItemViewHolder) holder).getBinding().deleteIb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event != null) {
                        ((ItemViewHolder) holder).getBinding().si.scroll(0);
                        event.deleteOne(position);
                    }
                }
            });
            ((ItemViewHolder) holder).getBinding().executePendingBindings();
        } else if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).getBinding().dateTv.setText(data.get(position).getTime().substring(0, 10));
            ((DateViewHolder) holder).getBinding().deleteIb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event != null) {
                        ((DateViewHolder) holder).getBinding().si.scroll(0);
                        event.deleteDay(position);
                    }
                }
            });
            ((DateViewHolder) holder).getBinding().executePendingBindings();
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

    class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryItemBinding binding;

        public ItemViewHolder(ItemHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemHistoryItemBinding getBinding() {
            return binding;
        }
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryDateBinding binding;

        public DateViewHolder(ItemHistoryDateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemHistoryDateBinding getBinding() {
            return binding;
        }
    }

    public interface Event {
        void deleteOne(int position);

        void deleteDay(int position);
    }

    public Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
