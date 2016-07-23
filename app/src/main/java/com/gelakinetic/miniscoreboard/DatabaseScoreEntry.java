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

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DatabaseScoreEntry {

    /* Fields in the database entry */
    public String mName;
    public int mTime;
    public int mDay;
    public int mMonth;
    public int mYear;
    public int mPuzzleSize;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class)
     */
    public DatabaseScoreEntry() {

    }

    /**
     * Constructor which initializes all fields
     *
     * @param name       The user's name for this entry
     * @param time       The time it took to solve the puzzle
     * @param day        The day of the month the puzzle was solved
     * @param month      The month the puzzle was solved
     * @param year       The year the puzzle was solved
     * @param puzzleSize The size of the solved puzzle (currently 5 or 7)
     */
    public DatabaseScoreEntry(String name, int time, int day, int month, int year, int puzzleSize) {
        this.mName = name;
        this.mTime = time;
        this.mDay = day;
        this.mMonth = month;
        this.mYear = year;
        this.mPuzzleSize = puzzleSize;
    }
}
