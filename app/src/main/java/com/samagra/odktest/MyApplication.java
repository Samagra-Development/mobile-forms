package com.samagra.odktest;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.di.FormManagementCommunicator;
import com.samagra.commons.CommonUtilities;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.InternetMonitor;
import com.samagra.commons.MainApplication;
import com.samagra.commons.Modules;
import com.samagra.commons.RxBus;
import com.samagra.odktest.di.component.ApplicationComponent;
import com.samagra.odktest.di.component.DaggerApplicationComponent;
import com.samagra.odktest.di.modules.ApplicationModule;

import org.odk.collect.android.application.CollectInitialisationListener;
import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.contracts.ComponentManager;
import org.odk.collect.android.contracts.FormManagementSectionInteractor;

import java.sql.Time;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The {@link Application} class for the app. This extends {@link Application} because the app module has a dependency on
 * the odk-collect library. Also, since the app module expresses a dependency on the commons module, the {@link Application}
 * class for app module must implement the {@link MainApplication}.
 *
 * @author Pranav Sharma
 * @see MainApplication
 */
public class MyApplication extends Application implements MainApplication, LifecycleObserver {

    protected ApplicationComponent applicationComponent;

    private Activity currentActivity = null;
    private RxBus eventBus = null;
    private static CompositeDisposable compositeDisposable = new CompositeDisposable();
    public static FirebaseRemoteConfig mFirebaseRemoteConfig;

    /**
     * All the external modules must be initialised here. This includes any modules that have an init
     * function in their drivers. Also, any application level subscribers for the event bus,
     * in this case {@link RxBus} must be defined here.
     *
     * @see AncillaryScreensDriver
     */
    @Override
    public void onCreate() {
        CollectInitialiser.INSTANCE.init(this, getApplicationContext(), new CollectInitialisationListener() {
            @Override
            public void onFailedToStart(@NotNull String message) {
                Timber.d("Fesv fev e" + "fail");
            }

            @Override
            public void onSuccess() {
                Timber.d("Fesv fev e" + "success");
            }
        });
        super.onCreate();
        eventBus = new RxBus();
        setupRemoteConfig();
        setupActivityLifecycleListeners();
        InternetMonitor.init(this);
        AncillaryScreensDriver.init(this);
        initializeFormManagementPackage();
        initBus();
    }



    private void initializeFormManagementPackage() {

        edec();
    }

    private void edec() {
        ComponentManager.registerFormManagementPackage(new FormManagementSectionInteractor());
        FormManagementCommunicator.setContract(ComponentManager.iFormManagementContract);
        ComponentManager.iFormManagementContract.setODKModuleStyle(this, R.drawable.login_bg, R.style.BaseAppTheme,
                R.style.FormEntryActivityTheme, R.style.BaseAppTheme_SettingsTheme_Dark, Long.MAX_VALUE);

    }

    private void initBus() {
        compositeDisposable.add(this.getEventBus()
                .toObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(exchangeObject -> {
                    if (exchangeObject instanceof ExchangeObject) {
                        if (((ExchangeObject) exchangeObject).to == Modules.MAIN_APP
                                && ((ExchangeObject) exchangeObject).from == Modules.ANCILLARY_SCREENS
                                && ((ExchangeObject) exchangeObject).type == ExchangeObject.ExchangeObjectTypes.SIGNAL) {
                            ExchangeObject.SignalExchangeObject signalExchangeObject = (ExchangeObject.SignalExchangeObject) exchangeObject;
                            if (signalExchangeObject.shouldStartAsNewTask) {
                                if (currentActivity != null) {
                                    CommonUtilities.startActivityAsNewTask(signalExchangeObject.intentToLaunch, currentActivity);
                                }
                            } else
                                startActivity(signalExchangeObject.intentToLaunch);
                        } else if (exchangeObject instanceof ExchangeObject.EventExchangeObject) {
                            // TODO : Remove this just for test
                            ExchangeObject.EventExchangeObject eventExchangeObject = (ExchangeObject.EventExchangeObject) exchangeObject;
                            Timber.d("Event Received %s ", eventExchangeObject.customEvents);
                            if (eventExchangeObject.to == Modules.MAIN_APP || eventExchangeObject.to == Modules.PROJECT) {
                                Timber.d("Event Received %s ", eventExchangeObject.customEvents);
                            }
                        } else if (exchangeObject instanceof ExchangeObject.NotificationExchangeObject) {
                        } else {
                            Timber.e("Received but not intended");
                        }
                    }
                }, Timber::e));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
        MultiDex.install(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} activity instance of the activity running in foreground.
     * You can use {@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)} to
     * get the currently resumed activity (activity in foreground)
     */
    @Override
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of the current {@link Application}.
     */
    @Override
    public Application getCurrentApplication() {
        return this;
    }

    /**
     * Must provide a {@link androidx.annotation.NonNull} instance of {@link RxBus} which acts as an event bus
     * for the app.
     */
    @Override
    public RxBus getEventBus() {
        return bus();
    }

    /**
     * Optional method to teardown a module after its use is complete.
     * Not all modules require to be teared down.
     */
    @Override
    public void teardownModule(Modules module) {

    }

    private void setupActivityLifecycleListeners() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                currentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings).addOnCompleteListener(task -> mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Timber.e("Remote config activate successful. Config params updated :: %s", task1.getResult());
            } else {
                Timber.e("Remote config activate failed.");
            }
        }));

    }

    public static FirebaseRemoteConfig getmFirebaseRemoteConfig() {
        return mFirebaseRemoteConfig;
    }

    private RxBus bus() {
        return eventBus;
    }

    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return ProcessLifecycleOwner.get().getLifecycle();
    }
}
