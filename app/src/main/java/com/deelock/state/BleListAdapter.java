package com.deelock.state;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.utils.BluetoothUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ble集合适配器
 */
public class BleListAdapter extends BaseAdapter {

    private String TAG = BleListAdapter.class.getSimpleName();

    private Context context = null;

    private LockState lockState = LockState.getLockState();

    public BleListAdapter(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int getCount() {
        return BluetoothUtil.scanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlantViewHolder plantViewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_blelist, null);
            plantViewHolder = new PlantViewHolder(convertView);
            convertView.setTag(plantViewHolder);
        } else {
            plantViewHolder = (PlantViewHolder) convertView.getTag();
        }
        //距离
        plantViewHolder.tvBleItemDistance.setText(BluetoothUtil.scanResults.get((getCount()-1) - position).getRssi() + "");
        //名称
        plantViewHolder.tvBleItemName.setText(BluetoothUtil.scanResults.get((getCount()-1) - position).getDevice().getName());
        //地址
        plantViewHolder.tvBleItemAddress.setText(BluetoothUtil.scanResults.get((getCount()-1) - position).getDevice().getAddress());
        return convertView;
    }

    class PlantViewHolder {

        //距离
        @BindView(R.id.tv_ble_item_distance)
        public TextView tvBleItemDistance = null;

        //名称
        @BindView(R.id.tv_ble_item_name)
        public TextView tvBleItemName = null;

        //地址
        @BindView(R.id.tv_ble_item_address)
        public TextView tvBleItemAddress = null;

        public PlantViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
