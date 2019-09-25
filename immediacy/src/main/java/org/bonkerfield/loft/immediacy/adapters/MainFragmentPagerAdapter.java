package org.bonkerfield.loft.immediacy.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.bonkerfield.loft.immediacy.fragments.LoginFragment;
import org.bonkerfield.loft.immediacy.fragments.RegisterFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return LoginFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}