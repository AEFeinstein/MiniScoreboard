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

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.gelakinetic.miniscoreboard.DailyEntryHolder;
import com.gelakinetic.miniscoreboard.DatabaseDailyEntry;
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
import java.util.Collections;

public class HistoryFragment extends MiniScoreboardFragment {

    /* Backing array list & adapter to store and display data */
    private ArrayList<DatabaseDailyEntry> mDailyEntries = new ArrayList<>();
    private ExpandableRecyclerAdapter<DailyEntryHolder, ScoreEntryHolder> mAdapter;

    /* Database reference and the listener for events */
    private Query mHistoryScoresDatabaseReference;
    private ChildEventListener mHistoryScoresChildEventListener = new ChildEventListener() {
        /**
         * Called when a child is added to the database.
         *
         * @param dataSnapshot The child added. The date is the key and its children are
         *                     DatabaseScoreEntry objects
         * @param key Unused
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String key) {
            /* Get the object from the database */
            Long date = Long.parseLong(dataSnapshot.getKey());

            /* Make an entry for a whole day's worth of scores */
            DatabaseDailyEntry dailyEntry = new DatabaseDailyEntry(date);

            /* Insert, sorted, into the array, notify the adapter
             * binarySearch returns the non-negative index of the element, or a negative index
             * which is the -index - 1 where the element would be inserted.
             */
            int index = Collections.binarySearch(mDailyEntries, dailyEntry);
            if (index < 0) {
                index = -1 * (index + 1);
            }
            mDailyEntries.add(index, dailyEntry);
            mAdapter.notifyParentItemInserted(index);

            /* For all the scores that day, save them in an array */
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                DatabaseScoreEntry scoreEntry = child.getValue(DatabaseScoreEntry.class);
                scoreEntry.mUid = child.getKey();
                scoreEntry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(scoreEntry.mUid);
                scoreEntry.mDate = date;

                mDailyEntries.get(index).mScores.add(scoreEntry);
                /* Notify the adapter that a child has been added */
                mAdapter.notifyChildItemInserted(index, mDailyEntries.get(index).mScores.size() - 1);
            }
        }

        /**
         * Called when a child is changed in the database.
         *
         * @param dataSnapshot The child changed. The date is the key and its children are
         *                     DatabaseScoreEntry objects
         * @param key Unused
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String key) {
            /* Get the object from the database */
            Long date = Long.parseLong(dataSnapshot.getKey());

            /* Make an entry for a whole day's worth of scores */
            DatabaseDailyEntry dailyEntry = new DatabaseDailyEntry(date);

            /* For all the scores that day, save them in an array */
            ArrayList<DatabaseScoreEntry> scoresTmp = new ArrayList<>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                DatabaseScoreEntry scoreEntry = child.getValue(DatabaseScoreEntry.class);
                scoreEntry.mUid = child.getKey();
                scoreEntry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(scoreEntry.mUid);
                scoreEntry.mDate = date;
                scoresTmp.add(scoreEntry);
            }

            /* Find where it is in the daily entries array */
            int oldIndex = mDailyEntries.indexOf(dailyEntry);
            /* See where it would go */
            int newIndex = Collections.binarySearch(mDailyEntries, dailyEntry);
            /* binarySearch returns the non-negative index of the element, or a negative index
             * which is the -index - 1 where the element would be inserted.
             */
            if (newIndex < 0) {
                newIndex = -1 * (newIndex + 1);
            }

            /* Make sure that the entry used to exist first */
            if (oldIndex != -1) {
                if (oldIndex == newIndex) {
                    /* The entry didn't move, it just changed. First remove all the children */
                    int numChildren = mDailyEntries.get(newIndex).mScores.size();
                    mDailyEntries.get(newIndex).mScores.clear();
                    mAdapter.notifyChildItemRangeRemoved(newIndex, 0, numChildren);

                    /* Then add the new children */
                    for (DatabaseScoreEntry entry : scoresTmp) {
                        mDailyEntries.get(newIndex).mScores.add(entry);
                        mAdapter.notifyChildItemInserted(newIndex, mDailyEntries.get(newIndex).mScores.size() - 1);
                    }
                } else {
                    /* The entry moved, remove it first from the old index*/
                    mDailyEntries.remove(oldIndex);
                    mAdapter.notifyParentItemRemoved(oldIndex);

                    /* Then add it in it's new position */
                    mDailyEntries.add(newIndex, dailyEntry);
                    mAdapter.notifyParentItemInserted(newIndex);

                    /* Then add the new children */
                    for (DatabaseScoreEntry entry : scoresTmp) {
                        mDailyEntries.get(newIndex).mScores.add(entry);
                        mAdapter.notifyChildItemInserted(newIndex, mDailyEntries.get(newIndex).mScores.size() - 1);
                    }
                }
            }
        }

        /**
         * Called when a child is moved in the database. This is handled by onChildChanged()
         *
         * @param dataSnapshot unused
         * @param key Unused
         */
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String key) {
            /* Always follows onChildChanged which caused the shuffle.
             * On onChildChanged handles it all
             */
        }

        /**
         * Called when a child is removed from the database.
         *
         * @param dataSnapshot The child removed. The date is the key and its children are
         *                     DatabaseScoreEntry objects
         */
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            /* Get the object from the database */
            Long date = Long.parseLong(dataSnapshot.getKey());

            /* Make an entry for a whole day's worth of scores */
            DatabaseDailyEntry dailyEntry = new DatabaseDailyEntry(date);

            /* Find where it is in the daily entries array */
            int index = mDailyEntries.indexOf(dailyEntry);
            if (index != -1) {
                /* If it exist, remove it and notify the adapter */
                mDailyEntries.remove(index);
                mAdapter.notifyParentItemRemoved(index);
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
    public HistoryFragment() {

    }

    /**
     * Create a view which will display the past month's scores
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
        super.onCreate(savedInstanceState);
        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        /* Set up the Recycler View */
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Animation is disabled for now
        //recyclerView.setItemAnimator(new SlideInRightAnimator(new DecelerateInterpolator()));
        //recyclerView.getItemAnimator().setAddDuration(750);
        //recyclerView.getItemAnimator().setRemoveDuration(750);
        //recyclerView.getItemAnimator().setMoveDuration(750);
        //recyclerView.getItemAnimator().setChangeDuration(750);
        mAdapter = new ExpandableRecyclerAdapter<DailyEntryHolder, ScoreEntryHolder>(mDailyEntries) {

            @Override
            public DailyEntryHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
                View statCard = getActivity().getLayoutInflater()
                        .inflate(R.layout.statistics_card, parentViewGroup, false);
                return new DailyEntryHolder(statCard);
            }

            @Override
            public ScoreEntryHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
                View itemMessage = getActivity().getLayoutInflater()
                        .inflate(R.layout.statistics_card, childViewGroup, false);

                /* Show extra padding for this card */
                itemMessage.findViewById(R.id.padding_view).setVisibility(View.VISIBLE);
                return new ScoreEntryHolder(itemMessage);
            }

            @Override
            public void onBindParentViewHolder(DailyEntryHolder parentViewHolder, int position,
                                               ParentListItem parentListItem) {
                parentViewHolder.setDateText(((DatabaseDailyEntry) parentListItem).getDate());
            }

            @Override
            public void onBindChildViewHolder(ScoreEntryHolder childViewHolder, int position,
                                              Object childListItem) {
                childViewHolder.setTitleText(((DatabaseScoreEntry) childListItem).mUsername);
                childViewHolder.setPuzzleTimeText(((DatabaseScoreEntry) childListItem).getPuzzleTime());
            }
        };
        recyclerView.setAdapter(mAdapter);

        /* Attach the database */
        mHistoryScoresDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("dailyScores")
                .orderByKey()
                .limitToLast(31); /* Only display the past month */
        mHistoryScoresDatabaseReference.addChildEventListener(mHistoryScoresChildEventListener);

        return view;
    }

    /**
     * Called when the View is destroyed. Detach the database and clear out any local entries
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /* Remove the database listener */
        mHistoryScoresDatabaseReference.removeEventListener(mHistoryScoresChildEventListener);
        /* And clear the entries */
        mDailyEntries.clear();
    }

    /**
     * @return false, because this fragment should not show the floating action button
     */
    @Override
    public boolean shouldShowFab() {
        return false;
    }

}
