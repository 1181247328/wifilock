package com.deelock.wifilock.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.FragmentInputInfoBinding;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\26 0026.
 */

public class InputInfoFragment extends BaseFragment {

    FragmentInputInfoBinding binding;

    String nickName;
    String phoneNumber;
    private String number = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_input_info, container, false);
        StatusBarUtil.StatusBarLightMode(getActivity());
        doBusiness();
        return binding.getRoot();
    }

    private void doBusiness() {
        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        binding.contactsRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                    //判断是否具有权限
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                                30);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setData(ContactsContract.Contacts.CONTENT_URI);
                        getActivity().startActivityForResult(intent, 30);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(ContactsContract.Contacts.CONTENT_URI);
                    getActivity().startActivityForResult(intent, 30);
                }
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickName = binding.nickNameEt.getText().toString();
                phoneNumber = binding.phoneNumberEt.getText().toString();
                String num = "[1]\\d{10}";
                if (TextUtils.isEmpty(nickName)){
                    ToastUtil.toastShort(getContext(), "请输入新用户昵称");
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)){
                    event.next(binding.nickNameEt.getText().toString(), null);
                    return;
                }
                if (!phoneNumber.matches(num)){
                    ToastUtil.toastShort(getContext(), "手机号码格式不正确");
                    return;
                }
                event.next(binding.nickNameEt.getText().toString(), binding.phoneNumberEt.getText().toString());
            }
        });
    }

    public void setPhoneNumber(String number){
        this.number = number;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (number.length() > 10){
            binding.phoneNumberEt.setText(number);
        }
    }

    public interface Event{
        void next(String name, String number);
    }

    public Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
