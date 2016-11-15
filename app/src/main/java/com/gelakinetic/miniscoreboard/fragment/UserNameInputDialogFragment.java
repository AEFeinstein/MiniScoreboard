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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gelakinetic.miniscoreboard.MainActivity;
import com.gelakinetic.miniscoreboard.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserNameInputDialogFragment extends DialogFragment {

    private EditText mEditText;
    private Button mPositiveButton;

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
                if (editable.length() > 0) {
                    mPositiveButton.setEnabled(true);
                } else {
                    mPositiveButton.setEnabled(false);
                }
            }
        });
        /* Display the dialog */
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(customView)
                .setTitle(R.string.username_input_title)
                .setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createNewUserName(mEditText.getText().toString().trim());
                    }
                })
                .show();

        /* Set it on the fragment, not the dialog! */
        this.setCancelable(false);

        /* Grab a reference to the button in order to enable & disable it */
        mPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(false);

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
                .child("users")
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
