package com.mcivicm.reactive.operator;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 公共部分
 */

public abstract class BaseOperators {

    void println(String s) {
        System.out.println(s);
    }

    class SimpleObserver implements Observer<Object> {

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            println("onSubscribe");
        }

        @Override
        public void onNext(@NonNull Object o) {
            println("onNext");
        }

        @Override
        public void onError(@NonNull Throwable e) {
            println("onError");
        }

        @Override
        public void onComplete() {
            println("onComplete");
        }
    }
}
