package com.mcivicm.reactive.operator;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * 创建操作符
 */

public class CreatingOperators extends BaseOperators {

    @Test
    public void create() throws Exception {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                e.onNext(new Object());
                e.onNext(new Object());
                e.onComplete();
            }
        }).subscribe(new SimpleObserver());
    }

    @Test
    public void generate() throws Exception {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(@NonNull Emitter<Object> objectEmitter) throws Exception {
                objectEmitter.onNext(new Object());
                objectEmitter.onComplete();
            }
        }).subscribe(new SimpleObserver());


    }

    @Test
    public void unsafeCreate() throws Exception {
        Observable.unsafeCreate(new ObservableSource<Object>() {
            @Override
            public void subscribe(@NonNull Observer<? super Object> observer) {
                observer.onNext(new Object());
                observer.onComplete();
            }
        }).subscribe(new SimpleObserver());
    }

    @Test
    public void defer() throws Exception {
        Observable.defer(new Callable<ObservableSource<String>>() {

            @Override
            public ObservableSource<String> call() throws Exception {
                return new ObservableSource<String>() {
                    @Override
                    public void subscribe(@NonNull Observer<? super String> observer) {
                        observer.onNext("one");
                        observer.onNext("two");
                        observer.onNext("three");
                        observer.onComplete();
                    }
                };
            }
        }).subscribe(new SimpleObserver());

    }

    @Test
    public void empty() throws Exception {
        //这里的泛型只能是Object
        Observable.empty().subscribe(new SimpleObserver());
    }

    @Test
    public void never() throws Exception {
        Observable.never().subscribe(new SimpleObserver());
    }

    @Test
    public void error() throws Exception {
        Observable.error(new Throwable("throwable")).subscribe(new SimpleObserver());
    }

    @Test
    public void from() throws Exception {
        println("~fromArray");
        Observable.fromArray(new Object[]{new Object(), new Object()})
                .subscribe(new SimpleObserver());
        println("~fromIterable");
        Observable.fromIterable(Arrays.asList(new Object(), new Object()))
                .subscribe(new SimpleObserver());
        println("~fromCallable");
        Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return new Object();
            }
        }).subscribe(new SimpleObserver());
        println("~fromFuture");
        Observable.fromFuture(new FutureTask<Object>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return new Object();
            }
        }), 3, TimeUnit.SECONDS).subscribe(new SimpleObserver());
        println("~fromPublisher");
        Observable.fromPublisher(new Publisher<Object>() {
            @Override
            public void subscribe(Subscriber<? super Object> s) {
                s.onNext(new Object());
            }
        }).subscribe(new SimpleObserver());
    }

    @Test
    public void interval() throws Exception {
        Observable.interval(1, TimeUnit.SECONDS)
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void intervalRange() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void just() throws Exception {
        Observable.just(new Object()).subscribe(new SimpleObserver());
    }

    @Test
    public void range() throws Exception {
        Observable.range(0, 5).subscribe(new SimpleObserver());
    }

    @Test
    public void repeat() throws Exception {
        //repeat不是一个静态方法
        Observable.just(new Object()).repeat(10).subscribe(new SimpleObserver());
    }

    @Test
    public void repeatUtil() throws Exception {
        Calendar endTime = new GregorianCalendar(Locale.CHINA);
        endTime.add(Calendar.SECOND, 10);
        Observable.just(new Object()).repeatUntil(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() throws Exception {
                if (new GregorianCalendar(Locale.CHINA).before(endTime)) {
                    return false;
                } else {
                    return true;
                }
            }
        }).subscribe(new SimpleObserver());
    }

    @Test
    public void repeatWhen() throws Exception {
        Observable.just(new Object()).repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                //暂时想不出应用场景
                return null;
            }
        }).subscribe(new SimpleObserver());
    }

    @Test
    public void timer() throws Exception {
        Observable.timer(10, TimeUnit.SECONDS).blockingSubscribe(new PrintObserver());
    }
}
