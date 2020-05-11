/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.content.Context;


import org.odk.collect.android.application.CollectInitialiser;
import org.odk.collect.android.application.InfrastructureProvider;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResetUtility {

    private List<Integer> failedResetActions;

    public List<Integer> reset(Context context, List<Integer> resetActions) {

        failedResetActions = new ArrayList<>();
        failedResetActions.addAll(resetActions);

        for (int action : resetActions) {
            switch (action) {
                case ResetAction.RESET_PREFERENCES:
                    resetPreferences(context);
                    break;
                case ResetAction.RESET_INSTANCES:
                    resetInstances();
                    break;
                case ResetAction.RESET_FORMS:
                    resetForms();
                    break;
                case ResetAction.RESET_LAYERS:
                    if (deleteFolderContents(CollectInitialiser.INSTANCE.getOFFLINE_LAYERS())) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_LAYERS));
                    }
                    break;
                case ResetAction.RESET_CACHE:
                    if (deleteFolderContents(CollectInitialiser.INSTANCE.getCACHE_PATH())) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_CACHE));
                    }
                    break;
                case ResetAction.RESET_OSM_DROID:
                    if (deleteFolderContents(Configuration.getInstance().getOsmdroidTileCache().getPath())) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_OSM_DROID));
                    }
                    break;
            }
        }

        return failedResetActions;
    }

    private void resetPreferences(Context context) {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        AdminSharedPreferences.getInstance().loadDefaultPreferences();

        boolean deletedSettingsFolderContest = !new File(CollectInitialiser.INSTANCE.getSETTINGS()).exists()
                || deleteFolderContents(CollectInitialiser.INSTANCE.getSETTINGS());

        boolean deletedSettingsFile = !new File(CollectInitialiser.INSTANCE.getODK_ROOT() + "/CollectInitialiser.INSTANCE.getSETTINGS()").exists()
                || (new File(CollectInitialiser.INSTANCE.getODK_ROOT() + "/CollectInitialiser.INSTANCE.getSETTINGS()").delete());
        
        new LocaleHelper().updateLocale(context);

        if (deletedSettingsFolderContest && deletedSettingsFile) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_PREFERENCES));
        }

        CollectInitialiser.INSTANCE.initProperties();
    }

    private void resetInstances() {
        new InstancesDao().deleteInstancesDatabase();

        if (deleteFolderContents(CollectInitialiser.INSTANCE.getINSTANCES_PATH())) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_INSTANCES));
        }
    }

    private void resetForms() {
        new FormsDao().deleteFormsDatabase();

        File itemsetDbFile = new File(CollectInitialiser.INSTANCE.getMETADATA_PATH() + File.separator + ItemsetDbAdapter.DATABASE_NAME);

        if (deleteFolderContents(CollectInitialiser.INSTANCE.getFORMS_PATH()) && (!itemsetDbFile.exists() || itemsetDbFile.delete())) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_FORMS));
        }
    }

    private boolean deleteFolderContents(String path) {
        boolean result = true;
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result = deleteRecursive(f);
                }
            }
        }
        return result;
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    public static class ResetAction {
        public static final int RESET_PREFERENCES = 0;
        public static final int RESET_INSTANCES = 1;
        public static final int RESET_FORMS = 2;
        public static final int RESET_LAYERS = 3;
        public static final int RESET_CACHE = 4;
        public static final int RESET_OSM_DROID = 5;
    }
}
