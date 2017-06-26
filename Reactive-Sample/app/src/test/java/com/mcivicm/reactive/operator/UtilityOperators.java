package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 工具操作符
 */

public class UtilityOperators extends BaseOperators {
    @Test
    public void delay() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.rangeLong(0, 10)
                .delay(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Long integer) throws Exception {
                        return Observable.timer(integer, TimeUnit.SECONDS);//timer just emit a complete() notification. (view notification as an item)
                    }
                })
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void delaySubscription() throws Exception {
        Observable.rangeLong(0,10)
                .delaySubscription()

    }
}
