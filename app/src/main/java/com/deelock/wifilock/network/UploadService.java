package com.deelock.wifilock.network;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

/**
 * Created by forgive for on 2018\3\26 0026.
 */

public interface UploadService {

    @FormUrlEncoded
    @POST()
    Call<BaseResponse> commonRequest(
            @Url String url,
            @Header("sign") String sign,
            @Header("token") String token,
            @Field("check") int check,
            @Field("content") String content
    );

    @Multipart
    @POST("/file_server/deelock/image_add.json")
    Call<BaseResponse> upload(
            @Part("uid") RequestBody uid,
            @Part("token") RequestBody token,
            @Part("timestamp") RequestBody timestamp,
            @Part("file") RequestBody files
    );
}
