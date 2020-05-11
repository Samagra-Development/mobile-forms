package org.odk.collect.android.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobManagerCreateException
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import net.danlew.android.joda.JodaTimeAndroid
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.android.dao.FormsDao
import org.odk.collect.android.external.ExternalDataManager
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent
import org.odk.collect.android.jobs.CollectJobCreator
import org.odk.collect.android.logic.FormController
import org.odk.collect.android.logic.PropertyManager
import org.odk.collect.android.preferences.*
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver
import org.odk.collect.android.tasks.sms.SmsSender
import org.odk.collect.android.tasks.sms.SmsSentBroadcastReceiver
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.LocaleHelper
import org.odk.collect.android.utilities.NotificationUtils
import org.odk.collect.android.utilities.PRNGFixes
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*


/**
 * Created by Umang Bhola on 11/5/20.
 * Samagra- Transforming Governance
 */
object CollectInitialiser {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var applicationComponent: AppDependencyComponent? = null

    var defaultSysLanguage: String? = null

    // Storage paths
    private var externalDataManager: ExternalDataManager? = null

    val ODK_ROOT = Environment.getExternalStorageDirectory()
            .toString() + File.separator + "odk"

    // Storage paths
    val FORMS_PATH = ODK_ROOT + File.separator + "forms"
    val INSTANCES_PATH = ODK_ROOT + File.separator + "instances"
    val CACHE_PATH = ODK_ROOT + File.separator + ".cache"
    val METADATA_PATH = ODK_ROOT + File.separator + "metadata"
    val TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg"
    val TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg"
    const val DEFAULT_FONTSIZE = "21"
    const val DEFAULT_FONTSIZE_INT = 21
    val OFFLINE_LAYERS = ODK_ROOT + File.separator + "layers"
    val SETTINGS = ODK_ROOT + File.separator + "settings"

    // Storage paths
    const val CLICK_DEBOUNCE_MS = 1000

    private var lastClickTime: Long = 0

    // Storage paths
    private var formController: FormController? = null
    private var lastClickName: String? = null

    fun init(
            application: Application,
            applicationContext: Context,
            collectInitalisationListener: CollectInitialisationListener? = null
    ) {

        when (applicationContext) {
            !is Application -> {
                Timber.e("Context isn't instance of Application")
                collectInitalisationListener?.onFailedToStart("Context isn't instance of Application")
            }
            else -> {
                InfrastructureProvider.applicationContext = applicationContext
                InfrastructureProvider.application = application
                firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
                installTls12(applicationContext)
                setupDagger(application)
                NotificationUtils.createNotificationChannel(application)
                applicationContext.registerReceiver(SmsSentBroadcastReceiver(), IntentFilter(SmsSender.SMS_SEND_ACTION))
                applicationContext.registerReceiver(SmsNotificationReceiver(), IntentFilter(SmsNotificationReceiver.SMS_NOTIFICATION_ACTION))
                try {
                    JobManager
                            .create(applicationContext)
                            .addJobCreator(CollectJobCreator())
                } catch (e: JobManagerCreateException) {
                    Timber.e(e)
                }
                reloadSharedPreferences()
                PRNGFixes.apply()
                AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
                JodaTimeAndroid.init(applicationContext)
                defaultSysLanguage = Locale.getDefault().language
                LocaleHelper().updateLocale(applicationContext)
                FormMetadataMigrator.migrate(PreferenceManager.getDefaultSharedPreferences(applicationContext))
                AutoSendPreferenceMigrator.migrate()
                initProperties()
                Timber.plant(DebugTree())
                setupLeakCanary(application, applicationContext)
            }
        }
    }

    fun getQuestionFontsize(): Int {
        return DEFAULT_FONTSIZE_INT
//        GeneralSharedPreferences.getInstance()[GeneralKeys.KEY_FONT_SIZE].toString().toInt()
    }

    private fun setupLeakCanary(application: Application, applicationContext: Context): RefWatcher? {
        return if (LeakCanary.isInAnalyzerProcess(applicationContext)) {
            RefWatcher.DISABLED
        } else LeakCanary.install(application)
    }

    fun initProperties() {
        val mgr = PropertyManager(InfrastructureProvider.applicationContext)

        // Use the server username by default if the metadata username is not defined
        if (mgr.getSingularProperty(PropertyManager.PROPMGR_USERNAME) == null || mgr.getSingularProperty(PropertyManager.PROPMGR_USERNAME).isEmpty()) {
            mgr.putProperty(PropertyManager.PROPMGR_USERNAME, PropertyManager.SCHEME_USERNAME, GeneralSharedPreferences.getInstance()[GeneralKeys.KEY_USERNAME] as String)
        }
        FormController.initializeJavaRosa(mgr)
    }

    fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled)
    }

    fun logNullFormControllerEvent(action: String?) {
        logRemoteAnalytics("NullFormControllerEvent", action, null)
    }

    private fun installTls12(applicationContext: Application) {
        if (Build.VERSION.SDK_INT <= 20) {
            ProviderInstaller.installIfNeededAsync(applicationContext, object : ProviderInstallListener {
                override fun onProviderInstalled() {}
                override fun onProviderInstallFailed(i: Int, intent: Intent) {
                    GoogleApiAvailability
                            .getInstance()
                            .showErrorNotification(applicationContext, i)
                }
            })
        }
    }


    private fun setupDagger(application: Application) {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(application)
                .build()
    }

    fun getComponent(): AppDependencyComponent {
        return applicationComponent!!
    }

    fun setComponent(applicationComponent: AppDependencyComponent) {
        this.applicationComponent = applicationComponent
//        applicationComponent.inject(this)
    }


    fun logRemoteAnalytics(event: String?, action: String?, label: String?) {
        // Google Analytics (
        InfrastructureProvider.getDefaultTracker()
                .send(HitBuilders.EventBuilder()
                        .setCategory(event)
                        .setAction(action)
                        .setLabel(label)
                        .build())

        // Firebase Analytics
        val bundle = Bundle()
        bundle.putString("action", action)
        bundle.putString("label", label)
        firebaseAnalytics.logEvent(event!!, bundle)
    }

    private fun reloadSharedPreferences() {
        GeneralSharedPreferences.getInstance().reloadPreferences()
        AdminSharedPreferences.getInstance().reloadPreferences()
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     *
     * @param formId      id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    fun getFormIdentifierHash(formId: String, formVersion: String?): String? {
        val formIdentifier = FormsDao().getFormTitleForFormIdAndFormVersion(formId, formVersion) + " " + formId
        return FileUtils.getMd5Hash(ByteArrayInputStream(formIdentifier.toByteArray()))
    }


    /**
     * Gets a unique, privacy-preserving identifier for the current form.
     *
     * @return md5 hash of the form title, a space, the form ID
     */
    fun getCurrentFormIdentifierHash(): String? {
        var formIdentifier = ""
        val formController: FormController? = getFormController()
        if (formController != null) {
            if (formController.formDef != null) {
                val formID = formController.formDef.mainInstance
                        .root.getAttributeValue("", "id")
                formIdentifier = formController.formTitle + " " + formID
            }
        }
        return FileUtils.getMd5Hash(ByteArrayInputStream(formIdentifier.toByteArray()))
    }

    fun getFormController(): FormController? {
        return formController
    }

    fun setFormController(controller: FormController?) {
        formController = controller
    }

    fun getExternalDataManager(): ExternalDataManager? {
        return externalDataManager
    }

    fun setExternalDataManager(dataManager: ExternalDataManager) {
        externalDataManager = dataManager
    }

    fun getVersionedAppName(): String? {
        var versionName = BuildConfig.VERSION_NAME
        versionName = " " + versionName.replaceFirst("-".toRegex(), "\n")
        return InfrastructureProvider.applicationContext.getString(R.string.app_name).toString() + versionName
    }


    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    @Throws(RuntimeException::class)
    fun createODKDirs() {
        val cardstatus = Environment.getExternalStorageState()
        if (cardstatus != Environment.MEDIA_MOUNTED) {
            throw RuntimeException(
                    InfrastructureProvider.applicationContext.getString(R.string.sdcard_unmounted, cardstatus))
        }
        val dirs = arrayOf(
                ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH, OFFLINE_LAYERS
        )
        for (dirName in dirs) {
            val dir = File(dirName)
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    val message: String =  InfrastructureProvider.applicationContext.getString(R.string.cannot_create_directory, dirName)
                    Timber.w(message)
                    throw RuntimeException(message)
                }
            } else {
                if (!dir.isDirectory) {
                    val message: String =  InfrastructureProvider.applicationContext.getString(R.string.not_a_directory, dirName)
                    Timber.w(message)
                    throw RuntimeException(message)
                }
            }
        }
    }
    /**
     * Predicate that tests whether a directory path might refer to an
     * ODK Tables instance data directory (e.g., for media attachments).
     */
    fun isODKTablesInstanceDataDirectory(directory: File): Boolean {
        /*
         * Special check to prevent deletion of files that
         * could be in use by ODK Tables.
         */
        var dirPath = directory.absolutePath
        if (dirPath.startsWith(ODK_ROOT)) {
            dirPath = dirPath.substring(ODK_ROOT.length)
            val parts = dirPath.split(if (File.separatorChar == '\\') "\\\\" else File.separator).toTypedArray()
            // [appName, instances, tableId, instanceId ]
            if (parts.size == 4 && parts[1] == "instances") {
                return true
            }
        }
        return false
    }


    // Debounce multiple clicks within the same screen
    fun allowClick(className: String): Boolean {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isSameClass = className == lastClickName
        val isBeyondThreshold = elapsedRealtime - lastClickTime > CLICK_DEBOUNCE_MS
        val isBeyondTestThreshold = lastClickTime.toInt() == 0 || lastClickTime === elapsedRealtime // just for tests
        val allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold
        if (allowClick) {
            lastClickTime = elapsedRealtime
            lastClickName = className
        }
        return allowClick
    }

    fun isNetworkAvailable(): Boolean {
        val manager = InfrastructureProvider.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetworkInfo = manager.activeNetworkInfo
        return currentNetworkInfo != null && currentNetworkInfo.isConnected
    }

}