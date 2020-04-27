package com.samagra.odktest.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.samagra.odktest.MyApplication;
import com.samagra.odktest.di.component.ActivityComponent;
import com.samagra.odktest.di.component.DaggerActivityComponent;
import com.samagra.odktest.di.modules.ActivityModule;

import org.odk.collect.android.activities.CollectAbstractActivity;

/**
 * This abstract class serves as the Base for all other activities used in this module. The class is
 * designed to support MVP Pattern with Dagger support. Any methods that need to be executed in all
 * activities, must be mentioned here. App level configuration changes (like theme change, language change, etc)
 * can be easily made through a BaseActivity. This must implement {@link MvpView}.
 * Since the app module expresses a dependency on the odk-collect, this base activity must also
 * extend {@link CollectAbstractActivity}.
 *
 * @author Pranav Sharma
 */
public abstract class BaseActivity extends CollectAbstractActivity implements MvpView {

    private ActivityComponent activityComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ActivityComponent getActivityComponent() {
        if (activityComponent == null) {
            activityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(MyApplication.get(this).getApplicationComponent())
                    .build();
        }
        return activityComponent;
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void showSnackbar(String message, int duration) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show();
    }
}
