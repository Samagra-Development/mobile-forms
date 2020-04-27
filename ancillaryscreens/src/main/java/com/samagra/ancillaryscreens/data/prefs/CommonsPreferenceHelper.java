package com.samagra.ancillaryscreens.data.prefs;


/**
 * Interface defining the access point to the SharedPreference used by the ancillaryscreens module.
 * All access functions to be implemented by a single solid implementation of this interface.
 *
 * @author Pranav Sharma
 * @see CommonsPrefsHelperImpl
 */
public interface CommonsPreferenceHelper {

    boolean isShowSplash();

    Long getLastAppVersion();


    void updateLastAppVersion(long updatedVersion);

    void updateFirstRunFlag(boolean value);

    boolean isLoggedIn();

    boolean isFirstRun();


    int getPreviousVersion();

    void updateAppVersion(int currentVersion);
}
