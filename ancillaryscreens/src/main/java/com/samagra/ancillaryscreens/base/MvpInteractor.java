package com.samagra.ancillaryscreens.base;

import com.samagra.ancillaryscreens.data.prefs.CommonsPreferenceHelper;

/**
 * This is the base interface that all 'Interactor Contracts' must extend.
 * Methods may be added as and when required.
 *
 * @author Pranav Sharma
 */
public interface MvpInteractor {
    CommonsPreferenceHelper getPreferenceHelper();
}
