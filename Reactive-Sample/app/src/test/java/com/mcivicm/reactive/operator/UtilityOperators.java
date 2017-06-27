package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;

/**
 * 工具操作符
 */

public class UtilityOperators extends BaseOperators {
    @Test
    public void delay() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.rangeLong(0, 10)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        println("doOnSubscribe");
                    }
                })
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
        CountDownLatch latch = new CountDownLatch(1);
        Observable.rangeLong(0, 10)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        println("doOnSubscribe");
                    }
                })
                .delaySubscription(5, TimeUnit.SECONDS)
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
    public void lifeCycle() throws Exception {
        Observable.just(0)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        println("doOnSubscribe");
                    }
                })
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        println("doOnNext:" + integer);
                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        println("doOnDispose");
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        println("doOnComplete");
                    }
                })
                .subscribe();

    }

    @Test
    public void materialize() throws Exception {
        Observable.range(0, 10)
                .materialize()
                .map(new Function<Notification<Integer>, Notification<Integer>>() {
                    @Override
                    public Notification<Integer> apply(@NonNull Notification<Integer> integerNotification) throws Exception {
                        println("materialize: " + integerNotification.toString());
                        return integerNotification;
                    }
                })
                .dematerialize()
                .map(new Function<Object, Object>() {
                    @Override
                    public Object apply(@NonNull Object o) throws Exception {
                        println("dematerialize: " + o.getClass().getName());
                        return o;
                    }
                })
                .subscribe(new PrintObserver());

    }

    @Test
    public void observeOn() throws Exception {
        //观察者线程切换
        CountDownLatch latch = new CountDownLatch(1);
        Observable.just(1)
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.newThread())
                .subscribe(new PrintObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        println("onSubscribe: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                        println("onComplete: " + Thread.currentThread().getName());
                    }
                });
        latch.await();
    }

    @Test
    public void subscribeOn() throws Exception {
        //同observeOn，数据源线程切换
    }

    /**
     * 迫使数据源发送的数据项和通知序列化，并用别的方式使数据源遵从通讯协议。
     * 数据源可能从不同的线程异步地调用观测者的方法。
     * 这将导致数据源的行为异常，比如数据源会在执行onNext之前执行onComplete或onError，或者从两个不同的线程并发地调用onNext方法。
     * 调用数据源的serialize()方法能使数据源规范化（遵守协议）和序列化。
     */
    @Test
    public void serialize() throws Exception {
        //暂想不到例子，详见上面翻译
    }

    @Test
    public void subscribe() throws Exception {
        Observable.just(0).subscribe(new PrintObserver());
    }

    /**
     * 订阅到当前Observable，并把给定的Observer（如果不是SafeObserver）包装成SafeObserver
     * 来处理行为失常（不遵循Reactive-Streams规范）的observer抛出的异常。
     */
    @Test
    public void safeSubscribe() throws Exception {
        Observable.range(0, 10)
                .safeSubscribe(new PrintObserver());
    }

    @Test
    public void blockingSubscribe() throws Exception {
        //blocking代表阻塞，阻塞当前线程直到订阅完成。（再也不用CountDownLatch了，现在才看到，有点晚了）
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void blockingForEach() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .blockingForEach(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long integer) throws Exception {
                        println("blockingSubscribe" + integer);
                    }
                });

    }

    @Test
    public void forEachWhile() throws Exception {
        PrintObserver observer = new PrintObserver();
        Observable.range(0, 10)
                .forEachWhile(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {//条件不满足即完成
                        observer.onNext(integer);
                        return integer < 5;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        observer.onError(throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        observer.onComplete();
                    }
                });
    }

    @Test
    public void timeInterval() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .timeInterval()//相对于上一个数据项的时间间隔
                .blockingForEach(new Consumer<Timed<Long>>() {
                    @Override
                    public void accept(@NonNull Timed<Long> longTimed) throws Exception {
                        println("Timed: " + longTimed.toString() + "--" + longTimed.value());
                    }
                });

    }

    @Test
    public void timeOut() throws Exception {
        Observable.timer(3, TimeUnit.SECONDS)
                //给定时间内没有收到数据，则会发送onError通知
                .timeout(2, TimeUnit.SECONDS, Observable.just(Long.MAX_VALUE))
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void timeStamp() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .timestamp()//绝对时间
                .blockingForEach(new Consumer<Timed<Long>>() {
                    @Override
                    public void accept(@NonNull Timed<Long> longTimed) throws Exception {
                        println("onNext: " + format.format(new Date(longTimed.time())) + "--" + longTimed.value());
                    }
                });
    }

    private class StringDisposable implements Disposable {

        private String value = null;

        public StringDisposable(String content) {
            value = content;
        }

        @Override
        public void dispose() {
            value = null;
        }

        @Override
        public boolean isDisposed() {
            return value == null;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "StringDisposable{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    @Test
    public void using() throws Exception {
        //创建一个和资源生命周期同步的数据源，即数据源的生命周期决定了资源的生命周期。
        //当观察着取消了订阅，或者数据源中断（正常或带错误地）时，using会调用第三个函数来处理创建的资源。
        Observable.using(new Callable<StringDisposable>() {//resourceSupplier：资源提供者。生成需要的资源。
            @Override
            public StringDisposable call() throws Exception {
                String s = String.valueOf((char) (65 + new Random().nextInt(10)));
                StringDisposable disposable = new StringDisposable(s);
                println("call resource: " + disposable.toString());
                return disposable;
            }
        }, new Function<StringDisposable, ObservableSource<String>>() {//sourceSupplier:数据源提供者。生成需要的数据源。
            @Override
            public ObservableSource<String> apply(@NonNull StringDisposable stringDisposable) throws Exception {
                println("create source from resource: " + stringDisposable.toString());
                return Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return stringDisposable.getValue();//10s重复发送10次
                    }
                });
            }
        }, new Consumer<StringDisposable>() {//disposer，资源销毁者
            @Override
            public void accept(@NonNull StringDisposable stringDisposable) throws Exception {
                println("dispose resource: " + stringDisposable);
                //here （the items from the Observable have been emitted） we can do work of disposing
                stringDisposable.dispose();
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                println("source doOnDispose");
            }
        }).blockingSubscribe(new PrintObserver());//用水
    }
}
