package com.samagra.odktest.ui.HomeScreen;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.material.snackbar.Snackbar;
import com.samagra.commons.utils.FormDownloadStatus;
import com.samagra.odktest.MyApplication;
import com.samagra.odktest.R;
import com.samagra.odktest.UtilityFunctions;
import com.samagra.odktest.base.BasePresenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.contracts.BuildCSVListener;
import org.odk.collect.android.contracts.DataFormDownloadResultCallback;
import org.odk.collect.android.contracts.FormListDownloadResultCallback;
import org.odk.collect.android.contracts.IFormManagementContract;
import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * The Presenter class for Home Screen. This class controls interaction between the View and Data.
 * This class <b>must</b> implement the {@link HomeMvpPresenter} and <b>must</b> be a type of {@link BasePresenter}.
 *
 * @author Pranav Sharma
 */
public class HomePresenter<V extends HomeMvpView, I extends HomeMvpInteractor> extends BasePresenter<V, I> implements HomeMvpPresenter<V, I> {

    private FormDownloadStatus formsDownloadStatus = FormDownloadStatus.FAILURE;

    /**
     * The injected values is provided through {@link com.samagra.odktest.di.modules.ActivityAbstractProviders}
     */
    @Inject
    public HomePresenter(I mvpInteractor, IFormManagementContract iFormManagementContract) {
        super(mvpInteractor, iFormManagementContract);
    }

    @Override
    public void applySettings() {
        getIFormManagementContract().applyODKCollectSettings(getMvpView().getActivityContext(), R.raw.settings);
    }

    @Override
    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getMvpView()
                .getActivityContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public boolean currentlyDownloading() {
        return formsDownloadStatus == FormDownloadStatus.DOWNLOADING;
    }

    @Override
    public void fetchPrefillDynamicData() {
        ArrayList<String> formName = new ArrayList<>();
        formName.add("Homework");
        getIFormManagementContract().startPreFillTask(getMvpView().getActivityContext(), new BuildCSVListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        },formName );
//        ArrayList<ArrayList<String>> data = getIFormManagementContract().readDownloadedFormReferenceCSV(getMvpView().getActivityContext());
    }

    private String getUserRoleFromPref() {
        return "Sample";
//        return getMvpInteractor().getPreferenceHelper().getUserRoleFromPref(); //Viewing and download of forms is based on User's role, you can configure it via Preferences when logging in as per User's Login response
    }


    private String getRoleFromRoleMappingFirebase(String userRole) {
        if (userRole.equals("")) return "all_grades";
        class RoleMapping {
            private String Designation;
            private String Role;

            private RoleMapping(String Designation, String Role) {
                this.Designation = Designation;
                this.Role = Role;
            }
        }


        String roleMapping = MyApplication.getmFirebaseRemoteConfig().getString("role_mapping");
        Timber.e("Role Mapping :: ");
        Timber.e("Role Mapping from firebase  is %s", roleMapping);
        String role = "";
        ArrayList<RoleMapping> roleMappings = new ArrayList<>();
        if (!roleMapping.equals("") && !userRole.equals("")) {
            try {
                boolean found = false;
                JSONArray jsonArray = new JSONArray(roleMapping);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    roleMappings.add(new RoleMapping(
                            object.getString("Designation"),
                            object.getString("Role"))
                    );
                    if (roleMappings.get(i).Designation.equals(userRole)) {
                        found = true;
                        role = roleMappings.get(i).Role;
                    }
                }
                if (!found) role = "All";
            } catch (JSONException e) {
                e.printStackTrace();
                role = "All";
            }
        }
        return role;
    }

    void checkForFormUpdates() {
        String latestFormVrsion = MyApplication.getmFirebaseRemoteConfig().getString("version");
        String previousVersion = getMvpInteractor().getPreferenceHelper().getFormVersion();
        String formsString = MyApplication.getmFirebaseRemoteConfig().getString(getRoleFromRoleMappingFirebase(getUserRoleFromPref()));
        formsDownloadStatus = FormDownloadStatus.DOWNLOADING;
        Timber.e("Are forms matching: %s", getIFormManagementContract().checkIfODKFormsMatch(formsString));
        if (isUpversioned(latestFormVrsion, previousVersion) || !getIFormManagementContract().checkIfODKFormsMatch(formsString)) {
            getMvpInteractor().getPreferenceHelper().updateFormVersion(latestFormVrsion);
            // Downloading new forms list.
            getIFormManagementContract().startDownloadODKFormListTask(new FormListDownloadListener());
            formsDownloadStatus = FormDownloadStatus.DOWNLOADING;
        } else {
            if (getMvpView() != null) {
                Timber.d("Rendering UI Visible as forms already downloaded");
                getMvpView().showSnackbar("Forms have already been downloaded.", Snackbar.LENGTH_LONG);
                getMvpView().renderLayoutVisible();
            }
            formsDownloadStatus = FormDownloadStatus.SUCCESS;
        }
    }

    public void onViewFormsClicked() {
        getIFormManagementContract().launchFormChooserView(getMvpView().getActivityContext(), UtilityFunctions.generateToolbarModificationObject(true,
                R.drawable.ic_arrow_back_white_24dp, "Choose Forms", true));
    }


    public void onViewSpecificFormClicked() {
        //You should know Forms'name

        getIFormManagementContract().launchSpecificDataForm(getMvpView().getActivityContext(), "All Widgets");
//        getIFormManagementContract().launchFormChooserView(getMvpView().getActivityContext(), UtilityFunctions.generateToolbarModificationObject(true,
//                R.drawable.ic_arrow_back_white_24dp, "Choose Forms", true));
    }

    private boolean isUpversioned(String version, String previousVersion) {
        try {
            return Integer.parseInt(version) > Integer.parseInt(previousVersion);
        } catch (Exception e) {
            return false;
        }
    }


    class FormListDownloadListener implements FormListDownloadResultCallback {
        @Override
        public void onSuccessfulFormListDownload(HashMap<String, FormDetails> latestFormListFromServer) {
            Timber.d("FormList download complete %s", latestFormListFromServer);
            String formsString = MyApplication.getmFirebaseRemoteConfig().getString(getRoleFromRoleMappingFirebase(getUserRoleFromPref()));
            HashMap<String, String> userRoleBasedForms = getIFormManagementContract().downloadFormList(formsString);
            // Download Forms if updates available or if forms not downloaded. Delete forms if not applied for the role.
            HashMap<String, String> formsToBeDownloaded =getIFormManagementContract().downloadNewFormsBasedOnDownloadedFormList(userRoleBasedForms, latestFormListFromServer);
//                    new HashMap<>();
//formsToBeDownloaded.put("sample_homework","Homework")/;
//
            if (formsToBeDownloaded.size() > 0)
                formsDownloadStatus = FormDownloadStatus.DOWNLOADING;
            else {
                formsDownloadStatus = FormDownloadStatus.SUCCESS;
                getMvpView().renderLayoutVisible();
            }
            if (formsDownloadStatus == FormDownloadStatus.DOWNLOADING)
                getIFormManagementContract().downloadODKForms(new FormDownloadListener(), formsToBeDownloaded);
        }

        @Override
        public void onFailureFormListDownload(boolean isAPIFailure) {
            if (isAPIFailure) {
                Timber.e("There has been an error in downlaoding the forms from Aggregagte");
                getMvpView().showSnackbar("There has been an error in downlaoding the forms from Aggregagte. \n" +
                        "Please check if URL is valid or not and ODK configs are alright.", Snackbar.LENGTH_LONG);
                getMvpView().renderLayoutVisible();
                formsDownloadStatus = FormDownloadStatus.FAILURE;
            }
            //+ Show error Message
//            checkForFormUpdates();
        }
    }


    class FormDownloadListener implements DataFormDownloadResultCallback {
        @Override
        public void formsDownloadingSuccessful(HashMap<FormDetails, String> result) {
            Timber.d("Form Download Complete %s", result);
            formsDownloadStatus = FormDownloadStatus.SUCCESS;
            if (getMvpView() != null)
                getMvpView().renderLayoutVisible();
        }

        @Override
        public void formsDownloadingFailure() {

        }

        @Override
        public void progressUpdate(String currentFile, int progress, int total) {
            Timber.v("Form Download InProgress = " + currentFile + " Progress" + progress + " Out of=" + total);
            Timber.d(String.valueOf(total));
            Timber.d(String.valueOf(progress));
            int formProgress = (progress * 100) / total;
            Timber.d("Form Download Progress: %s", formProgress);
            if (formProgress == 100) {
                if (getMvpView() != null) {
                    Timber.d("Rendering UI Visible as forms already downloadded not, but now downloaded");
                    getMvpView().renderLayoutVisible();
                    getMvpView().showSnackbar("ODK forms as requested have been downloaded.", Snackbar.LENGTH_LONG);
                }
                formsDownloadStatus = FormDownloadStatus.SUCCESS;
            }
        }

        @Override
        public void formsDownloadingCancelled() {
            getMvpView().showSnackbar("Unable to download the forms.", Snackbar.LENGTH_LONG);
            Timber.e("Form Download Cancelled");
        }
    }

}