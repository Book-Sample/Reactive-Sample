package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;

/**
 * 可连接数据源的操作符
 */

public class ConnectableObservableOperators extends BaseOperators {

    @Test
    public void publish() throws Exception {
        //将冷源变成热源，将一调用subscribe就发送变成调用connect才发送
        ConnectableObservable<Long> connectableObservable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).publish();
    }

    @Test
    public void connect() throws Exception {
        //只有调用了connect()数据源才发送数据项给订阅者
        ConnectableObservable<Long> connectable = Observable.intervalRange(0, 50, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("publish: " + aLong);
                    }
                })
                .publish();
        println("start connect:");
        Disposable disposable = connectable.connect();//通知ConnectableObservable开始从上游发送数据项
        println("connected.");
        println("sleep to wait subscribe...");
        Thread.sleep(10000);
        println("subscribe observer A:");
        connectable.subscribe(new PrintObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer A");
            }
        });
        Thread.sleep(10000);
        println("subscribe observer B:");
        connectable.subscribe(new PrintObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer B");
            }
        });
        Thread.sleep(10000);
        println("start dispose:");
        disposable.dispose();
        println("disposed.");
        connectable.subscribe(new PrintObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer C");
            }
        });
        println("sleep to reconnect...");
        Thread.sleep(10000);
        println("restart connect:");
        Disposable disposable1 = connectable.connect();//可以反复connect和dispose。重连后数据从头开始发送数据项
        println("reconnected.");
        connectable.blockingSubscribe();
    }

    @Test
    public void disposeObserver() throws Exception {
        ConnectableObservable<Long> connectable = Observable.intervalRange(0, 50, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("publish: " + aLong);
                    }
                })
                .publish();
        println("start connect:");
        connectable.connect();//通知ConnectableObservable开始从上游发送数据项
        println("connected.");
        println("sleep to wait subscribe...");
        Thread.sleep(10000);
        println("subscribe observer A:");
        DisposableObserver A = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer A");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer A");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer A");
            }
        };
        connectable.subscribe(A);
        Thread.sleep(10000);
        println("subscribe observer B:");
        DisposableObserver B = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer B");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer B");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer B");
            }
        };
        connectable.subscribe(B);
        Thread.sleep(10000);
        println("dispose A:");
        A.dispose();//取消observer的订阅不会影响源数据源的发送
        println("A disposed.");
        Thread.sleep(5000);
        println("dispose B:");
        B.dispose();//取消observer的订阅不会影响源数据源的发送
        println("B disposed.");
        Thread.sleep(5000);
        connectable.subscribe(new PrintObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer C");
            }
        });
        connectable.blockingSubscribe();
    }

    @Test
    public void share() throws Exception {
        //返回一个广发源Observable的数据源，只要存在一个Observer，该数据源就会被订阅并且发送数据项。当所有的订阅者取消后，就会销毁数据源。(ps:自动发送和停止机制)
        Observable<Long> observable = Observable.intervalRange(0, 30, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("publish: " + aLong);
                    }
                })
                .share();
        println("wait to subscribe...");
        Thread.sleep(5000);
        println("subscribe observer A");
        DisposableObserver A = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer A");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer A");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer A");
            }
        };
        observable.subscribe(A);
        Thread.sleep(5000);
        println("subscribe observer B: ");
        DisposableObserver B = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer B");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer B");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer B");
            }
        };
        observable.subscribe(B);//不会发送[已经发出的数据项]给Observer B
        Thread.sleep(5000);
        println("dispose A:");
        A.dispose();
        println("A disposed.");
        Thread.sleep(5000);
        println("dispose B");
        B.dispose();
        println("B disposed.");
        Thread.sleep(1000);
        println("there is no more publish.");
        Thread.sleep(10000);//wait to finish emitting completely.
    }

    @Test
    public void withoutShare() throws Exception {
        Observable<Long> observable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS);
        Thread.sleep(3000);
        println("subscribe observer A");
        observable.subscribe(new PrintObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer A");
            }
        });
        Thread.sleep(6000);
        println("subscribe observer B");
        observable.subscribe(new PrintObserver() {//会完整地把所有数据项都发给Observer B
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer B");
            }
        });
        observable.blockingSubscribe();

    }

    @Test
    public void replay() throws Exception {
        /**
         *返回一个和下游数据源共享一个subscription的可连接数据源,该可连接数据源会重新发送所有的数据项和通知给以后的Observer。
         * 一个Connectable数据源和普通数据源相似，不同的是，可连接数据源在订阅时不发送数据项，只当connect方法被调用之后才发送。
         */
        ConnectableObservable<Long> observable = Observable.intervalRange(0, 30, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("publish: " + aLong);
                    }
                })
                .replay();
        println("start connect: ");
        observable.connect();
        println("connected.");
        println("wait to subscribe...");
        Thread.sleep(5000);
        println("subscribe observer A");
        DisposableObserver A = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer A");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer A");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer A");
            }
        };
        observable.subscribe(A);
        Thread.sleep(5000);
        println("subscribe observer B: ");
        DisposableObserver B = new DisposableObserver() {
            @Override
            public void onNext(@NonNull Object o) {
                println(String.valueOf(o) + " from observer B");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                println("onError from observer B");
            }

            @Override
            public void onComplete() {
                println("onComplete form observer B");
            }
        };
        observable.subscribe(B);//重新发送所有数据项给Observer B，[包括]已经发送的数据项（一口气发完）
        Thread.sleep(5000);
        println("dispose A:");
        A.dispose();
        println("A disposed.");
        Thread.sleep(5000);
        println("dispose B");
        B.dispose();
        println("B disposed.");
        Thread.sleep(1000);
        println("there is no more publish.");
        Thread.sleep(10000);//wait to finish emitting completely.
    }
}
