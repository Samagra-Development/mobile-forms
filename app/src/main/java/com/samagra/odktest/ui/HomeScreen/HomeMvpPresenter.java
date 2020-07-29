package com.samagra.odktest.ui.HomeScreen;

import android.view.View;

import com.samagra.odktest.base.MvpPresenter;
import com.samagra.odktest.di.PerActivity;

import java.util.HashMap;

/**
 * The Presenter 'contract' for the HomeScreen. The {@link HomePresenter} <b>must</b> implement this interface.
 * This interface exposes presenter methods to the view ({@link HomeActivity}) so that the business logic is defined
 * in the presenter, but can be called from the view.
 * This interface should be a type of {@link MvpPresenter}
 *
 * @author Pranav Sharma
 */
@PerActivity
public interface HomeMvpPresenter<V extends HomeMvpView, I extends HomeMvpInteractor> extends MvpPresenter<V, I> {

    //TODO : Write Documentation
    void applySettings();

    boolean isNetworkConnected();

    boolean currentlyDownloading();

    void startStorageMigration();
}
