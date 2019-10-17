package com.deelock.wifilock.ui.fragment;

import android.content.res.AssetManager;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.FragmentInputPasswordBinding;
import com.deelock.wifilock.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\27 0027.
 */

public class InputPasswordFragment extends BaseFragment {

    FragmentInputPasswordBinding binding;
    List<Button> buttons;
    char[] password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_input_password, container, false);
        StatusBarUtil.StatusBarLightMode(getActivity());
        doBusiness();
        return binding.getRoot();
    }

    private void doBusiness() {
        password = new char[6];

        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 6; i++) {
                    if (password[i] == '\0') {
                        return;
                    }
                }
                if (event != null){
                    event.next(String.valueOf(password));
                    password = new char[]{'\0', '\0', '\0', '\0', '\0', '\0'};
                    binding.passwordPa.setText(password);
                }
            }
        });

        buttons = new ArrayList<>();
        buttons.add(binding.button1Btn);
        buttons.add(binding.button2Btn);
        buttons.add(binding.button3Btn);
        buttons.add(binding.button4Btn);
        buttons.add(binding.button5Btn);
        buttons.add(binding.button6Btn);
        buttons.add(binding.button7Btn);
        buttons.add(binding.button8Btn);
        buttons.add(binding.button9Btn);
        buttons.add(binding.button0Btn);
        buttons.add(binding.buttonCBtn);

        AssetManager mgr = getContext().getAssets();

//        根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/BAUHAUS_0.TTF");

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.buttonC_btn){
                    password = new char[]{'\0', '\0', '\0', '\0', '\0', '\0'};
                    binding.passwordPa.setText(password);
                    return;
                }
                for (int i = 0; i < 6; i++){
                    if (password[i] != '\0'){
                        continue;
                    }

                    switch (v.getId()){
                        case R.id.button0_btn:
                            password[i] = '0';
                            break;
                        case R.id.button1_btn:
                            password[i] = '1';
                            break;
                        case R.id.button2_btn:
                            password[i] = '2';
                            break;
                        case R.id.button3_btn:
                            password[i] = '3';
                            break;
                        case R.id.button4_btn:
                            password[i] = '4';
                            break;
                        case R.id.button5_btn:
                            password[i] = '5';
                            break;
                        case R.id.button6_btn:
                            password[i] = '6';
                            break;
                        case R.id.button7_btn:
                            password[i] = '7';
                            break;
                        case R.id.button8_btn:
                            password[i] = '8';
                            break;
                        case R.id.button9_btn:
                            password[i] = '9';
                            break;
                        default:
                            break;
                    }
                    binding.passwordPa.setText(password);
                    return;
                }
            }
        };

        for (int i = 0; i < buttons.size(); i++){
            buttons.get(i).setTypeface(tf);
            buttons.get(i).setOnClickListener(listener);
        }

        binding.buttonXBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 5; i > -1; i--){
                    if (password[i] == '\0'){
                        continue;
                    }
                    password[i] = '\0';
                    binding.passwordPa.setText(password);
                    return;
                }
            }
        });
    }

    public interface Event{
        void next(String password);
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
