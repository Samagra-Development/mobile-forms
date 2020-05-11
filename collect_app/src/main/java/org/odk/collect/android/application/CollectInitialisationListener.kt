package org.odk.collect.android.application

interface CollectInitialisationListener {

    fun onFailedToStart(message: String)
    fun onSuccess()

}
