package com.example.appchat.views.home.tabcontact.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mTitles = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    public void setFragments(List<Fragment> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mFragments.clear();
        mFragments.addAll(list);

    }

    public void setTitless(List<String> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mTitles.clear();
        mTitles.addAll(list);

    }

    public void addFragment(Fragment fragment,String name){
        mFragments.add(fragment);
        mTitles.add(name);
    }
}
