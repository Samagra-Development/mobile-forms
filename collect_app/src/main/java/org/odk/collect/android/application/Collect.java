/*
 * Copyright (C) 2017 University of Washington
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

package org.odk.collect.android.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.jobs.CollectJobCreator;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.preferences.FormMetadataMigrator;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.android.tasks.sms.SmsSentBroadcastReceiver;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.NotificationUtils;
import org.odk.collect.android.utilities.PRNGFixes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.logic.PropertyManager.SCHEME_USERNAME;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USERNAME;
import static org.odk.collect.android.tasks.sms.SmsNotificationReceiver.SMS_NOTIFICATION_ACTION;
import static org.odk.collect.android.tasks.sms.SmsSender.SMS_SEND_ACTION;

/**
 * The Open Data Kit Collect application.
 *
 * @author carlhartung
 */
public class Collect extends Application {

    // Storage paths





    private Tracker tracker;









    /*
        Adds support for multidex support library. For more info check out the link below,
        https://developer.android.com/studio/build/multidex.html
    */


    @Override
    public void onCreate() {
        super.onCreate();

//        defaultSysLanguage = Locale.getDefault().getLanguage();
//        new LocaleHelper().updateLocale(this);
    }







//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        //noinspection deprecation
//        defaultSysLanguage = newConfig.locale.getLanguage();
//        boolean isUsingSysLanguage = GeneralSharedPreferences.getInstance().get(KEY_APP_LANGUAGE).equals("");
//        if (!isUsingSysLanguage) {
//            new LocaleHelper().updateLocale(this);
//        }
//    }


















}
