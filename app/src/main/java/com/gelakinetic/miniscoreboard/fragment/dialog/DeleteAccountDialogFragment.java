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

package com.gelakinetic.miniscoreboard.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.database.DatabaseScoreEntry;
import com.gelakinetic.miniscoreboard.fragment.MiniScoreboardPreferenceFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_DAILY_SCORES;
import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_DAILY_WINNERS;
import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_PERSONAL_SCORES;
import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_USERS;

public class DeleteAccountDialogFragment extends DialogFragment {


    MiniScoreboardPreferenceActivity mActivity;
    int mNumCompetedTasks = 0;
    int mPendingTasks = 0;
    boolean mFoundNewWinners = false;

    private OnCompleteListener<Void> mRemoveDataCompleteListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            /* Wait for all tasks to complete, including finding new winners */
            mNumCompetedTasks++;
            if (mNumCompetedTasks == mPendingTasks && mFoundNewWinners) {
                /* Delete the user */
                FirebaseAuth.getInstance().getCurrentUser()
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            /* Account deleted, finish the activity */
                            mActivity.setResult(MiniScoreboardPreferenceFragment.RES_CODE_ACCT_DELETED);
                            mActivity.finish();
                        } else {
                            /* Account delete failed, display a message */
                            String message = mActivity.getString(R.string.delete_account_failed);
                            if (task.getException() != null) {
                                message += ": " + task.getException().getMessage();
                            }
                            mActivity.showSnackbar(message);
                        }
                    }
                });
            }
        }
    };

    /**
     * This is overridden to display a custom dialog, built with AlertDialog.Builder.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a
     *                           freshly created Fragment.
     * @return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (MiniScoreboardPreferenceActivity) getActivity();

        /* Display the dialog */
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.pref_delete_acct_title)
                .setMessage(R.string.delete_account_dialog_message)
                .setPositiveButton(R.string.delete_account_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton(R.string.delete_account_dialog_negative, null)
                .create();
    }

    /**
     * Delete this user's account & data. Each database task where some data is deleted will
     * increment mPendingTasks. Finding the new winners happens after a task is completed, so
     * there's a separate boolean for that, mFoundNewWinners. When all tasks have completed,
     * the account will be deleted from Firebase.
     */
    @MainThread
    private void deleteAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (null != currentUser) {
            /* Initialization */
            mNumCompetedTasks = 0;
            mPendingTasks = 0;
            mFoundNewWinners = false;
            final String uid = currentUser.getUid();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            /* Remove this user's username */
            mPendingTasks++;
            database.child(KEY_USERS)
                    .child(uid)
                    .removeValue()
                    .addOnCompleteListener(mRemoveDataCompleteListener);

            /* Remove this user's personal scores */
            mPendingTasks++;
            database.child(KEY_PERSONAL_SCORES)
                    .child(uid)
                    .removeValue()
                    .addOnCompleteListener(mRemoveDataCompleteListener);

            /* Remove all the times this user has won */
            mPendingTasks++;
            database.child(KEY_DAILY_WINNERS)
                    .orderByValue()
                    .startAt(uid)
                    .endAt(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue();
                            }
                            /* This task completed */
                            mRemoveDataCompleteListener.onComplete(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            /* TODO some error handling */
                        }
                    });

            /* Remove all this user's daily scores & find new winners */
            mPendingTasks++;
            database.child(KEY_DAILY_SCORES)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            /* For every day */
                            for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                                /* Keep track of this day's winners, there may be multiple */
                                ArrayList<String> winners = new ArrayList<>();
                                int winnerTime = Integer.MAX_VALUE;
                                /* For each entry this day */
                                for (DataSnapshot userSnapshot : daySnapshot.getChildren()) {
                                    DatabaseScoreEntry dailyEntry = userSnapshot.getValue(DatabaseScoreEntry.class);
                                    if (dailyEntry.mPuzzleTime < winnerTime) {
                                        /* Undisputed winner, clear out the priors */
                                        winners.clear();
                                        winners.add(userSnapshot.getKey()); /* UID */
                                        /* Record the new time */
                                        winnerTime = dailyEntry.mPuzzleTime;
                                    } else if (dailyEntry.mPuzzleTime == winnerTime) {
                                        /* Tie for the win, add the new UID */
                                        winners.add(userSnapshot.getKey()); /* UID */
                                    }
                                }

                                /* Write the winners to the database */
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                                /* Submit the data, first to daily scores then to personal scores. */
                                for (String winner : winners) {
                                    mPendingTasks++;
                                    database.child(KEY_DAILY_WINNERS)
                                            .child(daySnapshot.toString() + "-" + winners.indexOf(winner))
                                            .setValue(winner)
                                            .addOnCompleteListener(mRemoveDataCompleteListener);
                                }

                            }
                            /* This "task" completed */
                            mFoundNewWinners = true;
                            mRemoveDataCompleteListener.onComplete(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            /* TODO some error handling */
                        }
                    });
        }
    }
}
