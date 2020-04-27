package org.odk.collect.android.BackgroundRxCalls;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * This class contains all the Network calls made using RxAndroid.
 * All functions in this class must use public static methods
 */
public class WebCalls {

    /**
     * This function downloads the forms list and saves the response to sharedPreferences.
     *
     * @param context  - The Context of the activity from which this method has been called.
     * @param apiURL   - The URL used to fetch the actual data from the server.
     * @param rxEvents - The interface that allows the calling activity to receive callbacks from this API Call
     */
    public static void GetFormsListCall(Context context, String apiURL, RxEvents rxEvents) {
        Rx2AndroidNetworking.get(apiURL)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.i("OnSubscribe GetFormsList");
                        rxEvents.onSubscribe(d);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Timber.i("OnNext GetFormsList");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("FormList", jsonObject.toString());
                        editor.apply();
                        rxEvents.onNext(jsonObject);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("OnError GetFormsList %s", e.getMessage());
                        rxEvents.onError(new Throwable("Failed to get formList from server"));
                    }

                    @Override
                    public void onComplete() {
                        Timber.i("onComplete GetFormsList");
                        rxEvents.onComplete();
                    }
                });
    }


}
