package com.samagra.odktest.data.prefs;

import javax.inject.Singleton;

/**
 * Interface defining the access point to {@link android.content.SharedPreferences} used by the app module.
 * All access functions to be implemented by a solid implementation of this interface. This implementation should be
 * a {@link Singleton}.
 *
 * @author Pranav Sharma
 * @see AppPreferenceHelper
 */
@Singleton
public interface PreferenceHelper {
    String getCurrentUserName();

    String getUserRoleFromPref();

    void updateFormVersion(String version);

    String getUdiseListVersion();

    String getFormVersion();
}