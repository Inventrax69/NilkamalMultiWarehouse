package com.inventrax.athome_multiwh.application;

import android.content.Context;
import android.support.v4.app.FragmentActivity;



public class AbstractApplication {

    public static Context CONTEXT;
    public static FragmentActivity FRAGMENT_ACTIVITY;

    public static Context  get(){
       return CONTEXT;
    }

    public static FragmentActivity getFragmentActivity(){
        return  FRAGMENT_ACTIVITY;
    }


}
