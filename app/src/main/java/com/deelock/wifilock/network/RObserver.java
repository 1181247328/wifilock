package com.deelock.wifilock.network;

import android.content.Context;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by forgive for on 2018\3\23 0023.
 */

public class RObserver<T> implements Observer<T> {

    private ObserverListener listener;
    private Context context;

    public RObserver(Context context, ObserverListener listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        if (listener != null){
            listener.onNext(t);
        }
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
