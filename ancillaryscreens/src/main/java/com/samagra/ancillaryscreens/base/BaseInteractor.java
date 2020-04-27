package com.samagra.ancillaryscreens.base;

import com.samagra.ancillaryscreens.data.prefs.CommonsPreferenceHelper;

import javax.inject.Inject;

/**
 * A base for all the interactors (Java classes that serves as links between presenter (business logic) and database.
 * This class includes functionality that must be implemented by all the Interactors. Must implement {@link MvpInteractor}.
 *
 * @author Pranav Sharma
 */
public class BaseInteractor implements MvpInteractor {
    private final CommonsPreferenceHelper preferenceHelper;

    @Inject
    public BaseInteractor(CommonsPreferenceHelper preferenceHelper) {
        this.preferenceHelper = preferenceHelper;
    }

    @Override
    public CommonsPreferenceHelper getPreferenceHelper() {
        return preferenceHelper;
    }
}
