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
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.firebase.ui.auth.AuthUI;
import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MainActivity;
import com.gelakinetic.miniscoreboard.activity.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.fragment.dialog.DeleteAccountDialogFragment;
import com.gelakinetic.miniscoreboard.fragment.dialog.UserNameInputDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MiniScoreboardPreferenceFragment extends PreferenceFragmentCompat {
    public static final int RES_CODE_ACCT_DELETED = 841291;
    public static final int RES_CODE_SIGNED_OUT = 218652;
    public static final int RES_CODE_USERNAME_CHANGED = 539471;

    /**
     * Create the preferences from XML, then attach some listeners to the preferences which are
     * actually buttons
     *
     * @param bundle Unused
     * @param s      Unused
     */
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        findPreference(getString(R.string.pref_key_pref_change_username_btn))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UserNameInputDialogFragment newFragment = new UserNameInputDialogFragment();
                        newFragment.show(MiniScoreboardPreferenceFragment.this.getActivity().getSupportFragmentManager(),
                                MainActivity.DIALOG_TAG);
                        return true;
                    }
                });

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
                                            ((MiniScoreboardPreferenceActivity) getActivity())
                                                    .showSnackbar(R.string.sign_out_failed);
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
                        DeleteAccountDialogFragment newFragment = new DeleteAccountDialogFragment();
                        newFragment.show(MiniScoreboardPreferenceFragment.this.getActivity().getSupportFragmentManager(),
                                MainActivity.DIALOG_TAG);
                        return true;
                    }
                });
    }
}
