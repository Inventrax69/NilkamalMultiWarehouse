package com.inventrax.athome_multiwh.services;


import android.util.Log;

import com.inventrax.athome_multiwh.common.constants.ServiceURL;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Prasanna ch on 7/2/2015.
 */
public class RestService {


    //public static final String BASE_URL = "http://192.168.1.15/FalconWMSCore_Endpoint/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        try {
            retrofit = null;
            if (retrofit == null) {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(100,TimeUnit.SECONDS).build();

                         retrofit = new Retrofit.Builder()
                        .baseUrl(ServiceURL.getServiceUrl()).client(client)

                        // .baseUrl("http://192.168.59.7/API/").client(client)
                       //  .baseUrl("http://192.168.1.72/VIP_API/").client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        } catch (Exception ex) {
            Log.d("Exceptionerror", ex.toString());
        }
        return retrofit;
    }
}