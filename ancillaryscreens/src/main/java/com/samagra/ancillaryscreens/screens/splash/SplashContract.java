package com.samagra.ancillaryscreens.screens.splash;

import android.content.pm.PackageInfo;

import com.samagra.ancillaryscreens.base.MvpInteractor;
import com.samagra.ancillaryscreens.base.MvpPresenter;
import com.samagra.ancillaryscreens.base.MvpView;
import com.samagra.commons.MainApplication;

/**
 * The interface contract for Splash Screen. This interface contains the methods that the Model, View & Presenter
 * for Splash Screen must implement
 *
 * @author Pranav Sharma
 */
public interface SplashContract {
    interface View extends MvpView {
        void endSplashScreen();

        /**
         * This function configures the Splash Screen through the values provided to the {@link org.odk.collect.android.ODKDriver}
         * and renders it on screen. This includes the Splash screen image and other UI configurations.
         *
         * @see org.odk.collect.android.ODKDriver#init(MainApplication, int, int, int, int)
         */
        void showSimpleSplash();

        void finishActivity();

        /**
         * This function sets the activity layout and binds the UI Views.
         * This function should be called after the relevant permissions are granted to the app by the user
         */
        void showActivityLayout();
    }

    interface Interactor extends MvpInteractor {
    }

    interface Presenter<V extends View, I extends Interactor> extends MvpPresenter<V, I> {

        /**
         * Request the storage permissions which is necessary for ODK to read write data related to forms
         */
        void requestStoragePermissions();

        /**
         * Redirects the user to Home Screen
         *
         * @see com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
         */
        void moveToNextScreen();
    }
}
