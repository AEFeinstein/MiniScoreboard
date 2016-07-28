/**
 * Copyright 2016 Adam Feinstein
 * <p>
 * This file is part of Mini Scoreboard.
 * <p>
 * Mini Scoreboard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Mini Scoreboard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
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

import com.gelakinetic.miniscoreboard.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.MainActivity;
import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.ScoreEntryHolder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class DailyFragment extends MiniScoreboardFragment {

    private ArrayList<DatabaseScoreEntry> mDailyEntries = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Query mDailyScoreDatabaseReference;
    private ChildEventListener mDailyScoreChildEventListener = new ChildEventListener() {
        /**
         * TODO document
         * @param dataSnapshot
         * @param s
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mUid = dataSnapshot.getKey();
            entry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(dataSnapshot.getKey());

            /* Insert, sorted, into the array, notify the adapter */
            int index = Collections.binarySearch(mDailyEntries, entry, new DatabaseScoreEntry().new TimeComparator());
            /* binarySearch returns the non-negative index of the element, or a negative index
             * which is the -index - 1 where the element would be inserted.
             */
            if (index < 0) {
                index = -1 * (index + 1);
            }
            mDailyEntries.add(index, entry);
            mRecyclerView.getAdapter().notifyItemInserted(index);
        }

        /**
         * TODO document
         * @param dataSnapshot
         * @param s
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mUid = dataSnapshot.getKey();
            entry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(dataSnapshot.getKey());

            /* Find where it is in the daily entries array */
            int oldIndex = mDailyEntries.indexOf(entry);
            /* See where it would go */
            int newIndex = Collections.binarySearch(mDailyEntries, entry, new DatabaseScoreEntry().new TimeComparator());
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
                    mDailyEntries.remove(oldIndex);
                    mRecyclerView.getAdapter().notifyItemRemoved(oldIndex);
                    /* Then add it in it's new position */
                    mDailyEntries.add(newIndex, entry);
                    mRecyclerView.getAdapter().notifyItemInserted(newIndex);
                }
            }
        }

        /**
         * TODO document
         * @param dataSnapshot
         * @param s
         */
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            /* Always follows onChildChanged which caused the shuffle. On onChildChanged handles it all */
        }

        /**
         * TODO document
         * @param dataSnapshot
         */
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mUid = dataSnapshot.getKey();
            entry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(dataSnapshot.getKey());

            /* Find where it is in the daily entries array */
            int index = mDailyEntries.indexOf(entry);
            if (index != -1) {
                /* If it exist, remove it and notify the adapter */
                mDailyEntries.remove(index);
                mRecyclerView.getAdapter().notifyItemRemoved(index);
            }
        }

        /**
         * TODO document
         * @param databaseError
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {
            /* TODO some error handling */
        }
    };

    /**
     * Required empty public constructor
     */
    public DailyFragment() {

    }

    /**
     * TODO document
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        /* Figure out what day it is */
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        /* Set up the Recycler View */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.statistics_recycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new jp.wasabeef.recyclerview.animators.SlideInRightAnimator(new DecelerateInterpolator()));
        mRecyclerView.getItemAnimator().setAddDuration(750);
        mRecyclerView.getItemAnimator().setRemoveDuration(750);
        mRecyclerView.getItemAnimator().setMoveDuration(750);
        mRecyclerView.getItemAnimator().setChangeDuration(750);
        mRecyclerView.setAdapter(new RecyclerView.Adapter<ScoreEntryHolder>() {
            @Override
            public ScoreEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemMessage = getActivity().getLayoutInflater().inflate(R.layout.statistics_card, parent, false);
                return new ScoreEntryHolder(itemMessage);
            }

            @Override
            public void onBindViewHolder(ScoreEntryHolder holder, int position) {
                holder.setDateText(mDailyEntries.get(position).mUsername);
                holder.setPuzzleTimeText(mDailyEntries.get(position).getTime());
            }

            @Override
            public int getItemCount() {
                if (null == mDailyEntries) {
                    return 0;
                }
                return mDailyEntries.size();
            }
        });

        /* Get this daily data, and order it by date */
        mDailyScoreDatabaseReference = FirebaseDatabase.getInstance().getReference().child("dailyScores")
                .child(Long.toString(calendar.getTimeInMillis() / 1000))
                .orderByChild("mPuzzleTime")
                .limitToFirst(20);
        mDailyScoreDatabaseReference.addChildEventListener(mDailyScoreChildEventListener);

        /* TODO show this user's entry separately? */

        return view;
    }

    /**
     * TODO document
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /* Remove the database listener, and clear the entries */
        mDailyScoreDatabaseReference.removeEventListener(mDailyScoreChildEventListener);
        mDailyEntries.clear();
    }

    /**
     * TODO document
     *
     * @return
     */
    @Override
    public boolean shouldShowFab() {
        return true;
    }
}
