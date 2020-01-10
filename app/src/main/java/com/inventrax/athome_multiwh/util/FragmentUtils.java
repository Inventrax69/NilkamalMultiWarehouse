package com.inventrax.athome_multiwh.util;

/**
 * Created by padmaja on 05/01/2018.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentUtils {

    private static FragmentManager fragmentManager;
    private static FragmentTransaction fragmentTransaction;

    /**
     * To add fragment to the container
     *
     * @param activity          current FragmentActivity reference
     * @param fragmentContainer fragment container which holds the specified fragment
     * @param fragment          fragment added to the specified container
     */
    public static void addFragment(FragmentActivity activity, int fragmentContainer, Fragment fragment) {

        try {
            fragmentManager = activity.getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(fragmentContainer, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {

        }
    }

    /**
     * To add fragment to the container with back stack option
     *
     * @param activity          current FragmentActivity reference
     * @param fragmentContainer fragment container which holds the specified fragment
     * @param fragment          fragment added to the specified container
     */
    public static void addFragmentWithBackStack(FragmentActivity activity, int fragmentContainer, Fragment fragment) {

        try {
            fragmentManager = activity.getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(fragmentContainer, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception ex) {

        }
    }

    /**
     * To replace fragment for the specified container
     *
     * @param activity          current FragmentActivity reference
     * @param fragmentContainer fragment container which holds the specified fragment
     * @param fragment          fragment added to the specified container
     */
    public static void replaceFragment(FragmentActivity activity, int fragmentContainer, Fragment fragment) {

        try {
            fragmentManager = activity.getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(fragmentContainer, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {

        }
    }

    /**
     * To replace fragment for the specified container with back stack option
     *
     * @param activity          current FragmentActivity reference
     * @param fragmentContainer fragment container which holds the specified fragment
     * @param fragment          fragment added to the specified container
     */
    public static void replaceFragmentWithBackStack(FragmentActivity activity, int fragmentContainer, Fragment fragment) {

        try {
            fragmentManager = activity.getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(fragmentContainer, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception ex) {

        }
    }

    /**
     * To remove fragment from the container
     *
     * @param activity current FragmentActivity reference
     * @param fragment fragment added to the specified container
     */
    public static void removeFragment(FragmentActivity activity, Fragment fragment) {

        try {
            fragmentManager = activity.getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {

        }
    }


}