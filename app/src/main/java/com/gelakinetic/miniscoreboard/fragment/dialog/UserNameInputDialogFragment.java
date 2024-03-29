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

import static com.gelakinetic.miniscoreboard.database.DatabaseKeys.KEY_USERS;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MainActivity;
import com.gelakinetic.miniscoreboard.activity.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.fragment.MiniScoreboardPreferenceFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserNameInputDialogFragment extends DialogFragment {

    public static final String USERNAME_KEY = "USERNAME_KEY";
    private EditText mEditText;
    private Button mPositiveButton;
    private String mOldUserName;

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

        /* Inflate the view */
        View customView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_username_input, null);

        mEditText = (EditText) customView.findViewById(R.id.user_name_edit_text);

        /* Attempt to fill in the username before it's changed */
        try {
            mOldUserName = getActivity().getIntent().getStringExtra(USERNAME_KEY);
            mEditText.setText(mOldUserName);
        } catch (Exception e) {
            /* eat it, just don't set a name */
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* Don't care */
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* Don't care */
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPositiveButton.setEnabled(editable.length() > 0);
            }
        });
        /* Display the dialog */
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(customView)
                .setTitle(R.string.username_input_title)
                .setPositiveButton(R.string.button_ok_text, (dialogInterface, i) -> {
                    String newUserName = mEditText.getText().toString().trim();
                    if (mOldUserName != null) {
                        if (!mOldUserName.equals(newUserName)) {
                            /* Username existed, but changed */
                            createNewUserName(newUserName);
                            /* This code will restart the activity & refresh the
                             * username when the preferences are exited
                             */
                            getActivity().setResult(MiniScoreboardPreferenceFragment.RES_CODE_USERNAME_CHANGED);
                        }
                        /* Otherwise the username didn't change, do nothing */
                    } else {
                        /* Username didn't exist, create it now */
                        createNewUserName(newUserName);
                    }
                })
                .show();

        /* Set it on the fragment, not the dialog! */
        this.setCancelable(false);

        /* Grab a reference to the button & set the initial enabled state */
        mPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(mEditText.getText().length() > 0);

        /* Return the dialog */
        return alertDialog;
    }

    /**
     * Create a new username for this user
     *
     * @param username A String username for this user
     */
    private void createNewUserName(String username) {

        /* Get a database reference */
        FirebaseDatabase.getInstance().getReference()
                .child(KEY_USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(username);

        /* Give a little user feedback */
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showSnackbar(R.string.user_created);
        } else if (getActivity() instanceof MiniScoreboardPreferenceActivity) {
            ((MiniScoreboardPreferenceActivity) getActivity()).showSnackbar(R.string.username_changed);
        }
    }
}
