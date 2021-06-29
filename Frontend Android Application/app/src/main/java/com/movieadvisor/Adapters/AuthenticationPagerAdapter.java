package com.movieadvisor.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.movieadvisor.Advisor.AdvisorEventFragment;
import com.movieadvisor.Advisor.AdvisorMovieFragment;

public class AuthenticationPagerAdapter extends FragmentStatePagerAdapter {

    public AuthenticationPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AdvisorMovieFragment();
            case 1:
                return new AdvisorEventFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
