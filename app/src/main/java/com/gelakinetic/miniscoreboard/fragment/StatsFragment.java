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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.db.chart.model.BarSet;
import com.db.chart.model.ChartEntry;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MainActivity;
import com.gelakinetic.miniscoreboard.database.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.ui.ScoreEntryHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_DAILY_WINNERS;
import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_PERSONAL_SCORES;

public class StatsFragment extends MiniScoreboardFragment {

    /* Statistics text */
    private TextView mMeanTextView;
    private TextView mStddevTextView;
    private TextView mWinsTextView;
    private TextView mUsernameTextView;

    private static final float MAX_NUM_BINS = 30;
    private static final float MAX_NUM_X_LABELS = 10;
    private static final float MAX_NUM_Y_LABELS = 5;
    private BarChartView mBarChartView;

    private int mWins = 0;

    private final ArrayList<DatabaseScoreEntry> mStatisticsEntries = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DatabaseReference mStatsScoresDatabaseReference;
    private final ChildEventListener mStatsScoresChildEventListener = new ChildEventListener() {
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
            updateStatistics();
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
                    if (newIndex > oldIndex) {
                        /* If the new index is after the old index, shift it back one because the
                         * old item is removed first */
                        newIndex--;
                    }
                    /* The entry moved, remove it first from the old index*/
                    mStatisticsEntries.remove(oldIndex);
                    mRecyclerView.getAdapter().notifyItemRemoved(oldIndex);
                    /* Then add it in it's new position */
                    mStatisticsEntries.add(newIndex, entry);
                    mRecyclerView.getAdapter().notifyItemInserted(newIndex);
                }
            }
            updateStatistics();
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
            updateStatistics();
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
    private final ChildEventListener mWinnersChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String key) {
            if (key != null) {
                mWins++;
                if (isAdded() && mWinsTextView != null) {
                    mWinsTextView.setText(String.format(getString(R.string.wins_label), mWins));
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            /* Don't care if a win gets changed, only added or removed */
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getKey() != null) {
                mWins--;
                if (isAdded() && mWinsTextView != null) {
                    mWinsTextView.setText(String.format(getString(R.string.wins_label), mWins));
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            /* Don't care if a win gets moved, only added or removed */
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            /* TODO error handling? */
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
                return mStatisticsEntries.size();
            }
        });

        mStatsScoresDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mUsernameTextView = ((TextView) view.findViewById(R.id.user_name_text_view));

        /* Get references to the statistics text views */
        mMeanTextView = (TextView) view.findViewById(R.id.mean_text_view);
        mStddevTextView = (TextView) view.findViewById(R.id.stddev_text_view);
        mWinsTextView = (TextView) view.findViewById(R.id.wins_text_view);

        /* Get a reference to the bar chart */
        mBarChartView = (BarChartView) view.findViewById(R.id.barchart);

        /* Format the chart */
        mBarChartView.setXAxis(true)
                .setYAxis(true)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAxisColor(getResources().getColor(R.color.colorPrimaryDark));

        setUser(FirebaseAuth.getInstance().getCurrentUser().getUid());

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
        mStatsScoresDatabaseReference.removeEventListener(mWinnersChildEventListener);
        mStatisticsEntries.clear();
    }

    /**
     * @return false, since this fragment should not have a floating action button
     */
    @Override
    public boolean shouldShowFab() {
        return false;
    }

    @Override
    public boolean shouldShowChangeUsersButton() {
        return true;
    }

    /**
     * Updated the calculated statistics. This should be called whenever the entries change
     */
    private void updateStatistics() {
        if (isAdded() && mMeanTextView != null) {
            mMeanTextView.setText(String.format(getString(R.string.mean_label),
                    formatTime(getMean(mStatisticsEntries), true)));
            mStddevTextView.setText(String.format(getString(R.string.stddev_label),
                    formatTime(getStdDev(mStatisticsEntries), true)));
            updateBarChart(mStatisticsEntries);
        }
    }

    /**
     * Find the average value (mean) of the given puzzle solution times
     *
     * @param entries A collection of database entries with puzzle times
     * @return The mean of all times in the ArrayList of entries
     */
    private static double getMean(ArrayList<DatabaseScoreEntry> entries) {
        if (entries.size() == 0) {
            return 0;
        }
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
    private static double getStdDev(ArrayList<DatabaseScoreEntry> entries) {
        if (entries.size() == 0) {
            return 0;
        }
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
     * @param seconds   The time in seconds
     * @param hundreths Whether or not the hundreths of a second should be displayed
     * @return A string with the formatted time
     */
    private static String formatTime(double seconds, boolean hundreths) {
        if (hundreths) {
            return String.format("%01d:%02d.%02d",
                    ((int) seconds) / 60,
                    ((int) seconds) % 60,
                    (int) ((seconds - ((int) seconds)) * 100));
        } else {
            return String.format("%01d:%02d",
                    ((int) seconds) / 60,
                    ((int) seconds) % 60);
        }
    }

    /**
     * Update the bar chart with the latest data
     */
    private void updateBarChart(ArrayList<DatabaseScoreEntry> entries) {
        if (entries.size() == 0) {
            return;
        }

        HashMap<Integer, BarSet> mBarsHashMap = new HashMap<>();

        /* Calculate how often to write X axis labels (MAX_NUM_X_LABELS total) */
        int labelMod = (int) Math.ceil(MAX_NUM_BINS / MAX_NUM_X_LABELS);

        /* Find the largest time it took to solve a puzzle */
        int largestTime = 0;
        for (DatabaseScoreEntry entry : entries) {
            if (entry.mPuzzleTime > largestTime) {
                largestTime = entry.mPuzzleTime;
            }
        }

        /* Figure out how large each bin will be */
        int binSize = (int) Math.ceil(largestTime / MAX_NUM_BINS);

        /* For every score */
        for (DatabaseScoreEntry entry : entries) {
            /* Check if the hash map contains the size for this puzzle */
            if (!mBarsHashMap.containsKey(entry.mPuzzleSize)) {
                /* Create a new BarSet */
                BarSet newBarSet = new BarSet();

                /* Add all the bars to the BarSet */
                for (int i = 0; i < MAX_NUM_BINS; i++) {
                    if (labelMod == 0 || i % labelMod == 0) {
                        /* Label */
                        newBarSet.addBar(formatTime((i + 1) * binSize, false), 0);
                    } else {
                        /* No Label */
                        newBarSet.addBar("", 0);
                    }
                }

                /* Add the BarSet to the HashMap of all BarSets */
                mBarsHashMap.put(entry.mPuzzleSize, newBarSet);
            }

            /* Grab the barSet from the hash map */
            BarSet barSet = mBarsHashMap.get(entry.mPuzzleSize);

            /* Figure out what entry this bin goes into */
            int bin = entry.mPuzzleTime / binSize;

            /* Increment the bar by one */
            barSet.getEntry(bin).setValue(barSet.getEntry(bin).getValue() + 1);
        }

        /* Color the bars, first get the color array */
        int colors[] = getResources().getIntArray(R.array.barColors);
        int colorIdx = 0;

        /* Get and sort the keys from the hash map */
        Integer keySet[] = new Integer[mBarsHashMap.size()];
        mBarsHashMap.keySet().toArray(keySet);
        Arrays.sort(keySet);

        /* Store the largest bar value here, for setting the Y axis labels */
        float maxValue = 0;

        /* Format the bars, color & label */
        for (int key : keySet) {
            /* Color all the bars in this bar set, then increment the color index */
            mBarsHashMap.get(key).setColor(colors[colorIdx]);
            colorIdx = (colorIdx + 1) % colors.length;

            /* For every entry in this bar set, find the largest entry */
            for (ChartEntry entry : mBarsHashMap.get(key).getEntries()) {
                if (entry.getValue() > maxValue) {
                    maxValue = entry.getValue();
                }
            }
        }

        /* Reset the chart data */
        mBarChartView.dismiss();

        // TODO, this worked with williamchart 2.3.0, which no longer exists
//        try {
//            /* Use reflection to get the Y Axis Renderer */
//            ChartView cv = mBarChartView;
//            Field f = ChartView.class.getDeclaredField("yRndr");
//            f.setAccessible(true);
//            YRenderer yRndr = (YRenderer) f.get(cv);
//
//            /* Set the vertical step on the chart to display 5 marks */
//            int step = (int) Math.ceil(maxValue / MAX_NUM_Y_LABELS);
//            mBarChartView.setStep(step);
//
//            while (maxValue % step != 0) {
//                maxValue++;
//            }
//
//            /* Manually set the boarder values in the renderer. For whatever reason, this
//             * does not get automatically recalculated when new data is swapped into the chart
//             */
//            yRndr.setBorderValues(0, (int) maxValue);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            /* Eat it */
//        }

        /* Add all bar sets to the chart */
        for (int key : mBarsHashMap.keySet()) {
            mBarChartView.addData(mBarsHashMap.get(key));
        }

        /* Show the data */
        mBarChartView.show();
    }

    /**
     * TODO document
     *
     * @param mUid
     */
    public void setUser(String mUid) {
        /* Remove old listeners */
        mStatsScoresDatabaseReference.removeEventListener(mStatsScoresChildEventListener);
        mStatsScoresDatabaseReference.removeEventListener(mWinnersChildEventListener);

        /* Clear local data */
        mRecyclerView.getAdapter().notifyItemRangeRemoved(0, mStatisticsEntries.size());
        mStatisticsEntries.clear();
        updateStatistics();

        mWins = 0;
        mWinsTextView.setText(String.format(getString(R.string.wins_label), mWins));

        /* Get this daily data, and order it by date */
        mStatsScoresDatabaseReference
                .child(KEY_PERSONAL_SCORES)
                .child(mUid)
                .orderByKey()
                .addChildEventListener(mStatsScoresChildEventListener);

        mStatsScoresDatabaseReference
                .child(KEY_DAILY_WINNERS)
                .orderByValue()
                .startAt(mUid)
                .endAt(mUid)
                .addChildEventListener(mWinnersChildEventListener);

        /* Write the username at the top */
        mUsernameTextView.setText(((MainActivity) getActivity()).getUserNameFromUid(mUid));

        /* Reset the bar chart view */
        mBarChartView.reset();
    }
}
