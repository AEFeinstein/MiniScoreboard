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
     * @return The puzzle time of this entry, in String form
     */
    @Exclude
    public String getPuzzleTime() {
        int minutes = mPuzzleTime / 60;
        int seconds = mPuzzleTime % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Compare this DatabaseScoreEntry to another DatabaseScoreEntry, based on the puzzle time
     *
     * @param databaseScoreEntry The other DatabaseScoreEntry to compare to
     * @return 0 if they are equal, 1 if this puzzle took longer to solve, -1 if this puzzle took
     * less time to solve
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
     * Check if this DatabaseScoreEntry is equal to another. They are considered equal if the date
     * and uid are the same
     *
     * @param obj Another DatabaseScoreEntry
     * @return true if the date and uid match, false if they don't or obj isn't a DatabaseScoreEntry
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DatabaseScoreEntry)
                && (mDate == ((DatabaseScoreEntry) obj).mDate)
                && mUid.equals(((DatabaseScoreEntry) obj).mUid);
    }

    public class TimeComparator implements Comparator<DatabaseScoreEntry> {

        /**
         * Compare two DatabaseScoreEntrys based on puzzle time
         *
         * @param scoreEntry1 One DatabaseScoreEntry to compare
         * @param scoreEntry2 Another DatabaseScoreEntry to compare
         * @return a negative value if scoreEntry1 is faster than scoreEntry2.
         * 0 if the two entries are equal.
         * a positive value if scoreEntry1 is slower than scoreEntry2.
         */
        @Override
        public int compare(DatabaseScoreEntry scoreEntry1, DatabaseScoreEntry scoreEntry2) {
            if (scoreEntry1.mPuzzleTime > scoreEntry2.mPuzzleTime) {
                return 1;
            } else if (scoreEntry1.mPuzzleTime < scoreEntry2.mPuzzleTime) {
                return -1;
            }
            /* If the times are equal, compare based on UID for consistency */
            return scoreEntry1.mUid.compareTo(scoreEntry2.mUid);
        }
    }

    public class DateComparator implements Comparator<DatabaseScoreEntry> {

        /**
         * Compare two DatabaseScoreEntrys based on date
         *
         * @param scoreEntry1 One DatabaseScoreEntry to compare
         * @param scoreEntry2 Another DatabaseScoreEntry to compare
         * @return a negative value if scoreEntry2 came before scoreEntry1.
         * 0 if the two entries are on the same day.
         * a positive value if scoreEntry2 is after scoreEntry1.
         */
        @Override
        public int compare(DatabaseScoreEntry scoreEntry1, DatabaseScoreEntry scoreEntry2) {
            if (scoreEntry1.mDate > scoreEntry2.mDate) {
                return -1;
            } else if (scoreEntry1.mDate < scoreEntry2.mDate) {
                return 1;
            }
            return 0;
        }
    }
}
