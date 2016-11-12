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
import android.view.View;
import android.widget.EditText;

import com.gelakinetic.miniscoreboard.MainActivity;
import com.gelakinetic.miniscoreboard.MiniScoreboardPreferenceActivity;
import com.gelakinetic.miniscoreboard.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserNameInputDialogFragment extends DialogFragment {

    private EditText mEditText;

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
        /* Display the dialog */
        return new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(customView)
                .setTitle(R.string.username_input_title)
                .setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createNewUserName(mEditText.getText().toString());
                    }
                })
                .show();
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
