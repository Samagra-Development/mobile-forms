/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.samagra.commons.ExchangeObject;
import com.samagra.commons.Modules;

import org.odk.collect.android.BackgroundRxCalls.RxEvents;
import org.odk.collect.android.BackgroundRxCalls.UnzipDataTask;
import org.odk.collect.android.BackgroundRxCalls.WebCalls;
import org.odk.collect.android.ODKDriver;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.PermissionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SPLASH_PATH;

public class SplashScreenActivity extends Activity {

    private static final int SPLASH_TIMEOUT = 2000; // milliseconds
    private static final boolean EXIT = true;

    private int imageMaxWidth;
    public SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startGetFormListCall();
        startUnzipTask();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        new PermissionUtils().requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    Collect.createODKDirs();
                } catch (RuntimeException e) {
                    DialogUtils.showDialog(DialogUtils.createErrorDialog(SplashScreenActivity.this,
                            e.getMessage(), EXIT), SplashScreenActivity.this);
                    return;
                }

                init();
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finish();
            }
        });
    }

    private void init() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        imageMaxWidth = displayMetrics.widthPixels;

        setContentView(R.layout.splash_screen);

        // get the shared preferences object
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // get the package info object with version number
        PackageInfo packageInfo = null;
        try {
            packageInfo =
                    getPackageManager().getPackageInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Unable to get package info");
        }

        boolean firstRun = sharedPreferences.getBoolean(GeneralKeys.KEY_FIRST_RUN, true);
        boolean showSplash =
                sharedPreferences.getBoolean(GeneralKeys.KEY_SHOW_SPLASH, false);
        String splashPath = (String) GeneralSharedPreferences.getInstance().get(KEY_SPLASH_PATH);

        // if you've increased version code, then update the version number and set firstRun to true
        if (sharedPreferences.getLong(GeneralKeys.KEY_LAST_VERSION, 0)
                < packageInfo.versionCode) {
            editor.putLong(GeneralKeys.KEY_LAST_VERSION, packageInfo.versionCode);
            editor.apply();

            firstRun = true;
        }

        // do all the first run things
        if (firstRun || showSplash) {
            editor.putBoolean(GeneralKeys.KEY_FIRST_RUN, false);
            editor.commit();
        }
        showSimpleSplash();
    }

    private void endSplashScreen() {
        ExchangeObject exchangeObject = createExchangeSignalObject();
        ODKDriver.applicationInstance.getEventBus().send(exchangeObject);
    }

    private ExchangeObject createExchangeSignalObject() {
        Intent intent = new Intent(ODKDriver.LAUNCH_INTENT_ACTION);
        return new ExchangeObject.SignalExchangeObject(Modules.MAIN_APP, Modules.COLLECT_APP, intent);
    }

    // decodes image and scales it to reduce memory consumption
    // Not used
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Timber.e(e, "Unable to close file input stream");
            }

            int scale = 1;
            if (o.outHeight > imageMaxWidth || o.outWidth > imageMaxWidth) {
                scale =
                        (int) Math.pow(
                                2,
                                (int) Math.round(Math.log(imageMaxWidth
                                        / (double) Math.max(o.outHeight, o.outWidth))
                                        / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Timber.e(e, "Unable to close file input stream");
            }
        } catch (FileNotFoundException e) {
            Timber.d(e);
        }
        return b;
    }

    // Not used
    private void startSplashScreen(String path) {

        // add items to the splash screen here. makes things less distracting.
        ImageView iv = findViewById(R.id.splash);
        LinearLayout ll = findViewById(R.id.splash_default);

        File f = new File(path);
        if (f.exists()) {
            iv.setImageBitmap(decodeFile(f));
            ll.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
        }

        // create a thread that counts up to the timeout
        Thread t = new Thread() {
            int count;

            @Override
            public void run() {
                try {
                    super.run();
                    while (count < SPLASH_TIMEOUT) {
                        sleep(100);
                        count += 100;
                    }
                } catch (Exception e) {
                    Timber.e(e);
                } finally {
                    endSplashScreen();
                }
            }
        };
        t.start();
    }

    private void showSimpleSplash() {
        ImageView iv = findViewById(R.id.splash);
        LinearLayout ll = findViewById(R.id.splash_default);
        ll.setVisibility(View.GONE);
        iv.setImageResource(ODKDriver.getSplashScreenImageRes());
        iv.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(this::endSplashScreen, SPLASH_TIMEOUT);
    }

    private void startGetFormListCall() {
        WebCalls.GetFormsListCall(this,
                "http://142.93.208.135:8080/shiksha-saathi/get-formlist-for-role", new RxEvents() {
                    @Override
                    public void onComplete() {
                        boolean firstRun = sharedPreferences.getBoolean(GeneralKeys.KEY_FIRST_RUN, true);
                        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
                            // TODO: Implement Login logic and perform asctions based on that.
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("On Error: Could not update the form list %s", e.getMessage());
                    }
                });
    }

    private void startUnzipTask() {
        UnzipDataTask unzipDataTask = new UnzipDataTask(this);
        unzipDataTask.gunzipIt();
    }
}
