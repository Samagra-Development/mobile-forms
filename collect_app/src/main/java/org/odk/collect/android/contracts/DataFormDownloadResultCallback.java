package org.odk.collect.android.contracts;

import org.odk.collect.android.logic.FormDetails;

import java.util.HashMap;

public interface DataFormDownloadResultCallback {
    void formsDownloadingSuccessful(HashMap<FormDetails, String> result);

    void formsDownloadingFailure();

    void progressUpdate(String currentFile, int progress, int total);

    void formsDownloadingCancelled();
}
