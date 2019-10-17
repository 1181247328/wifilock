package com.deelock.wifilock.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;

import java.lang.ref.WeakReference;

/**
 * Created by forgive for on 2017\11\27 0027.
 */

public class DeleteFragmentDialog extends DialogFragment {

    private ImageView icon_iv;
    private TextView notice_tv;
    private TextView cancel_tv;
    private TextView ensure_tv;

    public interface DialogEvent{
        void onSure();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete, null);
        builder.setView(view);
        icon_iv = view.findViewById(R.id.icon_iv);
        notice_tv = view.findViewById(R.id.notice_tv);
        cancel_tv = view.findViewById(R.id.cancel_tv);
        ensure_tv = view.findViewById(R.id.ensure_tv);
        icon_iv.setImageResource(R.mipmap.nick_name_icon);
        notice_tv.setText("设置昵称失败！");

        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ensure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogEvent event = (DialogEvent) new WeakReference<Activity>(getActivity()).get();
                if (event != null){
                    event.onSure();
                }
            }
        });

        return builder.create();
    }
}
