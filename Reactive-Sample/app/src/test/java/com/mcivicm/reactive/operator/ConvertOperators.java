package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static android.R.id.list;

/**
 * 转换操作符
 */

public class ConvertOperators extends BaseOperators {

    @Test
    public void to() throws Exception {
        //求0-10的和
        int result = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                //将整个数据源转换成另一种类型
                .to(new Function<Observable<Long>, Integer>() {
                    @Override
                    public Integer apply(@NonNull Observable<Long> longObservable) throws Exception {
                        return longObservable.reduce(0, new BiFunction<Integer, Long, Integer>() {
                            @Override
                            public Integer apply(@NonNull Integer integer, @NonNull Long aLong) throws Exception {
                                println("emit: " + aLong);
                                return integer + Integer.valueOf(String.valueOf(aLong));//求和
                            }
                        }).blockingGet();
                    }
                });
        println("最终的结果是：" + result);
    }

    @Test
    public void toFuture() throws Exception {
        Future<Long> longFuture = Observable.just(5L).delay(5, TimeUnit.SECONDS)
                .toFuture();
        println("wait the data to come...");
        println("get: " + longFuture.get());
    }

    @Test
    public void toList() throws Exception {

        Single<List<Long>> longSingle = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("emit: " + aLong);
                    }
                })
                .toList();//返回Single

        List<Long> longList = longSingle.blockingGet();

        println("the final list: " + String.valueOf(list));
    }

    @Test
    public void toMap() throws Exception {
        Single<Map<String, Long>> mapSingle = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("emit: " + aLong);
                    }
                }).toMap(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (aLong + 65)) + String.valueOf((char) (aLong + 97));
                    }
                });//返回Single
        Map<String, Long> stringLongMap = mapSingle.blockingGet();
        println("final result: " + String.valueOf(stringLongMap));
    }

    @Test
    public void toMultimap() throws Exception {
        Single<Map<String, Collection<Long>>> stringCollectionSing = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        println("emit: " + aLong);
                    }
                })
                .toMultimap(new Function<Long, String>() {   //返回Single
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (65 + aLong));
                    }
                }, new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        return aLong + 10;
                    }
                }, new Callable<Map<String, Collection<Long>>>() {
                    @Override
                    public Map<String, Collection<Long>> call() throws Exception {
                        return new HashMap<String, Collection<Long>>(0);
                    }
                }, new Function<String, Collection<? super Long>>() {
                    @Override
                    public Collection<? super Long> apply(@NonNull String s) throws Exception {
                        return new ArrayList<Long>(0);
                    }
                });
        Map<String, Collection<Long>> stringCollectionMap = stringCollectionSing.blockingGet();
        println(stringCollectionMap.get("A").getClass().getName());
        println("final result: " + String.valueOf(stringCollectionMap));
    }

    @Test
    public void toSortedList() throws Exception {
        Single<List<Integer>> listSingle = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(@NonNull Long aLong) throws Exception {
                        int mapped = new Random().nextInt(10);
                        println("emit: " + mapped);
                        return mapped;
                    }
                })
                .toSortedList();//返回Single
        List<Integer> integerList = listSingle.blockingGet();
        println("final result: " + String.valueOf(integerList));
    }


    @Test
    public void sorted() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).
                map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(@NonNull Long aLong) throws Exception {
                        int random = new Random().nextInt(10);
                        println("emit: " + random);
                        return random;
                    }
                })
                .sorted()
                .blockingSubscribe(new PrintObserver());
    }

    //返回一个迭代源，该迭代源一直阻塞直到数据源发射一个数据项并返回这个数据项
    @Test
    public void blockingNext() throws Exception {
        Iterable<Long> iterable = Observable.intervalRange(0, 10, 3, 3, TimeUnit.SECONDS).blockingNext();
        iterable.forEach(new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                println("get: " + aLong);
            }
        });
    }

    @Test
    public void blockingLatest() throws Exception {
        //返回一个返回Observable最近发送的数据项的迭代器，必要时等待直到有可用的数据项。
        Iterable<Long> iterable = Observable.intervalRange(0, 10, 1, 1, TimeUnit.SECONDS).blockingLatest();
        iterable.forEach(new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                println("get: " + aLong);
            }
        });
    }

    @Test
    public void blockingMostRecent() throws Exception {
        Iterable<Long> iterable = Observable.intervalRange(0, 10, 0, 10, TimeUnit.MILLISECONDS).blockingMostRecent(Long.MAX_VALUE);
        iterable.forEach(new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                println("get: " + aLong);
            }
        });
    }

    @Test
    public void blockingIterable() throws Exception {
        Iterable<Long> iterable = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .blockingIterable();
        iterable.forEach(new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                println("get: " + aLong);
            }
        });
    }


}
