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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gelakinetic.miniscoreboard.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class StatsFragment extends MiniScoreboardFragment {

    /**
     * Required empty public constructor
     */
    public StatsFragment() {

    }

    /**
     * TODO
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

        /* Get the current firebase user */
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Get this user's data, and order it by date */
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reference = database.child("scores")
                .child(firebaseUser.getUid())
                .orderByChild("mDate").getRef();

        /* Set up the Recycler View */
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.statistics_recycler);
        recycler.setHasFixedSize(false);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setItemAnimator(new jp.wasabeef.recyclerview.animators.SlideInRightAnimator(new DecelerateInterpolator()));
        recycler.getItemAnimator().setAddDuration(750);
        recycler.getItemAnimator().setRemoveDuration(750);
        recycler.getItemAnimator().setMoveDuration(750);
        recycler.getItemAnimator().setChangeDuration(750);

        /* Attach the firebase database to the recycler view */
        FirebaseRecyclerAdapter<DatabaseScoreEntry, ScoreEntryHolder> mAdapter =
                new FirebaseRecyclerAdapter<DatabaseScoreEntry, ScoreEntryHolder>(DatabaseScoreEntry.class, R.layout.statistics_card, ScoreEntryHolder.class, reference) {
                    @Override
                    public void populateViewHolder(ScoreEntryHolder chatMessageViewHolder, DatabaseScoreEntry score, int position) {
                        chatMessageViewHolder.setDateText(score.getDate());
                        chatMessageViewHolder.setPuzzleTimeText(score.getTime());
                    }
                };
        AnimationAdapter animationAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animationAdapter.setInterpolator(new DecelerateInterpolator());
        animationAdapter.setDuration(750);
        recycler.setAdapter(animationAdapter);

        return view;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public boolean shouldShowFab() {
        return false;
    }

    public static class ScoreEntryHolder extends RecyclerView.ViewHolder {
        View mView;

        /**
         * TODO
         *
         * @param itemView
         */
        public ScoreEntryHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        /**
         * TODO
         *
         * @param name
         */
        public void setDateText(String name) {
            TextView field = (TextView) mView.findViewById(R.id.statistics_card_date_text);
            field.setText(name);
        }

        /**
         * TODO
         *
         * @param text
         */
        public void setPuzzleTimeText(String text) {
            TextView field = (TextView) mView.findViewById(R.id.statistics_card_puzzle_time_text);
            field.setText(text);
        }
    }
}
