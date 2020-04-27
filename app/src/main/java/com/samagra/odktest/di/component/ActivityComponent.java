package com.samagra.odktest.di.component;

import com.samagra.odktest.di.modules.ActivityAbstractProviders;
import com.samagra.odktest.ui.HomeScreen.HomeActivity;
import com.samagra.odktest.di.PerActivity;
import com.samagra.odktest.di.modules.ActivityModule;

import dagger.Component;

/**
 * A @{@link Component} annotated interface defines connection between provider of objects (@{@link dagger.Module}
 * and the objects which express a dependency. It is implemented internally by Dagger at build time.
 */
@PerActivity
@Component(modules = {ActivityModule.class, ActivityAbstractProviders.class}, dependencies = {ApplicationComponent.class})
public interface ActivityComponent {

    void inject(HomeActivity homeActivity);

}
