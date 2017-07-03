package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

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
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).buffer(3)
                .blockingSubscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        if (o instanceof List)
                            println("收到足够的数据才发射，不是边收边发：" + String.valueOf(((List) o).size()));
                    }
                });
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

    private class Student {
        private String name;
        private int score;

        public String getName() {
            return name;
        }

        public Student setName(String name) {
            this.name = name;
            return this;
        }

        public int getScore() {
            return score;
        }

        public Student setScore(int score) {
            this.score = score;
            return this;
        }
    }

    @Test
    public void groupBy() throws Exception {
        Observable.range(0, 80)//80人
                .map(new Function<Integer, Student>() {
                    @Override
                    public Student apply(@NonNull Integer integer) throws Exception {
                        return new Student().setName("学生" + integer).setScore(new Random().nextInt(100));
                    }
                })//随机一个分数
                .groupBy(new Function<Student, String>() {
                    @Override
                    public String apply(@NonNull Student student) throws Exception {
                        String rank;
                        if (student.getScore() > 90) {
                            rank = "优秀";
                        } else if (student.getScore() > 80) {
                            rank = "良好";
                        } else if (student.getScore() > 60) {
                            rank = "及格";
                        } else {
                            rank = "不及格";
                        }
                        return rank;
                    }
                })//贴上等级标签
                .subscribe(new Observer<GroupedObservable<String, Student>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        println("onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull GroupedObservable<String, Student> stringStudentGroupedObservable) {
                        //如果不订阅的话，要调用take清除缓存(discard their buffers) stringStudentGroupedObservable.take(Long.MAX_VALUE);
                        //当然也可以直接stringStudentGroupedObservable.subscribe();
                        stringStudentGroupedObservable.subscribe(student -> println("onNext:" + student.getScore() + ":" + stringStudentGroupedObservable.getKey()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        println("onError");
                    }

                    @Override
                    public void onComplete() {
                        println("onComplete");
                    }
                });//接收带有标签的数据
    }

    @Test
    public void map() throws Exception {
        Observable.just(0)
                .map(new Function<Integer, List<Integer>>() {
                    @Override
                    public List<Integer> apply(@NonNull Integer integer) throws Exception {
                        return null;
                    }
                });
    }

    @Test
    public void scan() throws Exception {
        //和Mathematica的FoldList语法类似，叫做accumulator
        Observable.fromArray(new String[]{"我", "是", "中", "国", "人"})
                .scan(new BiFunction<String, String, String>() {
                    @Override
                    public String apply(@NonNull String s, @NonNull String s2) throws Exception {
                        return s + s2;
                    }
                })
                .takeLast(1)
                .subscribe(new SimpleObserver() {
                    @Override
                    public void onNext(@NonNull Object o) {
                        super.onNext(o);
                        println(String.valueOf(o));
                    }
                });

    }

    @Test
    public void window() throws Exception {
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .window(3)
                .blockingSubscribe(new Observer<Observable<Long>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        println("onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull Observable<Long> longObservable) {
                        longObservable.subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                println("window-onSubscribe: 收到数据就发射，有明显的打开行为");
                            }

                            @Override
                            public void onNext(@NonNull Long aLong) {
                                println("window-" + aLong);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                println("window-onError");
                            }

                            @Override
                            public void onComplete() {
                                println("window-onComplete：收到足够的数据就完成，有明显的关闭行为");
                            }
                        });
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
}
