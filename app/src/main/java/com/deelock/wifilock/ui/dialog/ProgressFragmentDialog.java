package com.deelock.wifilock.ui.dialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2017\11\27 0027.
 */

public class ProgressFragmentDialog extends DialogFragment {

    private String text;
    private ProgressDialog pd;

    public void setText(String text) {
        this.text = text;
    }

    public ProgressFragmentDialog(String text){
        this.text = text;
    }

    @NonNull
    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        pd = new ProgressDialog(getActivity(), R.style.lightProgressDialog);
        pd.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(text)){
            pd.setMessage(text);
        } else {
            pd.setMessage("连接中，请等待...");
        }
        return pd;
    }

    public void setContent(String text){
        if (pd == null){
            return;
        }
        pd.setMessage(text);
    }

    public void disMiss(){
        pd.dismiss();
        dismiss();
    }
}
