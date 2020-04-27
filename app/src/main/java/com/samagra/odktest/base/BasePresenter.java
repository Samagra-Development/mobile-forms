package com.samagra.odktest.base;

import org.odk.collect.android.contracts.IFormManagementContract;

import javax.inject.Inject;

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
    private IFormManagementContract iFormManagementContract;


    @Inject
    public BasePresenter(I mvpInteractor, IFormManagementContract iFormManagementContract) {
        this.mvpInteractor = mvpInteractor;
        this.iFormManagementContract = iFormManagementContract;
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
    public void onAttach(V mvpView) {
        this.mvpView = mvpView;
    }

    @Override
    public void onDetach() {
        this.mvpView = null;
    }

    @Override
    public boolean isViewAttached() {
        return this.mvpView != null;
    }

    @Override
    public IFormManagementContract getIFormManagementContract() {
        return iFormManagementContract;
    }

}
