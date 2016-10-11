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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.hmspicker.HmsPicker;
import com.gelakinetic.miniscoreboard.MainActivity;
import com.gelakinetic.miniscoreboard.R;

import java.text.DateFormat;
import java.util.Calendar;

public class ScoreInputDialogFragment extends DialogFragment
        implements CalendarDatePickerDialogFragment.OnDateSetListener {

    /* The currently selected date */
    private Calendar mCalendar;

    /* UI Elements */
    private AppCompatSpinner mPuzzleSizeSpinner;
    private AppCompatButton mDateButton;
    private HmsPicker mTimePicker;

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
                .inflate(R.layout.dialog_score_input, null);

        /* Get today's date */
        mCalendar = Calendar.getInstance();

        /* Clear all but the year, month, and day */
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        mCalendar.setTimeInMillis(0);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);

        /* Set up the button to modify the date, and set the button text as the date */
        mDateButton = (AppCompatButton) customView.findViewById(R.id.date_button);
        setDateButtonText();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Display a Calendar Date Picker dialog */
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setPreselectedDate(
                                mCalendar.get(Calendar.YEAR),
                                mCalendar.get(Calendar.MONTH),
                                mCalendar.get(Calendar.DAY_OF_MONTH))
                        .setDoneText(getContext().getResources().getString(R.string.button_ok_text))
                        .setCancelText(
                                getContext().getResources().getString(R.string.button_cancel_text))
                        .setThemeLight()
                        .setOnDateSetListener(ScoreInputDialogFragment.this);
                cdp.show(getActivity().getSupportFragmentManager(), MainActivity.DATE_PICKER_TAG);
            }
        });

        /* Get a reference to the spinner, though don't do anything with it for now */
        mPuzzleSizeSpinner = (AppCompatSpinner) customView.findViewById(R.id.puzzle_size_spinner);

        /* Set up the time picker */
        mTimePicker = (HmsPicker) customView.findViewById(R.id.hms_picker);
        mTimePicker
                .setTheme(com.codetroopers.betterpickers.R.style.BetterPickersDialogFragment_Light);
        mTimePicker.setPlusMinusVisibility(View.INVISIBLE);

        /* Display the dialog */
        return new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setView(customView)
                .setTitle(R.string.score_input_title)
                .setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        /* Get the array that maps spinner position to puzzle size */
                        int sizeValues[] = getResources().getIntArray(R.array.puzzle_sizes_values);

                        /* Have the activity submit the data */
                        ((MainActivity) getActivity()).submitNewScore(
                                mCalendar.getTimeInMillis() / 1000,
                                mTimePicker.getTime(),
                                sizeValues[mPuzzleSizeSpinner.getSelectedItemPosition()]);

                        /* We're done here */
                        dialogInterface.dismiss();

                        /* Save the time this puzzle was submitted */
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(getString(R.string.pref_key_last_submission), System.currentTimeMillis());
                        editor.apply();
                    }
                })
                .setNegativeButton(R.string.button_cancel_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            /* We're done here */
                                dialogInterface.dismiss();
                            }
                        })
                .show();
    }

    /**
     * This callback is called when the CalendarDatePickerDialogFragment closes and the user
     * submitted a date
     *
     * @param dialog      A reference to the dialog which was closed
     * @param year        The year the user selected
     * @param monthOfYear The month
     * @param dayOfMonth  The day of the month the user selected
     */
    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear,
                          int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setDateButtonText();
    }

    /**
     * Sets the text of the date button to the currently selected date
     */
    private void setDateButtonText() {
        mDateButton.setText(DateFormat.getDateInstance().format(mCalendar.getTime()));
    }
}
