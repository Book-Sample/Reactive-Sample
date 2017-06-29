package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static android.R.id.list;

/**
 * 转换操作符
 */

public class ConvertOperators extends BaseOperators {
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
                .toList();

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
                });
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
                .toMultimap(new Function<Long, String>() {
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


    }
}
