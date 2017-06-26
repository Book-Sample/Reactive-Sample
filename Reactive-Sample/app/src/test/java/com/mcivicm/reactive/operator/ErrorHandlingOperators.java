package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * 错误处理操操作符
 */

public class ErrorHandlingOperators extends BaseOperators {
    /***************The [catch] part********************/
    @Test
    public void onErrorResumeNext() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        println("origin:" + aLong);
                        if (aLong == 5L) {
                            throw new Error("some error");
                        } else {
                            return aLong;
                        }
                    }
                })
                //捕捉Throwable, Exception, Error实例，出错后接入新的Observable，覆盖原有的Observable
                .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends Long>>() {
                    @Override
                    public ObservableSource<? extends Long> apply(@NonNull Throwable throwable) throws Exception {
                        //just fine, keep going
                        println("simulate error occurs");
                        return Observable.intervalRange(-10, 10, 0, 1, TimeUnit.SECONDS)
                                //just for printing
                                .map(new Function<Long, Long>() {
                                    @Override
                                    public Long apply(@NonNull Long aLong) throws Exception {
                                        println("new:" + aLong);
                                        return aLong;
                                    }
                                });
                    }
                })
                .subscribe(new PrintObserver() {
                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
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
    public void onExceptionResumeNext() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        if (aLong == 5) {
                            throw new Exception("simulate exception");
                        }
                        return aLong;
                    }
                })
                //不处理Throwable和Error实例,源Observable会继续进行
                .onExceptionResumeNext(Observable.just(Long.MAX_VALUE))
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
                    }
                });
        latch.await();

    }

    @Test
    public void onErrorReturn() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        if (aLong == 5) {
                            throw new Error("some error");
                        }
                        return aLong;
                    }
                })
                //出现错误时，发送一个数据项而不是执行onError方法
                .onErrorReturn(new Function<Throwable, Long>() {
                    @Override
                    public Long apply(@NonNull Throwable throwable) throws Exception {
                        //有则改之
                        println(throwable.getMessage());
                        return Long.MAX_VALUE;
                    }
                })
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void onErrorReturnItem() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        if (aLong == 5) {
                            throw new Error("some error");
                        }
                        return aLong;
                    }
                })
                //onErrorReturn的缺省形式
                .onErrorReturnItem(Long.MAX_VALUE)
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
                    }
                });
        latch.await();
    }

    /**************************the [retry] part******************************/
    @Test
    public void retry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(@NonNull Long aLong) throws Exception {
                        return new Random().nextInt(10);
                    }
                })
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        println("the random num: " + integer);
                        if (integer > 1) {
                            return integer > 1;
                        } else {
                            throw new Exception("只要大于1的数");
                        }
                    }
                })
                .retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(@NonNull Throwable throwable) throws Exception {
                        println(throwable.getMessage() + ", " + "重新请求数据。。。。。。");
                        return true;//just for printing
                    }
                })
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void retryUtil() throws Exception {
        //retry的变体
    }

    @Test
    public void retryWhen() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        println("doOnSubscribe");
                    }
                })
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(@NonNull Long aLong) throws Exception {
                        return new Random().nextInt(10);
                    }
                })
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        if (integer < 1) {
                            throw new Exception("小于1");
                        } else if (integer > 8) {
                            throw new Exception("大于8");
                        } else {
                            return true;
                        }
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {
                        //根据错误类型来决定再次请求的时间
                        return throwableObservable.map(new Function<Throwable, Long>() {
                            @Override
                            public Long apply(@NonNull Throwable throwable) throws Exception {
                                println(throwable.getMessage() + ", 等待再次请求。。。");
                                switch (throwable.getMessage()) {
                                    case "小于1":
                                        return 3L;//exception1错误延迟3s再次请求
                                    case "大于8":
                                        return 6L;//exception2错误延迟6s再次请求
                                    default:
                                        return 10L;//其他错误延迟10s再次请求
                                }
                            }
                        }).flatMap(new Function<Long, ObservableSource<Long>>() {
                            @Override
                            public ObservableSource<Long> apply(@NonNull Long aLong) throws Exception {
                                return Observable.timer(aLong, TimeUnit.SECONDS);
                            }
                        });
                    }
                })
                .subscribe(new PrintObserver() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        latch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        latch.countDown();
                    }
                });
        latch.await();
    }
}
