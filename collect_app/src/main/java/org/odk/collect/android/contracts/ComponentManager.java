package org.odk.collect.android.contracts;

import com.samagra.commons.MainApplication;

public class ComponentManager {
    public static IFormManagementContract iFormManagementContract;

    /**
     *
     * @param formManagmentClassImpl
     */
    public static void registerFormManagementPackage(IFormManagementContract formManagmentClassImpl) {
        iFormManagementContract = formManagmentClassImpl;
    }

}