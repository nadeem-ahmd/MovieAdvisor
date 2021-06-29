package com.movieadvisor.Adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.movieadvisor.Main.AdvisorFragment;
import com.movieadvisor.Main.BrowseFragment;
import com.movieadvisor.Main.GroupsFragment;
import com.movieadvisor.Main.ProfileFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BrowseFragment();
            case 1:
                return new AdvisorFragment();
            case 2:
                return new GroupsFragment();
            case 3:
                return new ProfileFragment();
        }
        return null;
    }

    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Browse";
            case 1:
                return "Advisor";
            case 2:
                return "Groups";
            case 3:
                return "Profile";
        }
        return null;
    }
}
