package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * 条件和布尔操作符
 */

public class ConditionalBooleanOperators extends BaseOperators {
    @Test
    public void all() throws Exception {
        Observable.range(0, 10)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer) throws Exception {
                        return new Random().nextInt(10);//生成随机数
                    }
                })
                .all(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer < 9;//是否小于9
                    }
                })
                .subscribe(new PrintSingle());
    }

    @Test
    public void amb() throws Exception {
        //只发送第一个发送数据的Observable的所有数据，其他Observable的数据都丢弃
        Observable.amb(Arrays.asList(
                Observable.intervalRange(0, 10, 600, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (aLong + 65));
                    }
                }),
                Observable.intervalRange(0, 10, 300, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (aLong + 97));
                    }
                }),
                Observable.intervalRange(0, 10, 0, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (aLong + 48));
                    }
                })
        )).blockingSubscribe(new PrintObserver());
    }

    @Test
    public void ambArray() throws Exception {
        //amb()的变体
    }

    @Test
    public void ambWith() throws Exception {
        //amb()的实例方法
    }

    @Test
    public void contains() throws Exception {
        Observable.range(0, 10)
                .contains(10)
                .subscribe(new PrintSingle());
    }

    @Test
    public void any() throws Exception {
        boolean res = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .any(new Predicate<Long>() {
                    @Override
                    public boolean test(@NonNull Long integer) throws Exception {
                        return integer > 9;
                    }
                }).blockingGet();
        println("是否有大于9的数：" + res);
    }

    @Test
    public void isEmpty() throws Exception {
        boolean result = Observable.empty()
                .delay(5, TimeUnit.SECONDS)
                .isEmpty()
                .blockingGet();
        println("是否为空：" + result);
    }

    @Test
    public void defaultIfEmpty() throws Exception {
        //如果空则发送默认值
        Observable.just(0)
                .delay(5, TimeUnit.SECONDS)
                .defaultIfEmpty(-1)
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void switchIfEmpty() throws Exception {
        Observable.empty()
                .delay(5, TimeUnit.SECONDS)
                .switchIfEmpty(Observable.just(Integer.MAX_VALUE))//等价于defaultIfEmpty(Integer.MAX_VALUE);
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void sequenceEqual() throws Exception {
        //不同时间间隔地发送两组数据
        boolean equalQ = Observable.sequenceEqual(
                Observable.just(1)
                        .delay(5, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(2))
                        .delay(4, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(3))
                        .delay(3, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(4))
                        .delay(1, TimeUnit.SECONDS),
                Observable.just(1)
                        .delay(1, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(2))
                        .delay(3, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(3))
                        .delay(4, TimeUnit.SECONDS)
                        .mergeWith(Observable.just(4))
                        .delay(5, TimeUnit.SECONDS))
                .blockingGet();
        println("sequence equal? " + equalQ);
    }

    @Test
    public void skipUtil() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .skipUntil(Observable.timer(5, TimeUnit.SECONDS))
                .blockingSubscribe(new PrintObserver());
    }

    @Test
    public void skipWhile() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .skipWhile(new Predicate<Long>() {
                    @Override
                    public boolean test(@NonNull Long aLong) throws Exception {
                        return aLong < 5;
                    }
                }).blockingSubscribe(new PrintObserver());

    }

    @Test
    public void takeUtil() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
                .blockingSubscribe(new PrintObserver());

    }

    @Test
    public void takeWhile() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .takeWhile(new Predicate<Long>() {
                    @Override
                    public boolean test(@NonNull Long aLong) throws Exception {
                        return aLong < 5;
                    }
                })
                .blockingSubscribe(new PrintObserver());
    }
}
