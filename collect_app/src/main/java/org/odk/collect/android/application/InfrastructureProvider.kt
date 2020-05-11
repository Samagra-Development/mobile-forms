package org.odk.collect.android.application

import android.app.Application
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker


/**
 * Created by Umang Bhola on 11/5/20.
 * Samagra- Transforming Governance
 */
object InfrastructureProvider {


    lateinit var application: Application
    lateinit var applicationContext: Application

    private var tracker: Tracker? = null





    @Synchronized
    fun getDefaultTracker(): Tracker {
        if (tracker == null) {
            val analytics = GoogleAnalytics.getInstance(applicationContext)
            tracker = analytics.newTracker("203998633")
        }
        return tracker!!
    }

}
