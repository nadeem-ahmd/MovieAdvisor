package com.movieadvisor.Main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.movieadvisor.Adapters.MainPagerAdapter;
import com.movieadvisor.R;
import com.movieadvisor.Views.CustomViewPager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CustomViewPager customViewPager;
    private MenuItem previousMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        customViewPager = findViewById(R.id.view_pager);
        customViewPager.setAdapter(mainPagerAdapter);
        customViewPager.setScrollable(true);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.browse_menu_item:
                        customViewPager.setCurrentItem(0);
                        break;
                    case R.id.advisor_menu_item:
                        customViewPager.setCurrentItem(1);
                        break;
                    case R.id.groups_menu_item:
                        customViewPager.setCurrentItem(2);
                        break;
                    case R.id.profile_menu_item:
                        customViewPager.setCurrentItem(3);
                        break;
                }
                return false;
            }
        });

        customViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }

                if (previousMenuItem != null)
                    previousMenuItem.setChecked(false);
                else
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                previousMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });
    }


}
