package org.odk.collect.android.contracts;

import org.odk.collect.android.logic.FormDetails;

import java.util.HashMap;

public interface FormListDownloadResultCallback {
    void onSuccessfulFormListDownload(HashMap<String, FormDetails> value);
    void onFailureFormListDownload(boolean isAPIFailure);
}
