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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MainActivity;
import com.gelakinetic.miniscoreboard.database.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.ui.ScoreEntryHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_DAILY_SCORES;
import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_PUZZLE_TIME;

public class DailyFragment extends MiniScoreboardFragment {

    private final ArrayList<DatabaseScoreEntry> mDailyEntries = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private Query mDailyScoreDatabaseReference;
    private final ChildEventListener mDailyScoreChildEventListener = new ChildEventListener() {
        /**
         * Called when a child is added to the database. The key for this value is the user's uid
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         * @param s Unused
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            /* Get the object from the database */
            DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
            entry.mUid = dataSnapshot.getKey();
            entry.mUsername = ((MainActivity) getActivity()).getUserNameFromUid(dataSnapshot.getKey());

            /* Insert, sorted, into the array, notify the adapter */
            int index = Collections.binarySearch(mDailyEntries, entry,
                    new DatabaseScoreEntry().new TimeComparator());
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
         * Called when a child is changed in the database. The key for this value is the user's uid
         * @param dataSnapshot The child added, a DatabaseScoreEntry
         * @param s Unused
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
                    if (newIndex > oldIndex) {
                        /* If the new index is after the old index, shift it back one because the
                         * old item is removed first */
                        newIndex--;
                    }
                    /* The entry moved, remove it first from the old index*/
                    mDailyEntries.remove(oldIndex);
                    mRecyclerView.getAdapter().notifyItemRemoved(oldIndex);
                    /* Then add it in it's new position */
                    if (newIndex > mDailyEntries.size() - 1) {
                        newIndex = mDailyEntries.size() - 1;
                    }
                    mDailyEntries.add(newIndex, entry);
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
            /* Always follows onChildChanged which caused the shuffle. On onChildChanged handles it all */
        }

        /**
         * Called when a child is removed from the database. The key for this value is the user's uid
         * @param dataSnapshot The child added, a DatabaseScoreEntry
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
    public DailyFragment() {

    }

    /**
     * Create a view which will display the daily scores
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
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

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
            /**
             * Called when each individual view is created in the RecyclerView
             *
             * @param parent The ViewGroup to add this ScoreEntryHolder to
             * @param viewType Unused
             * @return A ScoreEntryHolder to be displayed in the RecyclerView
             */
            @Override
            public ScoreEntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemMessage = getActivity().getLayoutInflater().inflate(R.layout.statistics_card, parent, false);
                return new ScoreEntryHolder(itemMessage);
            }

            /**
             * Called when data is bound to each individual view in the RecyclerView
             *
             * @param holder The ScoreEntryHolder to bind the data to
             * @param position The position in the list to get data from
             */
            @Override
            public void onBindViewHolder(ScoreEntryHolder holder, int position) {
                if (mDailyEntries.get(position).mUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.setBold(true);
                } else {
                    holder.setBold(false);
                }
                holder.setTitleText(mDailyEntries.get(position).mUsername);
                holder.setPuzzleTimeText(mDailyEntries.get(position).getPuzzleTime());
            }

            /**
             * @return The total number of items to be displayed in this RecyclerView
             */
            @Override
            public int getItemCount() {
                return mDailyEntries.size();
            }
        });

        /* Get this daily data, and order it by date */
        mDailyScoreDatabaseReference = FirebaseDatabase.getInstance().getReference().child(KEY_DAILY_SCORES)
                .child(Long.toString(calendar.getTimeInMillis() / 1000))
                .orderByChild(KEY_PUZZLE_TIME)
                .limitToFirst(20);
        mDailyScoreDatabaseReference.addChildEventListener(mDailyScoreChildEventListener);

        /* Write the date at the top */
        String date = DateFormat
                .getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(calendar.getTime());
        ((TextView) view.findViewById(R.id.date_text_view)).setText(date);

        view.findViewById(R.id.play_puzzle_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent crosswordIntent = new Intent(Intent.ACTION_VIEW);
                crosswordIntent.setData(Uri.parse(getString(R.string.mini_crossword_url)));
                startActivity(crosswordIntent);
            }
        });

        return view;
    }

    /**
     * Called when the View is destroyed. Detach the database and clear out any local entries
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /* Remove the database listener, and clear the entries */
        mDailyScoreDatabaseReference.removeEventListener(mDailyScoreChildEventListener);
        mDailyEntries.clear();
    }

    /**
     * @return true, since this fragment should show the floating action button
     */
    @Override
    public boolean shouldShowFab() {
        return true;
    }

    @Override
    public boolean shouldShowChangeUsersButton() {
        return false;
    }
}
