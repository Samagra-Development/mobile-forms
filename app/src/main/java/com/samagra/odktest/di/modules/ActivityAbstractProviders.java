package com.samagra.odktest.di.modules;

import com.samagra.odktest.di.PerActivity;
import com.samagra.odktest.ui.HomeScreen.HomeInteractor;
import com.samagra.odktest.ui.HomeScreen.HomeMvpInteractor;
import com.samagra.odktest.ui.HomeScreen.HomeMvpPresenter;
import com.samagra.odktest.ui.HomeScreen.HomeMvpView;
import com.samagra.odktest.ui.HomeScreen.HomePresenter;

import dagger.Binds;
import dagger.Module;

/**
 * This module is similar to previous ones, it just uses Binds instead of Provides for better performance
 * Using Binds generates a lesser number of files during build times.
 */
@Module
public abstract class ActivityAbstractProviders {
    @Binds
    @PerActivity
    abstract HomeMvpPresenter<HomeMvpView, HomeMvpInteractor> provideHomeMvpPresenter(
            HomePresenter<HomeMvpView, HomeMvpInteractor> presenter);

    @Binds
    @PerActivity
    abstract HomeMvpInteractor provideHomeMvpInteractor(HomeInteractor homeInteractor);


}
