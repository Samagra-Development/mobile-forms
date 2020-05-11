package org.odk.collect.android.support;


import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.application.InfrastructureProvider;
import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.robolectric.RuntimeEnvironment;

public class RobolectricHelpers {

    private RobolectricHelpers() {}

    public static void overrideAppDependencyModule(AppDependencyModule appDependencyModule) {
//        AppDependencyComponent testComponent = DaggerAppDependencyComponent.builder()
//                .application(RuntimeEnvironment.application)
//                .appDependencyModule(appDependencyModule)
//                .build();
//        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
    }

    public static AppDependencyComponent getApplicationComponent() {
            return CollectInitialiser.INSTANCE.getComponent();

    }
}
