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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.gelakinetic.miniscoreboard.R;

public class AboutDialogFragment extends DialogFragment {

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

        String versionName = "";
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            /* Eat it */
        }

        /* Display the dialog */
        return new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle(getString(R.string.app_name) + " " + versionName)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.thanks, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    private class UsernameUidPair implements Comparable<UsernameUidPair> {
        String mUsername;
        String mUid;

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
