package com.gelakinetic.miniscoreboard;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

/**
 * Created by Adam on 9/10/2016.
 */
public class DailyEntryHolder extends ParentViewHolder {

    private View mView;

    public DailyEntryHolder(View itemView) {
        super(itemView);
        mView = itemView;
        ((TextView)mView.findViewById(R.id.statistics_card_puzzle_time_text)).setText("");
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
