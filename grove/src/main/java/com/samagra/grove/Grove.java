package com.samagra.grove;

import android.app.Activity;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import timber.log.Timber;

import static android.util.Log.getStackTraceString;
import static android.util.Log.isLoggable;
import static timber.log.Timber.log;

public class Grove {

    private static LoggableApplication applicationInstance;

    static String appName = "N/A";

    public static void init(LoggableApplication applicationInstance) {
        Grove.applicationInstance = applicationInstance;
        Grove.appName = BuildConfig.FLAVOR.toUpperCase();
    }

    private static Class initClass() {
        String tag;
        if(applicationInstance == null) return null;
        Activity currentActivity = applicationInstance.getCurrentActivity();
        Class clazz = applicationInstance.getClass();

        if (currentActivity == null) tag = "";
        else {
            tag = currentActivity.getClass().getName();
            clazz = currentActivity.getClass();
        }

        return clazz;
    }

    public static void e(String message) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.e(message);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + message).build());
    }

    public static void e(Throwable t, String message, Object... args) {
        Timber.e(t, message, args);
    }

    public static void e(String message, Object... args) {
        Class clazz = initClass();
        Timber.e(message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
    }

    public static void e(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.e(e);
        Sentry.capture(e);
    }

    public static void e(String TAG, String message) {
        Class clazz = initClass();
        Timber.tag(TAG);
        Timber.e(message);
        Sentry.record(new BreadcrumbBuilder().setMessage(TAG + "::" + message).build());
    }

    public static void d(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.d(e);
        Sentry.capture(e);
    }

    public static void d(String message) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.d(message);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + message).build());
    }

    public static void i(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.i(e);
        Sentry.capture(e);
    }

    public static void d(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.d(t, message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
        Sentry.capture(t);
    }

    public static void d(String message, Object... args) {
        Class clazz = initClass();
        Timber.i(message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
    }

    public static void i(String message) {
        Class clazz = initClass();
        if(clazz != null) Timber.tag(clazz.getName());
        Timber.i(message);
    }

    public static void i(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.i(t, message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
        Sentry.capture(t);
    }

    public static void i(String message, Object... args) {
        Class clazz = initClass();
        Timber.i(message, args);
    }

    public static void v(String message) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.v(message);
    }

    public static void v(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.v(t, message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
        Sentry.capture(t);
    }

    public static void v(String message, Object... args) {
        Class clazz = initClass();
        Timber.v(message, args);
    }

    public static void w(String message) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.w(message);
    }

    public static void w(Throwable t, String message, Object... args) {
        Class clazz = initClass();
        Timber.w(t, message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
        Sentry.capture(t);
    }

    public static void w(String message, Object... args) {
        Class clazz = initClass();
        Timber.w(message, args);
        Sentry.record(new BreadcrumbBuilder().setMessage(clazz.getName() + "::" + formatMessage(message, args)).build());
    }

    public static void w(Throwable e) {
        Class clazz = initClass();
        Timber.tag(clazz.getName());
        Timber.w(e);
        Sentry.capture(e);
    }

    /**
     * Formats a log message with optional arguments.
     */
    public static String formatMessage(String message, Object[] args) {
        return String.format(message, args);
    }

}