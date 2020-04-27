package com.samagra.ancillaryscreens.base;

import android.content.Context;

/**
 * This is the Base interface that all 'View Contracts' must extend. For instance,
 * {@link com.samagra.ancillaryscreens.screens.login.LoginContract.View} extends this class and so does every other
 * Contract. Methods maybe added to it as and when required.
 *
 * @author Pranav Sharma
 */
public interface MvpView {

    Context getActivityContext();

    void showSnackbar(String message, int duration);
}
