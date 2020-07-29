package com.samagra.ancillaryscreens.screens.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.R;
import com.samagra.ancillaryscreens.R2;
import com.samagra.ancillaryscreens.base.BaseActivity;
import com.samagra.commons.Constants;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.Modules;

import org.odk.collect.android.application.Collect1;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * The View Part for the Splash Screen, must implement {@link SplashContract.View}
 * This Activity needs to be declared as the launcher activity in the AndroidManifest.xml
 *
 * @author Pranav Sharma
 */
public class SplashActivity extends BaseActivity implements SplashContract.View {

    private static final int SPLASH_TIMEOUT = 2000; // milliseconds

    public ImageView splashImage;
    public LinearLayout splashDefaultLayout;

    private Unbinder unbinder;

    @Inject
    SplashPresenter<SplashContract.View, SplashContract.Interactor> splashPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);
        splashPresenter.onAttach(this);
        splashPresenter.requestStoragePermissions();
    }


    @Override
    public void endSplashScreen() {
        splashPresenter.getIFormManagementContract().resetODKForms(getActivityContext());
        if (!splashPresenter.getIFormManagementContract().isScopedStorageUsed()) {
            splashPresenter.getIFormManagementContract().observeStorageMigration(getActivityContext());
        } else {
            Timber.e("Moving to Home directly from Splash");
            Intent intent = new Intent(Constants.INTENT_LAUNCH_HOME_ACTIVITY);
            ExchangeObject.SignalExchangeObject signalExchangeObject = new ExchangeObject.SignalExchangeObject(Modules.MAIN_APP, Modules.ANCILLARY_SCREENS, intent, true);
            Collect1.getInstance().getMainApplication().getEventBus().send(signalExchangeObject);
        }
        finish();
    }

    /**
     * This function configures the Splash Screen through the values provided to the
     * and renders it on screen. This includes the Splash screen image and other UI configurations.
     */
    @Override
    public void showSimpleSplash() {
        splashDefaultLayout.setVisibility(View.GONE);
        splashImage.setImageResource(R.drawable.login_bg);
        splashImage.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(this::endSplashScreen, SPLASH_TIMEOUT);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    /**
     * This function sets the activity layout and binds the UI Views.
     * This function should be called after the relevant permissions are granted to the app by the user
     */
    @Override
    public void showActivityLayout() {
        setContentView(R.layout.activity_splash);
        unbinder = ButterKnife.bind(this);
        splashImage = findViewById(R.id.splash);
        splashDefaultLayout = findViewById(R.id.splash_default);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
        splashPresenter.onDetach();
    }
}
