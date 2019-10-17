package com.deelock.wifilock.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.FragmentPrintSucceedBinding;
import com.deelock.wifilock.event.PrintNameEvent;

/**
 * Created by binChuan on 2017\10\24 0024.
 */

public class PrintSucceedFragment extends Fragment {

    FragmentPrintSucceedBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_print_succeed, container, false);
        doBusiness();
        return binding.getRoot();
    }

    private void doBusiness() {
        binding.nameEt.setFilters(new InputFilter[]{
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

        binding.setEvent(new PrintNameEvent() {
            @Override
            public void rightThumb() {
                binding.nameEt.setText("右手拇指");
            }

            @Override
            public void rightIndex() {
                binding.nameEt.setText("右手食指");
            }

            @Override
            public void rightMiddle() {
                binding.nameEt.setText("右手中指");
            }

            @Override
            public void leftThumb() {
                binding.nameEt.setText("左手拇指");
            }

            @Override
            public void leftIndex() {
                binding.nameEt.setText("左手食指");
            }

            @Override
            public void leftMiddle() {
                binding.nameEt.setText("左手中指");
            }

            @Override
            public void save() {
                String name = binding.nameEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)){
                    return;
                }
                if (event != null){
                    event.save(name);
                }
            }
        });
    }

    public interface Event{
        void save(String name);
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
