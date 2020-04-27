/*
 * Copyright 2018 The Android Open Source Project
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

package com.samagra.customworkmanager.impl.utils.taskexecutor;

import com.samagra.customworkmanager.impl.utils.SerialExecutor;
import com.samagra.customworkmanager.impl.utils.SynchronousExecutor;

import java.util.concurrent.Executor;

/**
 * A TaskExecutor that runs instantly.
 */
public class InstantWorkTaskExecutor implements TaskExecutor {

    private Executor mSynchronousExecutor = new SynchronousExecutor();
    private SerialExecutor mBackgroundExecutor = new SerialExecutor(mSynchronousExecutor);

    @Override
    public void postToMainThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public Executor getMainThreadExecutor() {
        return mSynchronousExecutor;
    }

    @Override
    public void executeOnBackgroundThread(Runnable runnable) {
        runnable.run();
    }

    @Override
    public SerialExecutor getBackgroundExecutor() {
        return mBackgroundExecutor;
    }
}
