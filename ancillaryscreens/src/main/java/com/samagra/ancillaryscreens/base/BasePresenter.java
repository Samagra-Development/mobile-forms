package com.samagra.ancillaryscreens.base;

import org.odk.collect.android.contracts.IFormManagementContract;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * A class that serves as a base for all the presenters (handles business logic) for the activities (serves as view).
 * The class uses Java Generics. The V and I stands for View and Interactor respectively. Since View and Interactors
 * are different for each activity, Java Generics are used. The class must implement {@link MvpPresenter}.
 *
 * @author Pranav Sharma
 */
public class BasePresenter<V extends MvpView, I extends MvpInteractor> implements MvpPresenter<V, I> {

    private V mvpView;
    private I mvpInteractor;
    private CompositeDisposable compositeDisposable;
    private IFormManagementContract iFormManagementContract;

    /**
     * These dependencies are provided by the {@link com.samagra.ancillaryscreens.di.modules.CommonsActivityModule} and
     * {@link com.samagra.ancillaryscreens.di.modules.CommonsActivityAbstractProviders} and are required by the Presenter
     * to carry out business logic tasks related to database access and/or netwrok access.
     *
     * @param mvpInteractor       - The interactor for the Activity. Must implement {@link MvpInteractor}
     * @param compositeDisposable - A {@link CompositeDisposable} object that keeps a track of all the API calls being
     *                            made in the current activity context. This object allows you to dispose all the calls
     *                            at the same time if need be (like in the event of back press during an API call)
     */
    @Inject
    public BasePresenter(I mvpInteractor, CompositeDisposable compositeDisposable, IFormManagementContract iFormManagementContract) {
        this.mvpInteractor = mvpInteractor;
        this.iFormManagementContract = iFormManagementContract;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public V getMvpView() {
        return mvpView;
    }

    @Override
    public I getMvpInteractor() {
        return mvpInteractor;
    }

    @Override
    public IFormManagementContract getIFormManagementContract() {
        return iFormManagementContract;
    }

    @Override
    public CompositeDisposable getCompositeDisposable() {
        return this.compositeDisposable;
    }

    @Override
    public void onAttach(V mvpView) {
        this.mvpView = mvpView;
    }

    @Override
    public void onDetach() {
        this.mvpView = null;
        if (this.compositeDisposable != null)
            this.compositeDisposable.dispose();
    }

    @Override
    public boolean isViewAttached() {
        return this.mvpView != null;
    }
}
