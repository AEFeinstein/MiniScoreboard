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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ScoreEntryHolder extends RecyclerView.ViewHolder {
    View mView;

    /**
     * TODO
     *
     * @param itemView
     */
    public ScoreEntryHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    /**
     * TODO
     *
     * @param name
     */
    public void setDateText(String name) {
        TextView field = (TextView) mView.findViewById(R.id.statistics_card_date_text);
        field.setText(name);
    }

    /**
     * TODO
     *
     * @param text
     */
    public void setPuzzleTimeText(String text) {
        TextView field = (TextView) mView.findViewById(R.id.statistics_card_puzzle_time_text);
        field.setText(text);
    }
}