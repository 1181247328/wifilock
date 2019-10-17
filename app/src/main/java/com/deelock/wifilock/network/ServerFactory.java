package com.deelock.wifilock.network;


import com.deelock.wifilock.utils.Logger;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Administrator on 2017\9\11 0011.
 */

public class ServerFactory {
    private static final Logger logger = Logger.getLogger(ServerFactory.class);

    private ServerFactory() {
        throw new RuntimeException("Create instance is not allowed!");
    }

    public static String BASE_URL = ServerHost.INTERNAL_SERVER_HOST;

    // Response mapper for Basic types (String, i nt, short etc.)
    private static final ScalarsConverterFactory SCALARS = ScalarsConverterFactory.create();

    // Global shared retrofit instance
    public static volatile Retrofit sRetrofit = createRetrofit(BASE_URL);

    private static Retrofit createRetrofit(String baseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .addConverterFactory(SCALARS)
                .addConverterFactory(DecodeConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(16, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        String time = String.valueOf(TimeUtil.getTime());
                        Request request = original.newBuilder()
                                .removeHeader("User-Agent")
                                .header("type", "10000000")
                                .header("source", "10000000")
                                .header("User-Agent", "Android")
                                .header("timestamp", time)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                }).build();
        return builder.baseUrl(baseUrl).client(client).build();
    }

    public static <Service> Service create(Class<Service> cls) {
        if (sRetrofit == null){
            synchronized (ServerFactory.class){
                if (sRetrofit == null){
                    sRetrofit = createRetrofit(BASE_URL);
                }
            }
        }
        return sRetrofit.create(cls);
    }

    @Deprecated
    public static <Service> Service create(String baseUrl, Class<Service> cls) {
        return createRetrofit(baseUrl).create(cls);
    }
}
