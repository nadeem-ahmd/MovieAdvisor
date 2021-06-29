package com.movieadvisor.Authentication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.movieadvisor.R;

import java.util.ArrayList;

public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authentication);

        AuthenticationPagerAdapter authenticationPagerAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
        authenticationPagerAdapter.addFragment(new LoginFragment());
        authenticationPagerAdapter.addFragment(new RegisterFragment());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(authenticationPagerAdapter);
    }

    class AuthenticationPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        AuthenticationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }
}
