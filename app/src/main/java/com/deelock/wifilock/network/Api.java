package com.deelock.wifilock.network;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by forgive for on 2018\3\20 0020.
 */

public class Api {

    //public static String baseUrl = "http://192.168.0.70:9881";   //内网
//    public static String baseUrl = "http://120.79.37.67:7211/smarthome/";  //外网
    public static String baseUrl = "http://120.79.37.67:7211/smarthome/";  //外网
    public static RequestService service;

    private Api(){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(16, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .removeHeader("User-Agent")
                                .header("type", "10000000")
                                .header("source", "10000000")
                                .header("User-Agent", "Android")
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(RequestService.class);
    }

    public static RequestService getService(){
        if (service == null) {
            synchronized (Api.class) {
                if (service == null) {
                    new Api();
                }
            }
        }
        return service;
    }
}
