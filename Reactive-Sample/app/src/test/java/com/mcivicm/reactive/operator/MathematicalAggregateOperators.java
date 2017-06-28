package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

/**
 * 数学和总计操作符
 */

public class MathematicalAggregateOperators extends BaseOperators {
    @Test
    public void concat() throws Exception {
        //一个Observable发送完成，再开始另一个，Obseravable之间不交错。
        Observable.concat(
                Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                        .map(new Function<Long, String>() {
                            @Override
                            public String apply(@NonNull Long aLong) throws Exception {
                                return String.valueOf((char) (aLong + 65));
                            }
                        }),
                Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS)//延迟500ms
                        .map(new Function<Long, String>() {
                            @Override
                            public String apply(@NonNull Long aLong) throws Exception {
                                return String.valueOf((char) (aLong + 97));
                            }
                        })
        ).blockingSubscribe(new PrintObserver());
    }

    @Test
    public void concatDelayError() throws Exception {
        //延迟发送错误直到所有的Observable都中断
        Observable.concatDelayError(
                Arrays.asList(
                        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                                .map(new Function<Long, String>() {
                                    @Override
                                    public String apply(@NonNull Long aLong) throws Exception {
                                        if (aLong == 5) {
                                            throw new Exception("some exception");
                                        } else {
                                            return String.valueOf((char) (aLong + 65));
                                        }
                                    }
                                }),
                        Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS)//延迟500ms
                                .map(new Function<Long, String>() {
                                    @Override
                                    public String apply(@NonNull Long aLong) throws Exception {
                                        return String.valueOf((char) (aLong + 97));
                                    }
                                }))
        ).blockingSubscribe(new PrintObserver());
    }

    @Test
    public void concatEager() throws Exception {
        //Eager意味着一旦订阅者订阅到一个数据源，该操作符会订阅所有的Observable。该操作符缓冲这些数据源发出的数据项，每当前一个Observable完成时，都按顺序排空这些数据项。
        Observable.concatEager(
                Arrays.asList(
                        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                                .map(new Function<Long, String>() {
                                    @Override
                                    public String apply(@NonNull Long aLong) throws Exception {
                                        return String.valueOf((char) (aLong + 65));
                                    }
                                }),
                        Observable.intervalRange(0, 10, 6, 1, TimeUnit.SECONDS)//延迟6s，上一个Observable结束时，已缓冲a,b,c,d，按顺序排空a,b,c,d
                                .map(new Function<Long, String>() {
                                    @Override
                                    public String apply(@NonNull Long aLong) throws Exception {
                                        return String.valueOf((char) (aLong + 97));
                                    }
                                }),
                        Observable.intervalRange(0, 10, 14, 1, TimeUnit.SECONDS)//延迟14s，上一个Observable结束时，已缓冲0,1，按顺序排空0,1
                                .map(new Function<Long, String>() {
                                    @Override
                                    public String apply(@NonNull Long aLong) throws Exception {
                                        return String.valueOf((char) (aLong + 48));
                                    }
                                })
                )
        ).blockingSubscribe(new PrintObserver());

    }

    @Test
    public void concatWith() throws Exception {
        //实例方法
    }

    @Test
    public void concatArray() throws Exception {
        //concat()变体
    }

    @Test
    public void concatArrayDelayError() throws Exception {
        //concatDelayError()变体
    }

    @Test
    public void concatArrayEager() throws Exception {
        //concatEager()的变体
    }

    @Test
    public void concatMap() throws Exception {
        //map()+concat()
    }

    @Test
    public void reduce() throws Exception {
        long res = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .reduce(0L, new BiFunction<Long, Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong, @NonNull Long aLong2) throws Exception {
                        println(aLong + ":" + aLong2);
                        return aLong + aLong2;
                    }
                })
                .blockingGet();
        println(res + "");
    }

    @Test
    public void reduceWith() throws Exception {
        Observable.range(0, 10)
                .reduceWith(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        println("callable");
                        return 0;
                    }
                }, new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer s, @NonNull Integer integer) throws Exception {
                        println(s + ":" + integer);
                        return integer + s;
                    }
                }).subscribe(new PrintSingle());
    }

    private class Adder {
        private int value;

        public void add(int a) {
            value += a;
        }

        @Override
        public String toString() {
            return "和：" + value;
        }
    }

    @Test
    public void collect() throws Exception {
        //把数据源发出的数据项收集到一个单一可变的数据结构，并返回这个数据结构
        Observable.range(0, 10)
                .collect(new Callable<Adder>() {
                    @Override
                    public Adder call() throws Exception {
                        return new Adder();
                    }
                }, new BiConsumer<Adder, Integer>() {
                    @Override
                    public void accept(Adder integers, Integer integer) throws Exception {
                        integers.add(integer);
                    }
                }).subscribe(new PrintSingle());
    }

    @Test
    public void collectInto() throws Exception {
        Observable.range(0, 10)
                .collectInto(new Adder(), new BiConsumer<Adder, Integer>() {
                    @Override
                    public void accept(Adder integers, Integer integer) throws Exception {
                        integers.add(integer);
                    }
                }).subscribe(new PrintSingle());

    }
}
