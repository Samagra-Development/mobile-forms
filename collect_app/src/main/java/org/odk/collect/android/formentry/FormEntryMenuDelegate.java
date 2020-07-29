package org.odk.collect.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.MenuDelegate;
import org.odk.collect.android.utilities.PlayServicesChecker;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate implements MenuDelegate, RequiresFormController {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;

    @Nullable
    private FormController formController;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider, FormIndexAnimationHandler formIndexAnimationHandler) {
        this.activity = activity;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;
    }

    @Override
    public void formLoaded(FormController formController) {
        this.formController = formController;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean useability;

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(getFormEntryViewModel().canAddRepeat());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_add_repeat) {
            getFormSaveViewModel().saveAnswersForScreen(answersProvider.getAnswers());
            getFormEntryViewModel().promptForNewRepeat();
            formIndexAnimationHandler.handle(getFormEntryViewModel().getCurrentIndex());
            return true;
        } else if (itemId == R.id.menu_preferences) {
            Intent pref = new Intent(activity, PreferencesActivity.class);
            activity.startActivity(pref);
            return true;
        } else if (itemId == R.id.track_location) {
            getBackgroundLocationViewModel().backgroundLocationPreferenceToggled();
            return true;
        }

        return false;
    }

    private FormEntryViewModel getFormEntryViewModel() {
        return ViewModelProviders.of(activity).get(FormEntryViewModel.class);
    }

    private FormSaveViewModel getFormSaveViewModel() {
        return ViewModelProviders.of(activity).get(FormSaveViewModel.class);
    }

    private BackgroundLocationViewModel getBackgroundLocationViewModel() {
        return ViewModelProviders.of(activity).get(BackgroundLocationViewModel.class);
    }
}