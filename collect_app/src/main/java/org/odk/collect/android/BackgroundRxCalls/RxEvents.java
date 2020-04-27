package org.odk.collect.android.BackgroundRxCalls;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public interface RxEvents {
    default void onSubscribe(Disposable disposable) {
        Timber.i("On Subscribe");
    }

    default void onNext(Object object) {
        Timber.i("On Next %s", object.toString());
    }

    default void onComplete() {
        Timber.i("On Complete");
    }

    default void onError(Throwable e) {
        Timber.e("On Error %s", e.getMessage());
    }
}
