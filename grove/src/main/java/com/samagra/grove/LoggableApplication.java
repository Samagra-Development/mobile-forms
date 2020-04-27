package com.samagra.grove;

import android.app.Activity;
import android.app.Application;

public interface LoggableApplication {
    Application getLoggableApplication();
    Activity getCurrentActivity();
}
