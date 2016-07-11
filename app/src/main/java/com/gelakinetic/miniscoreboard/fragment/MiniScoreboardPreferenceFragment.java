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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.firebase.ui.auth.AuthUI;
import com.gelakinetic.miniscoreboard.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MiniScoreboardPreferenceFragment extends PreferenceFragmentCompat {
    public static final int RES_CODE_ACCT_DELETED = 841291;
    public static final int RES_CODE_SIGNED_OUT = 218652;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        findPreference(getString(R.string.pref_key_sign_out_btn))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AuthUI.getInstance()
                                .signOut(getActivity())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            getActivity().setResult(RES_CODE_SIGNED_OUT);
                                            getActivity().finish();
                                        } else {
                                            ((MiniScoreboardPreferenceActivity) getActivity()).showSnackbar(R.string.sign_out_failed);
                                        }
                                    }
                                });
                        return true;
                    }
                });

        findPreference(getString(R.string.pref_key_pref_delete_acct_btn))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog dialog = new AlertDialog.Builder(getContext())
                                .setMessage("Are you sure you want to delete this account?")
                                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteAccount();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .create();

                        dialog.show();
                        return true;
                    }
                });
    }


    /**
     * TODO
     */
    @MainThread
    private void deleteAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (null != currentUser) {
            Task<Void> task = currentUser.delete();
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        getActivity().setResult(RES_CODE_ACCT_DELETED);
                        getActivity().finish();
                    } else {
                        ((MiniScoreboardPreferenceActivity) getActivity()).showSnackbar(R.string.delete_account_failed);
                    }
                }
            });
        }
    }
}
