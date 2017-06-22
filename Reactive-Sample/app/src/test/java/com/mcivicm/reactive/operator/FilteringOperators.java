package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 过滤操作符
 */

public class FilteringOperators extends BaseOperators {
    @Test
    public void debounce() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .debounce(500, TimeUnit.MILLISECONDS)//完全不懂
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        println(String.valueOf(o));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void throttleWithTimeOut() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)//看不懂
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        println(String.valueOf(o));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();

    }


    @Test
    public void distinct() throws Exception {
        Observable.just(1, 2, 3, 1, 2, 4, 3, 3, 6)
                .distinct()
                .subscribe(new PrintObserver());
    }

    @Test
    public void distinceUtilChanged() throws Exception {
        Observable.just(1, 2, 3, 1, 2, 4, 3, 3, 6)
                .distinctUntilChanged()
                .subscribe(new PrintObserver());
    }

    @Test
    public void elementAt() throws Exception {
        Observable.just(1, 2, 3, 4, 5)
                .elementAt(5)
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        println("onSubscribe");
                    }

                    @Override
                    public void onSuccess(@NonNull Integer integer) {
                        println("onNext:" + integer);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        println("onError");
                    }

                    @Override
                    public void onComplete() {
                        println("onComplete");
                    }
                });

    }

    @Test
    public void filter() throws Exception {
        Observable.range(0, 10)
                .filter(integer -> integer > 5)
                .subscribe(new PrintObserver());
    }

    @Test
    public void ofType() throws Exception {
        Observable.range(0, 10)
                .ofType(String.class)
                .subscribe(new PrintObserver());
    }

    @Test
    public void first() throws Exception {
        println("first:");
        Observable.just(5, 4, 3, 1).first(-1).subscribe(new PrintSingle());
        println("firstElement:");
        Observable.just(5, 4, 3, 1).firstElement().subscribe(new PrintMaybe());
        println("single:");
        Observable.just(5, 4, 3, 1).single(-1).subscribe(new PrintSingle());
        println("singleElement:");
        Observable.just(5, 4, 3, 1).singleElement().subscribe(new PrintMaybe());
        println("blockingFirst:");
        int first = Observable.just(5, 4, 3, 1).blockingFirst();
        println(String.valueOf(first));
        println("blockingSingle:");
        try {
            int single = Observable.just(5, 4, 3, 1).blockingSingle();
            println(String.valueOf(single));
        } catch (Exception e) {
            println(e.getMessage());
        }

    }
}
