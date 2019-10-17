package com.deelock.wifilock.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;

/**
 * Created by binChuan on 2017\10\16 0016.
 */

public class VerifyManagerFragment extends Fragment {

    View rootView;

    ImageButton back_ib;
    TextView title_tv;
    TextView notice_tv;
    ImageView image_iv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_verify_manager, container, false);
        doBusiness();
        return rootView;
    }

    private void doBusiness() {
        back_ib = (ImageButton) rootView.findViewById(R.id.back_ib);
        title_tv = (TextView) rootView.findViewById(R.id.title_tv);
        notice_tv = (TextView) rootView.findViewById(R.id.notice_tv);
        image_iv = (ImageView) rootView.findViewById(R.id.image_iv);

//        title_tv.setText("连接门锁");
//        notice_tv.setText("长按门锁显示屏“C”键3秒后，\n按语音提示进行操作");
//        image_iv.setImageResource(R.mipmap.link_lock_bg);

        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.back();
                }
            }
        });
    }

    public interface Event{
        void back();
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
