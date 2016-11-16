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

package com.gelakinetic.miniscoreboard.ui;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.gelakinetic.miniscoreboard.R;

public class DailyEntryHolder extends ParentViewHolder {

    private final View mView;

    /**
     * TODO document
     *
     * @param itemView
     */
    public DailyEntryHolder(View itemView) {
        super(itemView);
        mView = itemView;
        ((TextView) mView.findViewById(R.id.statistics_card_puzzle_time_text)).setText("");
    }

    /**
     * Set the date or name for the this puzzle
     *
     * @param text The date or name, in String form
     */
    public void setDateText(String text) {
        TextView field = (TextView) mView.findViewById(R.id.statistics_card_date_text);
        field.setText(text);
    }
}
