/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samagra.customworkmanager.impl;

import static com.samagra.customworkmanager.impl.utils.PackageManagerHelper.isComponentExplicitlyEnabled;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import com.samagra.customworkmanager.impl.background.systemalarm.SystemAlarmScheduler;
import com.samagra.customworkmanager.impl.background.systemalarm.SystemAlarmService;
import com.samagra.customworkmanager.impl.background.systemjob.SystemJobScheduler;
import com.samagra.customworkmanager.impl.background.systemjob.SystemJobService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class SchedulersTest {

    private Context mAppContext;
    private WorkManagerImpl mWorkManager;

    @Before
    public void setUp() {
        mWorkManager = mock(WorkManagerImpl.class);
        mAppContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    @SdkSuppress(minSdkVersion = WorkManagerImpl.MIN_JOB_SCHEDULER_API_LEVEL)
    public void testGetBackgroundScheduler_withJobSchedulerApiLevel() {
        Scheduler scheduler =
                Schedulers.createBestAvailableBackgroundScheduler(mAppContext, mWorkManager);
        assertThat(scheduler, is(instanceOf(SystemJobScheduler.class)));
        assertServicesEnabled(true, false);
    }

    @Test
    @SdkSuppress(maxSdkVersion = WorkManagerImpl.MAX_PRE_JOB_SCHEDULER_API_LEVEL)
    public void testGetBackgroundScheduler_beforeJobSchedulerApiLevel() {
        Scheduler scheduler =
                Schedulers.createBestAvailableBackgroundScheduler(mAppContext, mWorkManager);
        assertThat(scheduler, is(instanceOf(SystemAlarmScheduler.class)));
        assertServicesEnabled(false, true);
    }

    // Only one service should really be enabled at one time.
    private void assertServicesEnabled(boolean systemJobEnabled, boolean systemAlarmEnabled) {
        if (Build.VERSION.SDK_INT >= WorkManagerImpl.MIN_JOB_SCHEDULER_API_LEVEL) {
            assertThat(isComponentExplicitlyEnabled(mAppContext, SystemJobService.class),
                    is(systemJobEnabled));
        }
        assertThat(isComponentExplicitlyEnabled(mAppContext, SystemAlarmService.class),
                is(systemAlarmEnabled));
    }
}
