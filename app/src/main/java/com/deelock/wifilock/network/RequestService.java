package com.deelock.wifilock.network;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public interface RequestService {

    @FormUrlEncoded
    @POST()
    Call<BaseResponse> commonRequest(
            @Url String url,
            @Header("appVersion") String version,
            @Header("sign") String sign,
            @Header("token") String token,
            @Field("check") int check,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("http://47.96.158.139:8080/file_server/deelock/image_del.json")
    Call<BaseResponse> deleteImage(
            @Field("token") String token,
            @Field("uid") String uid,
            @Field("timestamp") long timestamp,
            @Field("imageUrl") String imageUrl
    );

    @FormUrlEncoded
    @POST()
    Observable<OResponse> oRequest(
            @Url String url,
            @Header("sign") String sign,
            @Header("token") String token,
            @Field("check") int check,
            @Field("content") String content
    );
}
