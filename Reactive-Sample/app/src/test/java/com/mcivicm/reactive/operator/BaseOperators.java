package com.mcivicm.reactive.operator;

import io.reactivex.CompletableObserver;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
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

    class PrintObserver implements Observer<Object> {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            println("onSubscribe");
        }

        @Override
        public void onNext(@NonNull Object o) {
            println(String.valueOf(o));
        }

        @Override
        public void onError(@NonNull Throwable e) {
            println("onError:" + e.getMessage());
        }

        @Override
        public void onComplete() {
            println("onComplete");
        }
    }

    class PrintSingle implements SingleObserver<Object> {

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            println("onSubscribe");
        }

        @Override
        public void onSuccess(@NonNull Object o) {
            println(String.valueOf(o));
        }

        @Override
        public void onError(@NonNull Throwable e) {
            println("onError:" + e.getMessage());
        }
    }

    class PrintMaybe implements MaybeObserver<Object> {

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            println("onSubscribe");
        }

        @Override
        public void onSuccess(@NonNull Object o) {
            println(String.valueOf(o));
        }

        @Override
        public void onError(@NonNull Throwable e) {
            println("onError:" + e.getMessage());
        }

        @Override
        public void onComplete() {
            println("onComplete");
        }
    }

    class PrintCompletable implements CompletableObserver {

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            println("onSubscribe");
        }

        @Override
        public void onComplete() {
            println("onComplete");
        }

        @Override
        public void onError(@NonNull Throwable e) {
            println("onError:" + e.getMessage());
        }
    }
}
