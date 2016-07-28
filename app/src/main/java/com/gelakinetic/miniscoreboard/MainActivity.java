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
import com.gelakinetic.miniscoreboard.fragment.ScoreInputDialogFragment;
import com.gelakinetic.miniscoreboard.fragment.StatsFragment;
import com.gelakinetic.miniscoreboard.fragment.UserNameInputDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /* Request code for intents */
    private static final int REQ_CODE_SETTINGS = 52394;

    /* Tags for fragments */
    public static final String DATE_PICKER_TAG = "DATE_PICKER_TAG";
    private static final String DIALOG_TAG = "DIALOG_TAG";

    /* The user data */
    private FirebaseUser mCurrentUser;

    /* UI Elements */
    private View mRootView;
    private FloatingActionButton mFab;
    private ViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    /* Shared Preferences */
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

    /* A hash map of user names, because there is no join */
    private HashMap<String, String> mUsernameHashMap = new HashMap<>();
    private DatabaseReference mUsernameDatabaseReference;
    private ChildEventListener mUsernameEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            /* This is called multiple times on init */
            mUsernameHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(String.class));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onChildRemoved(dataSnapshot);
            onChildAdded(dataSnapshot, s);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mUsernameHashMap.remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            /* The app doesn't care about username order */
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            /* TODO show some sort of message? */
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

        /* Make sure the user is authenticated */
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            startActivity(AuthUiActivity.createIntent(this));
            finish();
        }

        mUsernameDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        /* Load the initial username hashmap */
        mUsernameDatabaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mUsernameHashMap.put(snapshot.getKey(), snapshot.getValue(String.class));
                        }

                        /* Check if a username exists for this user */
                        if (null == mUsernameHashMap.get(mCurrentUser.getUid())) {
                            /* If it doesn't, prompt them for a username */
                            UserNameInputDialogFragment newFragment = new UserNameInputDialogFragment();
                            newFragment.show(getSupportFragmentManager(), DIALOG_TAG);
                        }

                        /* Now that the usernames are loaded, add the listener for any changes */
                        mUsernameDatabaseReference.addChildEventListener(mUsernameEventListener);

                        /* And set up the view pager */
                        setupViewPager(mViewPager);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO show an error, quit?
                    }
                });
        
        /* Set up the root view */
        setContentView(R.layout.activity_main);
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
                ScoreInputDialogFragment newFragment = new ScoreInputDialogFragment();
                newFragment.show(getSupportFragmentManager(), DIALOG_TAG);
            }
        });

        /* Set up the ViewPager, don't load fragments until the username hashmap is received */
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        /* With three fragments, all will stay loaded */
        mViewPager.setOffscreenPageLimit(2);

        /* Set up the TabLayout */
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /* Set up shared preferences */
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
        mUsernameDatabaseReference.removeEventListener(mUsernameEventListener);
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

    /**
     * Submit a user's score to the firebase database
     *
     * @param date
     * @param puzzleTime
     * @param puzzleSize
     */
    public void submitNewScore(long date, int puzzleTime, int puzzleSize) {

        /* Get a database reference */
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        /* Make the entry object */
        DatabaseScoreEntry score = new DatabaseScoreEntry(puzzleTime, puzzleSize);

        /* Submit the data, first to daily scores then to personal scores. */
        database.child("dailyScores")
                .child(Long.toString(date))
                .child(mCurrentUser.getUid())
                .setValue(score);

        database.child("personalScores")
                .child(mCurrentUser.getUid())
                .child(Long.toString(date))
                .setValue(score);

//        All this is for generating test data
//        long baseDate = date;
//        Random rand = new Random();
//
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 10; j++) {
//
//                puzzleTime = rand.nextInt(600);
//                DatabaseScoreEntry score = new DatabaseScoreEntry(puzzleTime, puzzleSize, mCurrentUser.getUid() + "-" + i);
//
//                /* Submit the data. Only allow one score per day per user */
//                database.child("dailyScores")
//                        .child(Long.toString(baseDate + (j * 86400)))
//                        .child(mCurrentUser.getUid() + "-" + i)
//                        .setValue(score);
//
//                database.child("personalScores")
//                        .child(mCurrentUser.getUid() + "-" + i)
//                        .child(Long.toString(baseDate + (j * 86400)))
//                        .setValue(score);
//
//                database.child("users")
//                        .child(mCurrentUser.getUid() + "-" + i)
//                        .setValue("Username-" + i);
//
//                /* Give a little user feedback */
//                showSnackbar(R.string.score_submitted);
//            }
//        }
    }

    /**
     * TODO
     *
     * @param key
     * @return
     */
    public String getUserNameFromUid(String key) {
        return mUsernameHashMap.get(key);
    }
}
