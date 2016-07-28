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

package com.gelakinetic.miniscoreboard;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

@IgnoreExtraProperties
public class DatabaseScoreEntry implements Comparable<DatabaseScoreEntry> {

    /* Fields in the database entry */
    public int mPuzzleTime;
    public int mPuzzleSize;
    @Exclude
    public String mUsername;
    @Exclude
    public long mDate = 0;
    @Exclude
    public String mUid = "";

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class)
     */
    public DatabaseScoreEntry() {

    }

    /**
     * Constructor which initializes all fields
     *
     * @param puzzleTime The time it took to solve the puzzle
     * @param puzzleSize The size of the solved puzzle (currently 5 or 7)
     */
    public DatabaseScoreEntry(int puzzleTime, int puzzleSize) {
        this.mPuzzleTime = puzzleTime;
        this.mPuzzleSize = puzzleSize;
    }

    /**
     * TODO
     *
     * @return
     */
    @Exclude
    public String getDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate * 1000);
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(cal.getTime());
    }

    /**
     * TODO
     *
     * @return
     */
    @Exclude
    public String getTime() {
        int minutes = mPuzzleTime / 60;
        int seconds = mPuzzleTime % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * TODO
     *
     * @param databaseScoreEntry
     * @return
     */
    @Override
    public int compareTo(@NonNull DatabaseScoreEntry databaseScoreEntry) {
        if (mPuzzleTime == databaseScoreEntry.mPuzzleTime) {
            return 0;
        } else if (mPuzzleTime > databaseScoreEntry.mPuzzleTime) {
            return 1;
        }
        return -1;
    }

    /**
     * TODO
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatabaseScoreEntry)) {
            return false;
        }
        return (mDate == ((DatabaseScoreEntry) obj).mDate) && mUid.equals(((DatabaseScoreEntry) obj).mUid);
    }

    /**
     * TODO
     *
     * @param entry
     */
    public void setValuesFrom(DatabaseScoreEntry entry) {
        this.mPuzzleTime = entry.mPuzzleTime;
        this.mPuzzleSize = entry.mPuzzleSize;
        this.mUid = entry.mUid;
        this.mDate = entry.mDate;
        this.mUsername = entry.mUsername;
    }

    public class TimeComparator implements Comparator<DatabaseScoreEntry> {

        /**
         * TODO
         * @param databaseScoreEntry
         * @param t1
         * @return
         */
        @Override
        public int compare(DatabaseScoreEntry databaseScoreEntry, DatabaseScoreEntry t1) {
            return (Integer.valueOf(databaseScoreEntry.mPuzzleTime)).compareTo(t1.mPuzzleTime);
        }
    }

    public class DateComparator implements Comparator<DatabaseScoreEntry> {

        /**
         * TODO
         * @param databaseScoreEntry
         * @param t1
         * @return
         */
        @Override
        public int compare(DatabaseScoreEntry databaseScoreEntry, DatabaseScoreEntry t1) {
            return (Long.valueOf(databaseScoreEntry.mDate)).compareTo(t1.mDate);
        }
    }
}
