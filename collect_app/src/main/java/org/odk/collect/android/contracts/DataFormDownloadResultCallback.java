package org.odk.collect.android.contracts;


import org.odk.collect.android.formmanagement.ServerFormDetails;

import java.util.HashMap;

public interface DataFormDownloadResultCallback {
    void formsDownloadingSuccessful(HashMap<ServerFormDetails, String> result);

    void formsDownloadingFailure();

    void progressUpdate(String currentFile, int progress, int total);

    void formsDownloadingCancelled();
}
