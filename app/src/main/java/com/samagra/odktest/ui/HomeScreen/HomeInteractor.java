package com.samagra.odktest.ui.HomeScreen;

import com.samagra.odktest.base.BaseInteractor;
import com.samagra.odktest.data.prefs.PreferenceHelper;

import javax.inject.Inject;

/**
 * This class interacts with the {@link HomeMvpPresenter} and the stored app data. The class abstracts
 * the source of the originating data - This means {@link HomeMvpPresenter} has no idea if the data provided
 * by the {@link HomeInteractor} is from network, database or SharedPreferences
 * This class <b>must</b> implement {@link HomeMvpInteractor} and <b>must</b> extend {@link BaseInteractor}.
 *
 * @author Pranav Sharma
 */
public class HomeInteractor extends BaseInteractor implements HomeMvpInteractor {

    /**
     * This injected value of {@link PreferenceHelper} is provided through
     * {@link com.samagra.odktest.di.modules.ApplicationModule}
     */
    @Inject
    public HomeInteractor(PreferenceHelper preferenceHelper) {
        super(preferenceHelper);
    }
}
