package com.deelock.wifilock.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemUserBinding;
import com.deelock.wifilock.databinding.ItemUserShareBinding;
import com.deelock.wifilock.databinding.ItemUserTailBinding;
import com.deelock.wifilock.databinding.ItemUserTitleBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.PasswordDetailActivity;
import com.deelock.wifilock.ui.dialog.InputPhoneDialog;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<UserPassword> passwordList;
    private List<UserFPrint> fPrintList;
    private List<User> userList;
    private String sdlId;
    private String authId;
    private int layoutId[];
    private final int TITLE = 1;
    private final int USER = 2;
    private final int TAIL = 3;
    private final int Share = 4;
    private int isPush = 1;  //开门提醒 1打开 0关闭
    Map params;
    private int marginX120;
    private int marginX60;

    private int isBtopen = -1;  //蓝牙钥匙分享 1打开 0关闭

    private SwitchCompat sc;
    private SwitchCompat userShareSwitch;
    private RelativeLayout userShareRl;

    private InputPhoneDialog inputPhoneDialog;
    private String phoneNumber;

    private int authUid = -1;  //授权用户的id;

    public UserAdapter(final Context context, List<User> userList, List<UserPassword> passwordList, List<UserFPrint> fPrintList,
                       String sdlId, int isPush, String authId, String phoneNumber) {
        this.context = context;
        this.userList = userList;
        this.passwordList = passwordList;
        this.fPrintList = fPrintList;
        this.sdlId = sdlId;
        this.phoneNumber = phoneNumber;
        layoutId = new int[4];
        layoutId[0] = R.layout.item_user_title;
        layoutId[1] = R.layout.item_user;
        layoutId[2] = R.layout.item_user_tail;
        layoutId[3] = R.layout.item_user_share;
        this.isPush = isPush;
        this.authId = authId;
        params = new HashMap();
        marginX120 = DensityUtil.getScreenWidth(context) * 120 / 750;
        marginX60 = DensityUtil.getScreenWidth(context) * 60 / 750;
        inputPhoneDialog = new InputPhoneDialog(context, R.style.dialog);
        inputPhoneDialog.setEvent(new InputPhoneDialog.Event() {
            @Override
            public void ensure(String phone) {
                if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                    Toast.makeText(context, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                } else {
                    inputPhoneDialog.dismiss();
                    updateShareUserInfo(phone, (isBtopen + 1) % 2);
                }
            }
        });
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            params.clear();
            params.put("timestamp", TimeUtil.getTime());
            params.put("uid", SPUtil.getUid(context));
            params.put("sdlId", sdlId);
            params.put("pid", authId);
            if (isPush == 1) {
                params.put("isPush", 0);
            } else {
                params.put("isPush", 1);
            }
            RequestUtils.request(RequestUtils.UPDATE_USER, context, params).enqueue(
                    new ResponseCallback<BaseResponse>((Activity) context) {
                        @Override
                        protected void onSuccess(int code, String content) {
                            super.onSuccess(code, content);
                            if (isPush == 1) {
                                isPush = 0;
                                sc.setChecked(false);
                            } else {
                                isPush = 1;
                                if (sc != null) {
                                    sc.setChecked(true);
                                }
                            }
                        }
                    }
            );
        }
    };

    /**
     * 更新需要分享的用户信息
     */
    private void updateShareUserInfo(final String phone, final int isBt) {
        params.clear();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("sdlId", sdlId);
        params.put("pid", authId);
        if (!TextUtils.isEmpty(phone)) {
            if (phone.equals(SPUtil.getStringData(context, "phoneNumber"))) {
                Toast.makeText(context, "请勿输入当前主用户手机号码", Toast.LENGTH_SHORT).show();
                return;
            } else {
                for (int i = 0; i < userList.size(); i++) {
                    if (phone.equals(userList.get(i).getPhoneNumber()) && TextUtils.isEmpty(phoneNumber)) {
                        Toast.makeText(context, "该手机号已被" + userList.get(i).getName() + "使用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                params.put("phoneNumber", phone);
            }
        }
        params.put("isBtopen", isBt);
        RequestUtils.request(RequestUtils.BLE_UPDATE_AUTH_USER, context, params).enqueue(
                new ResponseCallback<BaseResponse>((Activity) context) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        phoneNumber = phone;
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        try {
                            Integer uid = jsonObject.getIntValue("authUid");
                            setAuthUid(uid == 0 ? -1 : uid);
                        } catch (Exception e) {
                            setAuthUid(-1);
                        }

                        if (isBtopen == 1) {
                            isBtopen = 0;
                            userShareSwitch.setChecked(false);
                        } else {
                            isBtopen = 1;
                            if (userShareSwitch != null) {
                                userShareSwitch.setChecked(true);
                            }
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        if ("用户不存在".equals(message)) {
                            inputPhoneDialog.show();
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void setAuthUid(int uid) {
        authUid = uid;
    }

    public int getRefreshAuthUid() {
        return authUid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TITLE) {
            ItemUserTitleBinding binding = DataBindingUtil.inflate(inflater, layoutId[0], parent, false);
            return new TitleViewHolder(binding);
        } else if (viewType == USER) {
            ItemUserBinding binding = DataBindingUtil.inflate(inflater, layoutId[1], parent, false);
            return new UserViewHolder(binding);
        } else if (viewType == TAIL) {
            ItemUserTailBinding binding = DataBindingUtil.inflate(inflater, layoutId[2], parent, false);
            return new UserTailViewHolder(binding);
        } else {
            ItemUserShareBinding binding = DataBindingUtil.inflate(inflater, layoutId[3], parent, false);
            return new UserShareViewHolder(binding);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TitleViewHolder) {
            if (position == 0) {
                if (fPrintList == null || fPrintList.size() == 0) {
                    ((TitleViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
                } else {
                    ((TitleViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
                }

                ((TitleViewHolder) holder).getBinding().addIb.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (authId.equals("00000000000000000000000000000000")) {
                                    ToastUtil.toastLong(context, "本地用户请操作门锁添加");
                                } else {
                                    if (event != null) {
                                        event.addPrint(position);
                                    }
                                }
                            }
                        }
                );

                ((TitleViewHolder) holder).getBinding().iconIv.setImageResource(R.mipmap.user_manage_lock);
                ((TitleViewHolder) holder).getBinding().nameTv.setText("指纹管理");
            } else {
                if (passwordList == null || passwordList.size() == 0) {
                    ((TitleViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
                } else {
                    ((TitleViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
                }
                ((TitleViewHolder) holder).getBinding().addIb.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (authId.equals("00000000000000000000000000000000")) {
                                    ToastUtil.toastLong(context, "本地用户请操作门锁添加");
                                } else {
                                    if (event != null) {
                                        event.addPassword(position);
                                    }
                                }
                            }
                        }
                );
                ((TitleViewHolder) holder).getBinding().iconIv.setImageResource(R.mipmap.user_manage_print);
                ((TitleViewHolder) holder).getBinding().nameTv.setText("密码管理");
            }
            ((TitleViewHolder) holder).getBinding().executePendingBindings();
        } else if (holder instanceof UserViewHolder) {
            if (fPrintList != null && position < fPrintList.size() + 1) {
                ((UserViewHolder) holder).getBinding().nameTv.setText(
                        fPrintList.get(position - 1).getOpenName());
                if (fPrintList.get(position - 1).getType() == 1) {
                    ((UserViewHolder) holder).getBinding().managerIv.setVisibility(View.VISIBLE);
                } else {
                    ((UserViewHolder) holder).getBinding().managerIv.setVisibility(View.GONE);
                }

                if (fPrintList.size() > 0 && position == fPrintList.size()) {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((UserViewHolder) holder).getBinding().dividerV.getLayoutParams();
                    lp.leftMargin = marginX60;
                    ((UserViewHolder) holder).getBinding().dividerV.setLayoutParams(lp);
                } else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ((UserViewHolder) holder).getBinding().dividerV.getLayoutParams();
                    lp.leftMargin = marginX120;
                    ((UserViewHolder) holder).getBinding().dividerV.setLayoutParams(lp);
                }

                ((UserViewHolder) holder).itemView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean isOpen = false;
                                for (int i = 0; i < fPrintList.size(); i++) {
                                    if (fPrintList.get(i).getIsSecurityHelp() == 1) {
                                        if (i == position - 1) {
                                            break;
                                        }
                                        isOpen = true;
                                        break;
                                    }
                                }
                                Intent intent = new Intent(context, PasswordDetailActivity.class);
                                intent.putExtra("flag", 1);
                                intent.putExtra("sdlId", sdlId);
                                intent.putExtra("UserFPrint", fPrintList.get(position - 1));
                                intent.putExtra("isOpen", isOpen);
                                context.startActivity(intent);
                            }
                        }
                );
            } else {
                final int p;
                if (fPrintList == null) {
                    p = position - 2;
                } else {
                    p = position - 2 - fPrintList.size();
                }
                ((UserViewHolder) holder).getBinding().nameTv.setText(passwordList.get(p).getOpenName());

                if (passwordList.size() > 0 && p == passwordList.size() - 1) {
                    ((UserViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
                } else {
                    ((UserViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
                }

                if (passwordList.get(p).getType() == 1) {
                    ((UserViewHolder) holder).getBinding().managerIv.setVisibility(View.VISIBLE);
                }
                if (passwordList.get(p).getType() != 1) {
                    ((UserViewHolder) holder).getBinding().managerIv.setVisibility(View.GONE);
                }

                ((UserViewHolder) holder).itemView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean isOpen = false;
                                for (int i = 0; i < passwordList.size(); i++) {
                                    if (passwordList.get(i).getIsSecurityHelp() == 1) {
                                        if (i == p) {
                                            break;
                                        }
                                        isOpen = true;
                                        break;
                                    }
                                }
                                Intent intent = new Intent(context, PasswordDetailActivity.class);
                                intent.putExtra("flag", 2);
                                intent.putExtra("sdlId", sdlId);
                                intent.putExtra("UserPassword", passwordList.get(p));
                                intent.putExtra("isOpen", isOpen);
                                intent.putParcelableArrayListExtra("UserPasswords", (ArrayList<? extends Parcelable>) passwordList);
                                context.startActivity(intent);
                            }
                        }
                );
            }
            ((UserViewHolder) holder).getBinding().executePendingBindings();
        } else if (holder instanceof UserTailViewHolder) {
            if (isPush == 1) {
                ((UserTailViewHolder) holder).getBinding().switchSc.setChecked(true);
            } else {
                ((UserTailViewHolder) holder).getBinding().switchSc.setChecked(false);
            }

            sc = ((UserTailViewHolder) holder).getBinding().switchSc;
            ((UserTailViewHolder) holder).getBinding().switchSc.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        new Thread(r).start();
                    }
                    return true;
                }
            });
            ((UserTailViewHolder) holder).getBinding().executePendingBindings();
        } else {
            userShareRl = ((UserShareViewHolder) holder).getBinding().userBleShareItem;
            userShareSwitch = ((UserShareViewHolder) holder).getBinding().userBleShare;
            userShareSwitch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (TextUtils.isEmpty(phoneNumber)) {
                            inputPhoneDialog.show();
                        } else {
                            updateShareUserInfo(phoneNumber, (isBtopen + 1) % 2);
                        }
                    }
                    return true;
                }
            });
            ((UserShareViewHolder) holder).getBinding().executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        int size = 4;
        if (passwordList != null) {
            size += passwordList.size();
        }
        if (fPrintList != null) {
            size += fPrintList.size();
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TITLE;
        }
        if (fPrintList != null && position == fPrintList.size() + 1) {
            return TITLE;
        }
        if (fPrintList == null && position == 1) {
            return TITLE;
        }
        if (position == getItemCount() - 2) {
            return TAIL;
        }
        if (position == getItemCount() - 1) {
            return Share;
        }
        return USER;
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        private ItemUserTitleBinding binding;

        public TitleViewHolder(ItemUserTitleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserTitleBinding getBinding() {
            return binding;
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserBinding getBinding() {
            return binding;
        }
    }

    class UserTailViewHolder extends RecyclerView.ViewHolder {
        private ItemUserTailBinding binding;

        public UserTailViewHolder(ItemUserTailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserTailBinding getBinding() {
            return binding;
        }
    }

    class UserShareViewHolder extends RecyclerView.ViewHolder {
        private ItemUserShareBinding binding;

        UserShareViewHolder(ItemUserShareBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserShareBinding getBinding() {
            return binding;
        }
    }

    public interface Event {
        void addPrint(int position);

        void addPassword(int position);
    }

    public void setShowShare(boolean show) {
        userShareRl.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setIsBtopen(int isBtopen) {
        this.isBtopen = isBtopen;
        userShareSwitch.setChecked(isBtopen == 1);
    }

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }

}
