package com.samagra.odktest.di.modules;

import android.app.Activity;
import android.content.Context;

import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.odktest.di.ActivityContext;

import org.odk.collect.android.contracts.IFormManagementContract;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Classes marked with @{@link Module} are responsible for providing objects that can be injected.
 * Such classes define methods annotated with @{@link Provides}. The returned objects from such methods are
 * available for DI.
 */
@Module
public class ActivityModule {

    private Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return activity;
    }

    @Provides
    Activity provideActivity() {
        return activity;
    }


    @Provides
    IFormManagementContract provideIFormManagementContract() {
        return FormManagementCommunicator.getContract();
    }
}
