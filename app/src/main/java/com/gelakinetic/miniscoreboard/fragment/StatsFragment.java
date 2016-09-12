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

package com.gelakinetic.miniscoreboard.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.gelakinetic.miniscoreboard.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.MainActivity;
import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.ScoreEntryHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class StatsFragment extends MiniScoreboardFragment {

    /* Statistics text */
    private TextView mMeanTextView;
    private TextView mStddevTextView;

    /* Bar Chart globals */
    private static final int BIN_SIZE = 10;
    private BarChartView mBarChartView;
    HashMap<Integer, BarSet> mBarsHashMap = new HashMap<>(2);

    private ArrayList<DatabaseScoreEntry> mStatisticsEntries = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Query mStatsScoresDatabaseReference;
    private ChildEventListener mStatsScoresChildEventListener = new ChildEventListener() {
        /**
         * Called when a child is added to the database. The key for this value is the date
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         * @param s Unused
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mDate = Long.parseLong(dataSnapshot.getKey());

            /* Insert, sorted, into the array, notify the adapter */
            int index = Collections.binarySearch(mStatisticsEntries, entry,
                    new DatabaseScoreEntry().new DateComparator());
            /* binarySearch returns the non-negative index of the element, or a negative index
             * which is the -index - 1 where the element would be inserted.
             */
            if (index < 0) {
                index = -1 * (index + 1);
            }
            mStatisticsEntries.add(index, entry);
            mRecyclerView.getAdapter().notifyItemInserted(index);
            updateStatistics(entry.mPuzzleTime, entry.mPuzzleSize);
        }

        /**
         * Called when a child is changedin the database. The key for this value is the date
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         * @param s Unused
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mDate = Long.parseLong(dataSnapshot.getKey());

            /* Find where it is in the daily entries array */
            int oldIndex = mStatisticsEntries.indexOf(entry);
            /* See where it would go */
            int newIndex = Collections.binarySearch(mStatisticsEntries, entry, new DatabaseScoreEntry().new DateComparator());
            /* binarySearch returns the non-negative index of the element, or a negative index
             * which is the -index - 1 where the element would be inserted.
             */
            if (newIndex < 0) {
                newIndex = -1 * (newIndex + 1);
            }

            if (oldIndex != -1) {
                if (oldIndex == newIndex) {
                    /* The entry didn't move, it just changed */
                    mRecyclerView.getAdapter().notifyItemChanged(oldIndex);
                } else {
                    /* The entry moved, remove it first from the old index*/
                    mStatisticsEntries.remove(oldIndex);
                    mRecyclerView.getAdapter().notifyItemRemoved(oldIndex);
                    /* Then add it in it's new position */
                    mStatisticsEntries.add(newIndex, entry);
                    mRecyclerView.getAdapter().notifyItemInserted(newIndex);
                }
            }
            updateStatistics(entry.mPuzzleTime, entry.mPuzzleSize);
        }

        /**
         * Called when a child is moved in the database. This is handled by onChildChanged
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         * @param s Unused
         */
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            /* Always follows onChildChanged which caused the shuffle.
             * On onChildChanged handles it all
             */
        }

        /**
         * Called when a child is removed from the database. The key for this value is the date
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         */
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mDate = Long.parseLong(dataSnapshot.getKey());

            /* Find where it is in the daily entries array */
            int index = mStatisticsEntries.indexOf(entry);
            if (index != -1) {
                /* If it exist, remove it and notify the adapter */
                mStatisticsEntries.remove(index);
                mRecyclerView.getAdapter().notifyItemRemoved(index);
            }
            updateStatistics(entry.mPuzzleTime, entry.mPuzzleSize);
        }

        /**
         * Called when the database operation is cancelled
         *
         * @param databaseError The database error that cancelled the operation
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {
            /* TODO some error handling */
        }
    };

    /**
     * Required empty public constructor
     */
    public StatsFragment() {

    }

    /**
     * Create a view which will display the statistics for the current user
     * Attach to the Firebase database too
     *
     * @param inflater           A LayoutInflater to inflate the view with
     * @param container          A ViewGroup to add this view to
     * @param savedInstanceState Unused
     * @return The View for this Fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        /* Set up the Recycler View */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.statistics_recycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new SlideInRightAnimator(new DecelerateInterpolator()));
        mRecyclerView.getItemAnimator().setAddDuration(750);
        mRecyclerView.getItemAnimator().setRemoveDuration(750);
        mRecyclerView.getItemAnimator().setMoveDuration(750);
        mRecyclerView.getItemAnimator().setChangeDuration(750);
        mRecyclerView.setAdapter(new RecyclerView.Adapter<ScoreEntryHolder>() {
            /**
             * Called when each individual view is created in the RecyclerView
             *
             * @param parent The ViewGroup to add this ScoreEntryHolder to
             * @param viewType Unused
             * @return A ScoreEntryHolder to be displayed in the RecyclerView
             */
            @Override
            public ScoreEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View statCard = getActivity().getLayoutInflater()
                        .inflate(R.layout.statistics_card, parent, false);
                return new ScoreEntryHolder(statCard);
            }

            /**
             * Called when data is bound to each individual view in the RecyclerView
             *
             * @param holder The ScoreEntryHolder to bind the data to
             * @param position The position in the list to get data from
             */
            @Override
            public void onBindViewHolder(ScoreEntryHolder holder, int position) {
                holder.setTitleText(mStatisticsEntries.get(position).getDate());
                holder.setPuzzleTimeText(mStatisticsEntries.get(position).getPuzzleTime());
            }

            /**
             * @return The total number of items to be displayed in this RecyclerView
             */
            @Override
            public int getItemCount() {
                if (null == mStatisticsEntries) {
                    return 0;
                }
                return mStatisticsEntries.size();
            }
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /* Get this daily data, and order it by date */
        mStatsScoresDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("personalScores")
                .child(uid)
                .orderByKey();
        mStatsScoresDatabaseReference.addChildEventListener(mStatsScoresChildEventListener);

        /* Write the username at the top */
        String username = ((MainActivity) getActivity()).getUserNameFromUid(uid);
        ((TextView) view.findViewById(R.id.user_name_text_view)).setText(username);

        /* Get references to the statistics text views */
        mMeanTextView = (TextView) view.findViewById(R.id.mean_text_view);
        mStddevTextView = (TextView) view.findViewById(R.id.stddev_text_view);

        /* Get a reference to the bar chart */
        mBarChartView = (BarChartView) view.findViewById(R.id.barchart);

        /* Format the chart */
        mBarChartView.setBarSpacing(Tools.fromDpToPx(40));
        mBarChartView.setRoundCorners(Tools.fromDpToPx(2));
        mBarChartView.setXAxis(true)
                .setYAxis(true)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAxisColor(getResources().getColor(R.color.colorPrimaryDark));

        return view;
    }

    /**
     * Called when the View is destroyed. Detach the database and clear out any local entries
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /* Remove the database listener, and clear the entries */
        mStatsScoresDatabaseReference.removeEventListener(mStatsScoresChildEventListener);
        mStatisticsEntries.clear();
    }

    /**
     * @return false, since this fragment should not have a floating action button
     */
    @Override
    public boolean shouldShowFab() {
        return false;
    }

    /**
     * Updated the calculated statistics. This should be called whenever the entries change
     *
     * @param newPuzzleTime The time for the latest entry
     * @param size          The latest entry's size (5x5 or 7x7)
     */
    private void updateStatistics(int newPuzzleTime, int size) {
        mMeanTextView.setText(String.format(getString(R.string.mean_label),
                formatTime(getMean(mStatisticsEntries))));
        mStddevTextView.setText(String.format(getString(R.string.stddev_label),
                formatTime(getStdDev(mStatisticsEntries))));

        updateBarChart(newPuzzleTime, size);
    }

    /**
     * Find the average value (mean) of the given puzzle solution times
     *
     * @param entries A collection of database entries with puzzle times
     * @return The mean of all times in the ArrayList of entries
     */
    static double getMean(ArrayList<DatabaseScoreEntry> entries) {
        double sum = 0.0;
        for (DatabaseScoreEntry entry : entries) {
            sum += entry.mPuzzleTime;
        }
        return sum / ((double) entries.size());
    }

    /**
     * Find the standard deviation of the given puzzle solution times
     *
     * @param entries A collection of database entries with puzzle times
     * @return The standard deviation of all times in the ArrayList of entries
     */
    static double getStdDev(ArrayList<DatabaseScoreEntry> entries) {

        double mean = getMean(entries);
        double temp = 0;
        for (DatabaseScoreEntry entry : entries) {
            temp += (entry.mPuzzleTime - mean) * (entry.mPuzzleTime - mean);
        }
        double variance = temp / ((double) entries.size());
        return Math.sqrt(variance);
    }

    /**
     * Given a time in seconds, format it nicely like 0:00.00
     *
     * @param seconds The time in seconds
     * @return A string with the formatted time
     */
    static String formatTime(double seconds) {
        return String.format("%01d:%02d.%02d",
                ((int) seconds) / 60,
                ((int) seconds) % 60,
                (int) ((seconds - ((int) seconds)) * 100));
    }

    /**
     * Update the bar chart with the latest data
     *
     * @param seconds The latest entry, in seconds
     * @param size    The latest entry's size (5x5 or 7x7)
     */
    private void updateBarChart(int seconds, int size) {

        /* Check if the hash map contains the size for this puzzle */
        if (!mBarsHashMap.containsKey(size)) {
            /* Add it if it does not exist */
            mBarsHashMap.put(size, new BarSet());
        }

        /* Grab the barSet from the hash map */
        BarSet barSet = mBarsHashMap.get(size);

        /* Figure out what entry this bin goes into */
        int bin = seconds / BIN_SIZE;

        /* Find the largest bin across all BarSets */
        int maxNumBins = bin + 1; /* This entry's bin */
        for (int key : mBarsHashMap.keySet()) {
            int binSize = mBarsHashMap.get(key).size();
            if (binSize > maxNumBins) {
                maxNumBins = binSize;
            }
        }

        /* Make sure that bin exists, for all BarSets */
        for (int i = 0; i < maxNumBins; i++) {
            for (int key : mBarsHashMap.keySet()) {
                if (mBarsHashMap.get(key).size() < i + 1) {
                    mBarsHashMap.get(key).addBar(String.format("%d", (i + 1) * BIN_SIZE), 0);
                }
            }
        }

        /* Increment the bar by one */
        barSet.getEntry(bin).setValue(barSet.getEntry(bin).getValue() + 1);

        /* Color the bars, first get the color array */
        int colors[] = getResources().getIntArray(R.array.barColors);
        int colorIdx = 0;
        /* Get and sort the keys from the hash map */
        Integer keySet[] = new Integer[mBarsHashMap.size()];
        mBarsHashMap.keySet().toArray(keySet);
        Arrays.sort(keySet);
        /* Color each bar */
        for (int key : keySet) {
            mBarsHashMap.get(key).setColor(colors[colorIdx]);
            colorIdx = (colorIdx + 1) % colors.length;
        }


        /* Reset the chart data */
        mBarChartView.getData().clear();

        /* Add all bar sets to the chart */
        for (int key : mBarsHashMap.keySet()) {
            mBarChartView.addData(mBarsHashMap.get(key));
        }

        /* Show the data */
        mBarChartView.show();
    }
}
