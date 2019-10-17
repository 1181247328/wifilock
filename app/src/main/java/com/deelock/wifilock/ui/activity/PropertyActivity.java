package com.deelock.wifilock.ui.activity;

import android.annotation.SuppressLint;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.PropertyInfo;
import com.deelock.wifilock.presenter.PropertyPresenter;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.view.IPropertyView;

/**
 * Created by forgive for on 2018\5\3 0003.
 */
public class PropertyActivity extends BaseActivity implements IPropertyView {

    //widget
    private ImageButton back_ib;
    private TextView modify_tv;
    private EditText name_et;
    private TextView city_tv;
    private TextView community_tv;
    private EditText address_et;
    private EditText phone_number_et;
    private TextView state_tv;
    private TextView message_tv;
    private View divider_v;
    private RelativeLayout apply_rl;
    private TextView apply_tv;
    private ImageView cancel_iv;
    private RelativeLayout switch_rl;
    private SwitchCompat switch_sc;
    private View number_v;

    private PropertyPresenter presenter;
    private boolean dialogEnable = true;
    private int margin60;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_property);
    }

    @Override
    protected void findView() {
        back_ib = f(R.id.back_ib);
        modify_tv = f(R.id.modify_tv);
        name_et = f(R.id.name_et);
        city_tv = f(R.id.city_tv);
        community_tv = f(R.id.community_tv);
        address_et = f(R.id.address_et);
        phone_number_et = f(R.id.phone_number_et);
        state_tv = f(R.id.state_tv);
        message_tv = f(R.id.message_tv);
        divider_v = f(R.id.divider_v);
        apply_rl = f(R.id.apply_rl);
        apply_tv = f(R.id.apply_tv);
        cancel_iv = f(R.id.cancel_iv);
        switch_rl = f(R.id.switch_rl);
        switch_sc = f(R.id.switch_sc);
        number_v = f(R.id.number_v);
    }

    @Override
    protected void doBusiness() {
        String sdlId = getIntent().getStringExtra("sdlId");
        int isProsHelp = getIntent().getIntExtra("isProsHelp", 0);
        if (isProsHelp == 1){
            switch_sc.setEnabled(true);
            switch_sc.setChecked(true);
        }
        presenter = new PropertyPresenter(this, this, sdlId, isProsHelp);
        margin60 = DensityUtil.getScreenWidth(this) * 60 / 750;
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        modify_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notOpen();
            }
        });

        city_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogEnable){
                    hideInputKeyboard();
                    presenter.selectCity();
                }
            }
        });

        community_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogEnable){
                    hideInputKeyboard();
                    presenter.selectCommunity();
                }
            }
        });

        apply_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_et.getText().toString().trim();
                String address = address_et.getText().toString().trim();
                String number = phone_number_et.getText().toString().trim();
                presenter.apply(name, address, number);
            }
        });

        cancel_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.cancelApply();
            }
        });

        switch_sc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.switchPros(switch_sc.isChecked());
            }
        });
    }

    @Override
    public void notOpen(){
        modify_tv.setVisibility(View.GONE);
        setEditTextEditable(name_et);
        dialogEnable = true;
        setEditTextEditable(address_et);
        setEditTextEditable(phone_number_et);
        switch_rl.setVisibility(View.GONE);
        state_tv.setVisibility(View.GONE);
        message_tv.setVisibility(View.GONE);
        divider_v.setVisibility(View.VISIBLE);
        apply_rl.setVisibility(View.VISIBLE);
        apply_tv.setText("申请开通");
        cancel_iv.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) number_v.getLayoutParams();
        lp.leftMargin = 0;
        number_v.setLayoutParams(lp);
    }

    @Override
    public void opening(){
        modify_tv.setVisibility(View.GONE);
        setEditTextNotEditable(name_et);
        dialogEnable = false;
        setEditTextNotEditable(address_et);
        setEditTextNotEditable(phone_number_et);
        switch_rl.setVisibility(View.GONE);
        state_tv.setVisibility(View.GONE);
        message_tv.setVisibility(View.VISIBLE);
        message_tv.setText(R.string.message_wait);
        divider_v.setVisibility(View.GONE);
        apply_rl.setVisibility(View.VISIBLE);
        apply_tv.setText("申请中..");
        cancel_iv.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) number_v.getLayoutParams();
        lp.leftMargin = 0;
        number_v.setLayoutParams(lp);
    }

    @Override
    public void opened(){
        modify_tv.setVisibility(View.VISIBLE);
        setEditTextNotEditable(name_et);
        dialogEnable = false;
        setEditTextNotEditable(address_et);
        setEditTextNotEditable(phone_number_et);
        switch_rl.setVisibility(View.VISIBLE);
        state_tv.setVisibility(View.GONE);
        message_tv.setVisibility(View.GONE);
        divider_v.setVisibility(View.GONE);
        apply_rl.setVisibility(View.GONE);
        apply_tv.setText("申请成功");
        cancel_iv.setVisibility(View.GONE);
        switch_sc.setEnabled(true);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) number_v.getLayoutParams();
        lp.leftMargin = margin60;
        number_v.setLayoutParams(lp);
    }

    @Override
    public void openFailed() {
        modify_tv.setVisibility(View.VISIBLE);
        setEditTextNotEditable(name_et);
        dialogEnable = false;
        setEditTextNotEditable(address_et);
        setEditTextNotEditable(phone_number_et);
        switch_rl.setVisibility(View.GONE);
        state_tv.setVisibility(View.VISIBLE);
        state_tv.setText("申请失败");
        message_tv.setVisibility(View.VISIBLE);
        message_tv.setText(R.string.message_wrong);
        divider_v.setVisibility(View.GONE);
        apply_rl.setVisibility(View.VISIBLE);
        apply_tv.setText("重新申请");
        cancel_iv.setVisibility(View.VISIBLE);
        switch_sc.setEnabled(true);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) number_v.getLayoutParams();
        lp.leftMargin = 0;
        number_v.setLayoutParams(lp);
    }

    @Override
    public void openRefused() {
        modify_tv.setVisibility(View.VISIBLE);
        setEditTextNotEditable(name_et);
        dialogEnable = false;
        setEditTextNotEditable(address_et);
        setEditTextNotEditable(phone_number_et);
        switch_rl.setVisibility(View.GONE);
        state_tv.setVisibility(View.VISIBLE);
        state_tv.setText("申请被拒绝");
        message_tv.setVisibility(View.VISIBLE);
        message_tv.setText(R.string.message_wrong);
        divider_v.setVisibility(View.GONE);
        apply_rl.setVisibility(View.VISIBLE);
        apply_tv.setText("重新申请");
        cancel_iv.setVisibility(View.VISIBLE);
        switch_sc.setEnabled(true);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) number_v.getLayoutParams();
        lp.leftMargin = 0;
        number_v.setLayoutParams(lp);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setInfo(PropertyInfo info) {
        name_et.setText(info.getMasterName());
        community_tv.setText(info.getCommName() + info.getBuildingName());
        address_et.setText(info.getFloor());
        phone_number_et.setText(info.getPhoneNumber());
    }

    @Override
    public void setCity(String city){
        city_tv.setText(city);
    }

    @Override
    public void setAddress(String address){
        community_tv.setText(address);
    }

    /**
     * 禁止编辑框输入
     * @param view
     */
    private void setEditTextNotEditable(EditText view){
        view.setCursorVisible(false);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
    }

    /**
     * 允许编辑框输入
     * @param view
     */
    private void setEditTextEditable(EditText view){
        view.setCursorVisible(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
    }
}
