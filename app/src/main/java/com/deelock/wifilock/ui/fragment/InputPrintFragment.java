package com.deelock.wifilock.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.deelock.wifilock.R;
import com.deelock.wifilock.overwrite.PrintView;

/**
 * Created by binChuan on 2017\10\24 0024.
 */

public class InputPrintFragment extends Fragment {

    View rootView;

    private PrintView print;
    private ImageButton back_ib;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_input_print, container, false);
        doBusiness();
        return rootView;
    }

    private void doBusiness() {
        print = (PrintView) rootView.findViewById(R.id.print);
        back_ib = (ImageButton) rootView.findViewById(R.id.back_ib);

        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event.back();
            }
        });
    }

    public void setStep(int step){
        if (print == null){
            return;
        }
        print.setStep(step);
    }

    public interface Event{
        void back();
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
