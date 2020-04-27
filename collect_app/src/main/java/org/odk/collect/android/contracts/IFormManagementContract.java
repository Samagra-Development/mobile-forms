package org.odk.collect.android.contracts;

import android.content.Context;

import com.samagra.commons.MainApplication;

import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.w3c.dom.Document;

import java.util.HashMap;


public interface IFormManagementContract {

    /**
     *
     * @param mainApplication
     * @param splashScreenDrawableID
     * @param baseAppThemeStyleID
     * @param formActivityThemeID
     * @param customThemeId_Settings
     * @param toolbarIconResId
     */
    void setODKModuleStyle(MainApplication mainApplication, int splashScreenDrawableID, int baseAppThemeStyleID,
                           int formActivityThemeID, int customThemeId_Settings, long toolbarIconResId);

    void resetPreviousODKForms();

    void resetEverythingODK();

    void createODKDirectories();

    void resetODKForms(Context context);

    boolean checkIfODKFormsMatch(String formsString);

    void startDownloadODKFormListTask(FormListDownloadResultCallback formListDownloadResultCallback);

    HashMap<String, String> downloadNewFormsBasedOnDownloadedFormList(HashMap<String, String> userRoleBasedForms, HashMap<String, FormDetails> latestFormListFromServer);

    void downloadODKForms(DataFormDownloadResultCallback dataFormDownloadResultCallback, HashMap<String, String> formsToBeDownloaded);

    HashMap<String, String> downloadFormList(String formsString);

    void initialiseODKProps();

    void applyODKCollectSettings(Context context, int inputStream);

    void launchSpecificDataForm(Context context, String formIdentifier);

    int fetchSpecificFormID(String matcher);

    void launchViewUnsubmittedFormView(Context context, String className);

    void launchViewSubmittedFormsView(Context context, HashMap<String, Object> toolbarModificationObject);

    void launchFormChooserView(Context context, HashMap<String, Object> toolbarModificationObject);


    void updateFormBasedOnIdentifier(String formIdentifier, String tag, String tagValue);
}