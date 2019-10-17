package com.deelock.wifilock.network;

import com.google.gson.TypeAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by forgive for on 2017\11\24 0024.
 */

public class DecodeResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;

    DecodeResponseBodyConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {

        byte[] bytes = value.bytes();
//        //先解密 在解压
//        try {
//            bytes = GzipUtil.unZip(new TripleDES(Contacts.BODY_ENCRYPTION_KEY).decryptionByteData(bytes));
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        LogUtils.d(new String(bytes,"UTF-8"));

        //解密字符串
        return bytes==null?null:adapter.fromJson(new String(bytes,"UTF-8"));
    }
}
