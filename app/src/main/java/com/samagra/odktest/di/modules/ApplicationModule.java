package com.samagra.odktest.di.modules;

import android.content.Context;

import com.samagra.commons.MainApplication;
import com.samagra.odktest.AppConstants;
import com.samagra.odktest.data.prefs.AppPreferenceHelper;
import com.samagra.odktest.data.prefs.PreferenceHelper;
import com.samagra.odktest.di.ApplicationContext;
import com.samagra.odktest.di.PreferenceInfo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final MainApplication mainApplication;

    public ApplicationModule(MainApplication application) {
        this.mainApplication = application;
    }

    @ApplicationContext
    @Provides
    Context provideContext() {
        return mainApplication.getCurrentApplication().getApplicationContext();
    }

    @Provides
    MainApplication provideApplication() {
        return mainApplication;
    }

    @Singleton
    @Provides
    PreferenceHelper providePreferenceHelper(AppPreferenceHelper appPreferenceHelper) {
        return appPreferenceHelper;
    }

    @Provides
    @PreferenceInfo
    String providePreferenceFileName() {
        return AppConstants.PREF_FILE_NAME;
    }

}
