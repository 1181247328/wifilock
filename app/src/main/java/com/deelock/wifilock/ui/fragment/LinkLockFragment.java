package com.deelock.wifilock.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.utils.DensityUtil;

import java.lang.ref.WeakReference;

/**
 * Created by binChuan on 2017\9\26 0026.
 */

public class LinkLockFragment extends Fragment {

    View rootView;

    ImageButton back_ib;
    CheckBox checkbox_cb;
    TextView title_tv;
    TextView notice_tv;
    ImageView image_iv;
    Button configure_ib;
    ImageView hand_iv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_link, container, false);
        doBusiness();
        return rootView;
    }

    private void doBusiness() {
        back_ib = (ImageButton) rootView.findViewById(R.id.back_ib);
        checkbox_cb = (CheckBox) rootView.findViewById(R.id.checkbox_cb);
        title_tv = (TextView) rootView.findViewById(R.id.title_tv);
        notice_tv = (TextView) rootView.findViewById(R.id.notice_tv);
        image_iv = (ImageView) rootView.findViewById(R.id.image_iv);
        configure_ib = (Button) rootView.findViewById(R.id.configure_ib);
        hand_iv = (ImageView) rootView.findViewById(R.id.hand_iv);

        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.back();
                }
            }
        });

        configure_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = 0;
                if (checkbox_cb.isChecked()){
                    type = 1;
                }
                if (event != null){
                    event.configure(type);
                }
            }
        });
    }

    public void startAnima(){
        hand_iv.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int width = DensityUtil.getScreenWidth(getActivity());
                    int height = DensityUtil.getScreenHeight(getActivity());
                    float offX = 463 * width / 750;
                    float offY = 416 * height / 1334;
                    final TranslateAnimation animation = new TranslateAnimation(-offX, 0, offY, 0);
                    animation.setDuration(800);
                    hand_iv.setVisibility(View.VISIBLE);
                    hand_iv.startAnimation(animation);
                } catch (Exception e){

                }
            }
        }, 300);
    }

    public void limitView(int size, int manager){
        if (size == 0){
            checkbox_cb.setChecked(true);
            checkbox_cb.setClickable(false);
//            checkbox_cb.setEnabled(false);
            return;
        }

        if (manager > 1){
            checkbox_cb.setChecked(false);
            checkbox_cb.setClickable(false);
//            checkbox_cb.setEnabled(false);
        }
    }

    public interface Event{
        void back();
        void configure(int type);
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
