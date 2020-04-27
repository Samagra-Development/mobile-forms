package com.samagra.odktest.ui.HomeScreen;

import android.view.View;

import com.samagra.odktest.base.MvpView;

/**
 * The view interface 'contract' for the Home Screen. This defines all the functionality required by the
 * Presenter for the view as well as for enforcing certain structure in the Views.
 * The {@link HomeActivity} <b>must</b> implement this interface. This way, the business logic behind the screen
 * can remain unaffected.
 *
 * @author Pranav Sharma
 */
public interface HomeMvpView extends MvpView {

    void renderLayoutVisible();

    void renderLayoutInvisible();
}
