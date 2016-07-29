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
import java.util.Collections;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class StatsFragment extends MiniScoreboardFragment {

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

        /* Write the date at the top */
        String username = ((MainActivity)getActivity()).getUserNameFromUid(uid);
        ((TextView)view.findViewById(R.id.user_name_text_view)).setText(username);

        /* TODO show other statistics */

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
}
