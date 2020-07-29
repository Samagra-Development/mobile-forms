package org.odk.collect.android.contracts;


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