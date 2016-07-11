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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

public class AuthUiActivity extends Activity {

    private static final String UNCHANGED_CONFIG_VALUE = "CHANGE-ME";

    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";

    private static final int RC_SIGN_IN = 100;

    private View mRootView;

    @MainThread
    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, AuthUiActivity.class);
        return in;
    }

    @MainThread
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_auth_ui);
        mRootView = findViewById(android.R.id.content);

        if (!isGoogleConfigured()) {
            showSnackbar(R.string.configuration_required);
        } else {
            if (auth.getCurrentUser() != null) {
                startActivity(MainActivity.createIntent(this));
                finish();
            } else {
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                                .setTheme(R.style.AppTheme)
                                .setLogo(R.mipmap.ic_launcher)
                                .setProviders(AuthUI.GOOGLE_PROVIDER)
                                .setTosUrl(GOOGLE_TOS_URL)
                                .build(),
                        RC_SIGN_IN);
            }
        }
    }

    @MainThread
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode) {
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
    }

    @MainThread
    private boolean isGoogleConfigured() {
        return !UNCHANGED_CONFIG_VALUE.equals(
                getResources().getString(R.string.default_web_client_id));
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
