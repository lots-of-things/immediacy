package com.google.firebase.codelab.friendlychat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.firebase.codelab.friendlychat.fragments.ConversationsFragment;
import com.google.firebase.codelab.friendlychat.fragments.MapFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:

                return MapFragment.newInstance();
            case 1:
                return ConversationsFragment.newInstance();
            default:
                return MapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}