package org.odk.collect.android.support;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;


import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.application.InfrastructureProvider;
import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.logic.FormController;

public final class CollectHelpers {

    private CollectHelpers() {}

    public static FormController waitForFormController() throws InterruptedException {
        if (CollectInitialiser.INSTANCE.getFormController() == null) {
            do {
                Thread.sleep(1);
            } while (CollectInitialiser.INSTANCE.getFormController() == null);
        }

        return CollectInitialiser.INSTANCE.getFormController();
    }

    public static AppDependencyComponent getAppDependencyComponent() {
        return null;
//        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        Collect application = (Collect) context;
//        return application.getComponent();
    }
}
