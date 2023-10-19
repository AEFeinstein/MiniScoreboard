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

package com.gelakinetic.miniscoreboard.database;

import androidx.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.google.firebase.database.Exclude;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseDailyEntry implements Comparable<DatabaseDailyEntry>, ParentListItem {

    private final long mDate;
    public final ArrayList<DatabaseScoreEntry> mScores;

    /**
     * TODO document
     *
     * @param date
     */
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
        } else return ((DatabaseDailyEntry) obj).mDate == this.mDate;
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

    /**
     * TODO document
     *
     * @return
     */
    @Override
    public List<?> getChildItemList() {
        return mScores;
    }

    /**
     * TODO document
     *
     * @return
     */
    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
