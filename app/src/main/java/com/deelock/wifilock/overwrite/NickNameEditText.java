package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.EditText;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2017\11\30 0030.
 */

public class NickNameEditText extends EditText {

    public NickNameEditText(Context context) {
        this(context, null);
    }

    public NickNameEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NickNameEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) &&
                                    !Character.toString(source.charAt(i)).equals("_")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(16)
        });
    }
}
