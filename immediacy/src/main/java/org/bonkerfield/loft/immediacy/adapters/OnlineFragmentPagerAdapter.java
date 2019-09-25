package org.bonkerfield.loft.immediacy.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.bonkerfield.loft.immediacy.fragments.ConversationsFragment;
import org.bonkerfield.loft.immediacy.fragments.NearbyListFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:

                return NearbyListFragment.newInstance();
            case 1:
                return ConversationsFragment.newInstance();
            default:
                return NearbyListFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}