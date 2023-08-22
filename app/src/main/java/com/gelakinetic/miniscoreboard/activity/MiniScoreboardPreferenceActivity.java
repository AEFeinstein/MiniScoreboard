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

package com.gelakinetic.miniscoreboard.activity;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.gelakinetic.miniscoreboard.R;

public class MiniScoreboardPreferenceActivity extends AppCompatActivity {

    /* The root view for displaying the snackbar */
    private View mRootView;

    /**
     * Set up the UI. activity_mini_scoreboard_preference hardcodes the preference fragment
     *
     * @param savedInstanceState Passed to super.onCreate()
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_scoreboard_preference);
        mRootView = findViewById(android.R.id.content);

        /* Set up the Toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Show a little message on the Snackbar
     *
     * @param message A resource ID for a message to display
     */
    public void showSnackbar(int message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a little message on the Snackbar
     *
     * @param message A message to display
     */
    public void showSnackbar(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }
}
