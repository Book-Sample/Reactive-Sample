package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

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
        println("firstOrError:");
        Observable.just(5, 4, 3, 1).firstOrError().subscribe(new PrintSingle());
        println("firstElement:");
        Observable.just(5, 4, 3, 1).firstElement().subscribe(new PrintMaybe());
        println("blockingFirst:");
        int first = Observable.just(5, 4, 3, 1).blockingFirst();
        println(String.valueOf(first));
        println("blockingFirstWithArgument:");
        int firstA = Observable.just(5, 4, 3, 1).blockingFirst(-1);
        println(String.valueOf(firstA));
    }

    @Test
    public void ignoreElements() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .ignoreElements()
                .subscribe(new PrintCompletable() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void last() throws Exception {
        println("last:");
        Observable.just(5, 4, 3, 1)
                .last(-1)
                .subscribe(new PrintSingle());
        println("lastOrError:");
        Observable.just(5, 4, 3, 1)
                .lastOrError()
                .subscribe(new PrintSingle());
        println("lastElement:");
        Observable.just(5, 4, 3, 1)
                .lastElement()
                .subscribe(new PrintMaybe());
        println("blockingLast:");
        Integer integer = Observable.just(5, 4, 3, 1)
                .blockingLast();
        println(String.valueOf(integer));
        println("blockingLastWithArgument:");
        Integer integerA = Observable.just(5, 4, 3, 1)
                .blockingLast(-1);
        println(String.valueOf(integerA));
    }

    @Test
    public void sample() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.range(0, 10)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer) throws Exception {
                        Thread.sleep(new Random().nextInt(5) * 1000);
                        println("map:" + integer);
                        return integer;
                    }
                })//只为睡眠打印
                .sample(5, TimeUnit.SECONDS)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void skip() throws Exception {
        println("skip count:");
        Observable.range(0, 10)
                .skip(5)
                .subscribe(new PrintObserver());
        println("skip time:");
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .skip(5, TimeUnit.SECONDS)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void skipLast() throws Exception {
        println("skipLast count:");
        Observable.range(0, 10)
                .skipLast(5)
                .subscribe(new PrintObserver());
        println("skipLast time:");
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 1, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        println("map:" + aLong);
                        return aLong;
                    }
                })
                .skipLast(5, TimeUnit.SECONDS)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void take() throws Exception {
        println("take count:");
        Observable.range(0, 10)
                .take(5)
                .subscribe(new PrintObserver());
        println("take time:");
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .take(5, TimeUnit.SECONDS)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void takeLast() throws Exception {
        println("skipLast count:");
        Observable.range(0, 10)
                .takeLast(5)
                .subscribe(new PrintObserver());
        println("skipLast time:");
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 1, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        println("map:" + aLong);
                        return aLong;
                    }
                })
                .takeLast(5, TimeUnit.SECONDS)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }
                });
        latch.await();
    }
}
