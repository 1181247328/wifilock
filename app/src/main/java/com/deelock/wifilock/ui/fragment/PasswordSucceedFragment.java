package com.deelock.wifilock.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.FragmentPasswordSucceedBinding;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\27 0027.
 */

public class PasswordSucceedFragment extends Fragment {

    FragmentPasswordSucceedBinding binding;
    String password;
    String number = "";
    /**
     * 5 时段密码
     */
    private int flag;

    /**
     * 1 一次性密码
     */
    private int type;

    private long begin;
    private long end;

    private boolean operatAble = false;

    public PasswordSucceedFragment() {
    }

    @SuppressLint("ValidFragment")
    public PasswordSucceedFragment(int flag) {
        this.flag = flag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_password_succeed, container, false);
        StatusBarUtil.StatusBarLightMode(getActivity());
        doBusiness();
        requestData();
        return binding.getRoot();
    }

    private void requestData() {

    }

    public void setName(String name) {
        if (flag != 5) {
            binding.nameEt.setText(name);
        }
    }

    private void doBusiness() {
        if (flag == 5) {
            binding.nameEt.setInputType(InputType.TYPE_CLASS_PHONE);
            binding.nameEt.setHint("请输入手机号（选填）");
            binding.selectRl.setVisibility(View.VISIBLE);
            binding.selectRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                        //判断是否具有权限
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                                    20);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setData(ContactsContract.Contacts.CONTENT_URI);
                            getActivity().startActivityForResult(intent, 20);
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setData(ContactsContract.Contacts.CONTENT_URI);
                        getActivity().startActivityForResult(intent, 20);
                    }
                }
            });
        } else {
            binding.messageLl.setVisibility(View.GONE);
        }

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

        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null) {
                    event.back();
                }
            }
        });

        if (flag == 5) {
            binding.messageLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event == null) {
                        return;
                    }

                    String number = binding.nameEt.getText().toString().trim();

                    if (TextUtils.isEmpty(number) || number.length() < 11) {
                        ToastUtil.toastShort(getActivity(), "手机号格式有误");
                        return;
                    }
                    event.saveAndMessage(number);
                }
            });
        }

        binding.shareLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEt.getText().toString().trim();

                if (flag != 5) {
                    if (TextUtils.isEmpty(name)) {
                        ToastUtil.toastShort(getContext(), "昵称不能为空!");
                        return;
                    }
                } else {
                    name = "";
                }
                if (event != null) {
                    event.share(name);
                }
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEt.getText().toString().trim();

                if (flag != 5) {
                    if (TextUtils.isEmpty(name)) {
                        ToastUtil.toastShort(getContext(), "昵称不能为空!");
                        return;
                    }
                } else {
                    name = "";
                }

                if (event != null) {
                    event.save(name, false);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (number.length() > 10) {
            binding.nameEt.setText(number);
        }
    }

    public void setPhoneNumber(String number) {
        this.number = number;
    }

    public void setType(int type, long begin, long end) {
        this.type = type;
        this.begin = begin;
        this.end = end;
    }

    public void setPassword(String password) {
        binding.passwordTv.setText(password);
        this.password = password;
    }

    public void hideShareView() {
        binding.messageLl.setVisibility(View.GONE);
        binding.shareLl.setVisibility(View.GONE);
    }

    public void setPwName(String name) {
        binding.nameEt.setText(name);
    }

    public interface Event {
        void back();

        void save(String name, boolean isFollow);

        void share(String name);

        void saveAndMessage(String number);
    }

    public Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
