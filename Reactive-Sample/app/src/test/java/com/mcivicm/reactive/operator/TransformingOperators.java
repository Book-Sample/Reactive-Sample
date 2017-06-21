package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 转换操作符
 */

public class TransformingOperators extends BaseOperators {
    @Test
    public void buffer() throws Exception {
        Observable.range(0, 10).buffer(3)
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        if (o instanceof List)
                            println(String.valueOf(((List) o).size()));
                    }
                });
    }

    @Test
    public void buffer1() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).buffer(3)
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        if (o instanceof List)
                            println(String.valueOf(((List) o).size()));
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
    public void flatMap() throws Exception {
        //细胞分裂
        //1变5,5变10,最后发射10个，而不是1+5+10=16个。
        Observable.just(0)
                .flatMap(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                        return Observable.range(integer, 5);
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                        return Observable.just(integer, integer);
                    }
                })
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        println(String.valueOf(o));
                    }
                });
    }

    @Test
    public void groupBy() throws Exception {
        Observable.range(0, 80)//80人
                .map(new Function<Integer, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> apply(@NonNull Integer integer) throws Exception {
                        HashMap<String, Integer> result = new HashMap<String, Integer>(0);
                        result.put("学生" + integer, new Random().nextInt(100));
                        return result;
                    }
                })//随机一个分数
                .groupBy(new Function<HashMap<String, Integer>, String>() {
                    @Override
                    public String apply(@NonNull HashMap<String, Integer> map) throws Exception {
                        if (map.get() < 60) {
                            return "不及格";
                        } else if (integer < 6) {
                            return "小于6"
                        }
                    }
                })
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        println(String.valueOf(o));
                    }
                });

    }

    @Test
    public void map() throws Exception {
        Observable.just(0).map(new Function<Integer, List<Integer>>() {
            @Override
            public List<Integer> apply(@NonNull Integer integer) throws Exception {
                return null;
            }
        });
    }
}
