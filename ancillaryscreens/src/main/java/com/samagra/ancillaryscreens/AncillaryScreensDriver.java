package com.samagra.ancillaryscreens;

import androidx.annotation.NonNull;

import com.samagra.commons.MainApplication;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * The driver class for this module, any screen that needs to be launched from outside this module, should be
 * launched using this class.
 * Note: It is essential that you call the {@link AncillaryScreensDriver#init(MainApplication)} to initialise
 * the class prior to using it.
 *
 * @author Pranav Sharma
 */
public class AncillaryScreensDriver {
    public static MainApplication mainApplication = null;

    public static void init(@NonNull MainApplication mainApplication) {
        AncillaryScreensDriver.mainApplication = mainApplication;
    }


}
