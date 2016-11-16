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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.gelakinetic.miniscoreboard.activity.MainActivity;
import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.fragment.StatsFragment;

import java.util.ArrayList;
import java.util.Collections;

public class AllUsersDialogFragment extends DialogFragment {

    private String[] mUids = null;

    /**
     * This is overridden to display a dialog built with AlertDialog.Builder.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a
     *                           freshly created Fragment.
     * @return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        /* Get all the usernames and UIDs, move them from a HashMap to an ArrayList for sorting */
        ArrayList<UsernameUidPair> users = new ArrayList<>();
        MainActivity activity = (MainActivity) getActivity();
        for (String uid : activity.mUsernameHashMap.keySet()) {
            users.add(new UsernameUidPair(uid, activity.mUsernameHashMap.get(uid)));
        }
        Collections.sort(users);

        /* Copy the usernames and UIDs to regular arrays */
        String[] usernames = new String[users.size()];
        mUids = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            usernames[i] = users.get(i).mUsername;
            mUids[i] = users.get(i).mUid;
        }

        /* Display the dialog */
        return new AlertDialog.Builder(getContext())
                .setItems(usernames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /* Find the one StatsFragment, tell it to switch the user */
                        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                            if (fragment instanceof StatsFragment) {
                                ((StatsFragment) fragment).setUser(mUids[i]);
                                return;
                            }
                        }
                    }
                })
                .setTitle(R.string.menu_title_change_user)
                .show();
    }

    private class UsernameUidPair implements Comparable<UsernameUidPair> {
        final String mUsername;
        final String mUid;

        /**
         * TODO document
         *
         * @param uid
         * @param username
         */
        UsernameUidPair(String uid, String username) {
            mUid = uid;
            mUsername = username;
        }

        /**
         * TODO document
         *
         * @param other
         * @return
         */
        @Override
        public int compareTo(@NonNull UsernameUidPair other) {
            return this.mUsername.compareTo(other.mUsername);
        }
    }
}
