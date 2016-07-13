/**
 * Copyright 2016 Adam Feinstein
 * <p/>
 * This file is part of Mini Scoreboard.
 * <p/>
 * Mini Scoreboard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Mini Scoreboard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Mini Scoreboard.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gelakinetic.miniscoreboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.gelakinetic.miniscoreboard.fragment.DailyFragment;
import com.gelakinetic.miniscoreboard.fragment.HistoryFragment;
import com.gelakinetic.miniscoreboard.fragment.MiniScoreboardFragment;
import com.gelakinetic.miniscoreboard.fragment.MiniScoreboardPreferenceFragment;
import com.gelakinetic.miniscoreboard.fragment.StatsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SETTINGS = 52394;
    private View mRootView;
    private FloatingActionButton mFab;
    private ViewPagerAdapter mViewPagerAdapter;
    private SharedPreferences mSharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefKey) {
            if (prefKey.equals(getString(R.string.pref_key_daily_notification))) {
                if (mSharedPreferences.getBoolean(prefKey, false)) {
                    MiniScoreboardAlarm.setAlarm(MainActivity.this);
                } else {
                    MiniScoreboardAlarm.cancelAlarm(MainActivity.this);
                }
            }
        }
    };

    /**
     * TODO
     *
     * @param context
     * @return
     */
    @MainThread
    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, MainActivity.class);
        return in;
    }

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    @MainThread
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Set up the root view */
        mRootView = findViewById(android.R.id.content);

        /* Set up the Toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        /* Set up the FloatingActionButton */
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* Set up the ViewPager */
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        /* Set up the TabLayout */
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

        /* Make sure the user is authenticated */
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(AuthUiActivity.createIntent(this));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }

    /**
     * TODO
     *
     * @param menu
     * @return
     */
    @MainThread
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * TODO
     *
     * @param item
     * @return
     */
    @MainThread
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent in = new Intent();
                in.setClass(MainActivity.this, MiniScoreboardPreferenceActivity.class);
                startActivityForResult(in, REQ_CODE_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SETTINGS: {
                switch (resultCode) {
                    case MiniScoreboardPreferenceFragment.RES_CODE_ACCT_DELETED: {
                        startActivity(AuthUiActivity.createIntent(this));
                        finish();
                        break;
                    }
                    case MiniScoreboardPreferenceFragment.RES_CODE_SIGNED_OUT: {
                        startActivity(AuthUiActivity.createIntent(this));
                        finish();
                        break;
                    }
                }
                break;
            }
            default: {
                break;
            }
        }

    }

    /**
     * TODO
     *
     * @param viewPager
     */
    @MainThread
    private void setupViewPager(ViewPager viewPager) {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(this, new DailyFragment(), R.string.daily_fragment_title);
        mViewPagerAdapter.addFragment(this, new HistoryFragment(), R.string.history_fragment_title);
        mViewPagerAdapter.addFragment(this, new StatsFragment(), R.string.stats_fragment_title);
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        onPageSelected(0);
    }

    /**
     * TODO
     *
     * @param i
     */
    private void onPageSelected(int i) {
        if (((MiniScoreboardFragment) mViewPagerAdapter.getFragment(i)).shouldShowFab()) {
            mFab.show();
        } else {
            mFab.hide();
        }
    }

    /**
     * TODO
     *
     * @param errorMessageRes
     */
    @MainThread
    public void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
