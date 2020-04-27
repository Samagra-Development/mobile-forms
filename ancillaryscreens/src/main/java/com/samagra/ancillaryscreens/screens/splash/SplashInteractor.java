package com.samagra.ancillaryscreens.screens.splash;

import com.samagra.ancillaryscreens.base.BaseInteractor;
import com.samagra.ancillaryscreens.data.prefs.CommonsPreferenceHelper;

import javax.inject.Inject;

/**
 * This class interacts with the {@link com.samagra.ancillaryscreens.screens.splash.SplashContract.Presenter} and the stored
 * app data. The class abstracts the source of the originating data - This means {@link com.samagra.ancillaryscreens.screens.splash.SplashContract.Presenter}
 * has no idea if the data provided by the {@link com.samagra.ancillaryscreens.screens.splash.SplashContract.Interactor} is
 * from network, database or SharedPreferences
 *
 * @author Pranav Sharma
 */
public class SplashInteractor extends BaseInteractor implements SplashContract.Interactor {

    @Inject
    public SplashInteractor(CommonsPreferenceHelper preferenceHelper) {
        super(preferenceHelper);
    }

}