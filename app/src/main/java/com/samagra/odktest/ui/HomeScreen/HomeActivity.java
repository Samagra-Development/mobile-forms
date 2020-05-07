package com.samagra.odktest.ui.HomeScreen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.samagra.commons.InternetMonitor;
import com.samagra.odktest.R;
import com.samagra.odktest.base.BaseActivity;

import org.odk.collect.android.utilities.SnackbarUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * View part of the Home Screen. This class only handles the UI operations, all the business logic is simply
 * abstracted from this Activity. It <b>must</b> implement the {@link HomeMvpView} and extend the {@link BaseActivity}
 *
 * @author Pranav Sharma
 */
public class HomeActivity extends BaseActivity implements HomeMvpView {


    @BindView(R.id.show_specific_odk_forms)
    public Button show_specific_odk_forms;
    @BindView(R.id.show_odk_forms)
    public Button view_odk_forms;

    @BindView(R.id.circularProgressBar)
    public ProgressBar circularProgressBar;

    @BindView(R.id.download_odk_forms)
    public Button downloadODKForms;

    @BindView(R.id.parent)
    public RelativeLayout parentLayout;

    @BindView(R.id.get_dynamic_cascading_module_data)
    public Button get_dynamic_cascading_module_data;

    private PopupMenu popupMenu;

    private Unbinder unbinder;

    @Inject
    HomePresenter<HomeMvpView, HomeMvpInteractor> homePresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getActivityComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        homePresenter.onAttach(this);
        setupToolbar();
        homePresenter.applySettings();
        InternetMonitor.startMonitoringInternet();
        initializeLayout();
        setClickListener();
    }

    private void setClickListener() {
        view_odk_forms.setOnClickListener(v -> homePresenter.onViewFormsClicked());

        show_specific_odk_forms.setOnClickListener(v -> {
            homePresenter.onViewSpecificFormClicked();
        });

        get_dynamic_cascading_module_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.fetchPrefillDynamicData();
            }
        });
        downloadODKForms.setOnClickListener(v -> {
//            homePr//esenter.checkForFormUpdates();
            if (!homePresenter.isNetworkConnected()) {
                SnackbarUtils.showLongSnackbar(parentLayout, "Please connect to the internet.");
            } else {
                if (!homePresenter.currentlyDownloading()) {
                    circularProgressBar.setVisibility(View.VISIBLE);
                    homePresenter.checkForFormUpdates();
                } else {
                    Toast.makeText(getActivityContext(), "Please wait while forms are being downloaded", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        customizeToolbar();
    }


    private void initializeLayout() {
        downloadODKForms.setVisibility(View.VISIBLE);
        circularProgressBar.setVisibility(View.GONE);
        if (!homePresenter.isNetworkConnected()) {
            renderLayoutInvisible();
            SnackbarUtils.showLongSnackbar(parentLayout, "Please connect to the internet.");
        }

    }

    @Override
    public void renderLayoutVisible() {
        parentLayout.setVisibility(View.VISIBLE);
        circularProgressBar.setVisibility(View.GONE);
        view_odk_forms.setVisibility(View.VISIBLE);
        show_specific_odk_forms.setVisibility(View.VISIBLE);
        get_dynamic_cascading_module_data.setVisibility(View.VISIBLE);

    }

    @Override
    public void renderLayoutInvisible() {
        downloadODKForms.setVisibility(View.GONE);
        circularProgressBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        homePresenter.onDetach();
        unbinder.unbind();
    }

    @Override
    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    public void customizeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(this::initAndShowPopupMenu);
    }

    /**
     * Giving Control of the UI to XML file for better customization and easier changes
     */
    private void initAndShowPopupMenu(View v) {

        if (popupMenu == null) {
            popupMenu = new PopupMenu(HomeActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.home_screen_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.about_us:
                        break;
                    case R.id.profile:
                        break;
                    case R.id.tutorial_video:
                        break;
                    case R.id.logout:
                        break;
                }
                return true;
            });
        }
        popupMenu.show();
    }


}
