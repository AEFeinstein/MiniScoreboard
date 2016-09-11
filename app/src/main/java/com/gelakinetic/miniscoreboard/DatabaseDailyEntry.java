package com.gelakinetic.miniscoreboard;

import android.support.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.google.firebase.database.Exclude;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Adam on 9/10/2016.
 */
public class DatabaseDailyEntry implements Comparable<DatabaseDailyEntry>, ParentListItem {

    public long mDate;
    public ArrayList<DatabaseScoreEntry> mScores;

    public DatabaseDailyEntry(Long date) {
        mDate = date;
        mScores = new ArrayList<>();
    }

    /*
     * a negative value if the value of this long is less than the value of object; 0 if the value
     * of this long and the value of object are equal; a positive value if the value of this long
     * is greater than the value of object.
     */

    @Override
    public int compareTo(@NonNull DatabaseDailyEntry databaseDailyEntry) {
        if (this.mDate > databaseDailyEntry.mDate) {
            return -1;
        } else if (this.mDate < databaseDailyEntry.mDate) {
            return 1;
        }
        return 0;
    }

    /**
     * Check if two DatabaseDailyEntries are equal, based on their dates
     *
     * @param obj Another DatabaseDailyEntry, hopefully
     * @return true if the dates are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatabaseDailyEntry)) {
            return false;
        } else if (((DatabaseDailyEntry) obj).mDate == this.mDate) {
            return true;
        }
        return false;
    }

    /**
     * @return The date of this entry, in String form
     */
    @Exclude
    public String getDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate * 1000);
        return DateFormat
                .getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(cal.getTime());
    }

    @Override
    public List<?> getChildItemList() {
        return mScores;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
