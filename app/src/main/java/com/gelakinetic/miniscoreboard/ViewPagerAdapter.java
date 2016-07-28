/**
 * Copyright 2016 Adam Feinstein
 * <p>
 * This file is part of Mini Scoreboard.
 * <p>
 * Mini Scoreboard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Mini Scoreboard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Mini Scoreboard.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gelakinetic.miniscoreboard;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

class ViewPagerAdapter extends FragmentPagerAdapter {

    /* The list of fragments to be displayed by this adapter */
    private final List<Fragment> mFragmentList = new ArrayList<>();
    /* The titles for each fragment */
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * TODO document
     *
     * @param manager
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * TODO document
     *
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * TODO document
     *
     * @return
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * TODO document
     *
     * @param ctx
     * @param fragment
     * @param title
     */
    public void addFragment(Context ctx, Fragment fragment, int title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(ctx.getString(title));
    }

    /**
     * TODO document
     *
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    /**
     * TODO document
     *
     * @param i
     * @return
     */
    public Object getFragment(int i) {
        return mFragmentList.get(i);
    }
}