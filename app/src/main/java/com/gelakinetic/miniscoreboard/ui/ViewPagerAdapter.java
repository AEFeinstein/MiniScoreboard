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

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    /* The list of fragments to be displayed by this adapter */
    private final List<Fragment> mFragmentList = new ArrayList<>();
    /* The titles for each fragment */
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Default constructor
     *
     * @param manager A FragmentManager to manage the fragments in this adapter
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * Return the fragment at a given position
     *
     * @param position The position of the fragment to return
     * @return A fragment at the given position
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * @return The total number of Fragments managed by this ViewPagerAdapter
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * Add a Fragment to this ViewPagerAdapter
     *
     * @param ctx      A Context to get resources with
     * @param fragment The Fragment to add to this ViewPagerAdapter
     * @param title    A resource ID for the title of this fragment
     */
    public void addFragment(Context ctx, Fragment fragment, int title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(ctx.getString(title));
    }

    /**
     * Get the title of a Fragment at the given position
     *
     * @param position The position of the Fragment to get its title
     * @return The title of the fragment
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    /**
     * Get a Fragment at the given position
     *
     * @param position The position of the Fragment to return
     * @return A Fragment at the given position
     */
    public Fragment getFragment(int position) {
        return mFragmentList.get(position);
    }
}