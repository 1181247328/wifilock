package com.deelock.wifilock.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by binChuan on 2017\9\26 0026.
 */

public class ViewPagerFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> list;
    private List<String> titleList;

    public ViewPagerFragmentAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(titleList == null){
            return super.getPageTitle(position);
        } else {
            return titleList.get(position);
        }
    }

    public void setTitleList(List<String> titleList) {
        this.titleList = titleList;
    }
}
