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

package com.gelakinetic.miniscoreboard.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.gelakinetic.miniscoreboard.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

public class AuthUiActivity extends Activity {

    private static final int RC_SIGN_IN = 100;

    private View mRootView;

    /**
     * Create an Intent to launch the AuthUiActivity
     *
     * @param context a Context to create the Intent with
     * @return An Intent used to start AuthUiActivity
     */
    @MainThread
    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, AuthUiActivity.class);
        return in;
    }

    /**
     * Create the View for this activity. If the user is authenticated, start the MainActivity
     * Otherwise start the login flow
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @MainThread
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_auth_ui);
        mRootView = findViewById(android.R.id.content);

        if (auth.getCurrentUser() != null) {
            startActivity(MainActivity.createIntent(this));
            finish();
        } else {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setTheme(R.style.AppTheme)
                            .setLogo(R.mipmap.ic_launcher)
                            .setProviders(Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            /*.setTosUrl(GOOGLE_TOS_URL) TODO add a TOS? */
                            .build(),
                    RC_SIGN_IN);
        }
    }

    /**
     * Called when the AuthUiSignInIntent returns
     *
     * @param requestCode The request code, hopefully RC_SIGN_IN
     * @param resultCode  The result code, either RESULT_OK or RESULT_CANCELED
     * @param data        Unused, but it could return extra data
     */
    @MainThread
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                startActivity(MainActivity.createIntent(this));
                finish();
                return;
            }
            if (resultCode == RESULT_CANCELED) {
                showSnackbar(R.string.sign_in_cancelled);
                finish();
                return;
            }
            showSnackbar(R.string.unknown_sign_in_response);
            return;
        }
        showSnackbar(R.string.unknown_response);
    }

    /**
     * Show a little message on the Snackbar
     *
     * @param message A resource ID for a message to display
     */
    @MainThread
    private void showSnackbar(@StringRes int message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }
}
