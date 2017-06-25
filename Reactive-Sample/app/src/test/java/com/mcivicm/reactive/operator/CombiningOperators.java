package com.mcivicm.reactive.operator;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * 组合
 */

public class CombiningOperators extends BaseOperators {
    @Test
    public void combineLatest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable<String> o1 = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(aLong -> String.valueOf((char) (aLong + 97)));
        //延迟500毫秒发送
        Observable<String> o2 = Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(aLong -> String.valueOf((char) (aLong + 65)));
        //注意combineLatest是个静态方法，最好放在调用链的开头
        Observable.combineLatest(o1, o2, new BiFunction<String, String, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull String s2) throws Exception {
                return s2 + s;
            }
        }).subscribe(new PrintObserver() {
            @Override
            public void onComplete() {
                super.onComplete();
                latch.countDown();
            }
        });
        latch.await();
    }

    /**
     * Join操作符组合两个Observable发出的数据项，然后根据给每个数据项定义的“时段窗”来选择需要组合的数据。
     * 这些时段窗是以Observable的形式实现，它的生命周期开始于数据项被发射（onSubsribe）的时刻（或时段窗打开）。
     * 当定义时段窗的Observable发射了一个数据项（onNext）或者完成(onComplete),这个数据项关联的时段窗就关闭了（或Observable生命周期结束（onDispose））。
     * 只要数据项的时段窗是打开的，它就会和其他Observable发出的数据项组合。
     * 数据项如何组合的函数自己定义。
     */


    @Test
    public void join() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //左Observable,每秒产生一个数据
        Observable<String> o1 = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(aLong -> String.valueOf((char) (aLong + 97)));
        //右Observable,延迟500毫秒发送,每秒产生一个数据
        Observable<String> o2 = Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(aLong -> String.valueOf((char) (aLong + 65)));

        o1.join(o2, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                println("left:" + s);
                return Observable.just(s)
                        .delay(5, TimeUnit.SECONDS)//定义时间窗的间隔为5s，即5s内数据项都是有效的（可组合的）
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                println("left doOnSubscribe");
                            }
                        })
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                println("left doOnNext:" + s);
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("left doOnComplete");
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("left doOnDispose");
                            }
                        });
            }
        }, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                println("right:" + s);
                return Observable.just(s)
                        //定义时间窗的间隔为0s，即数据项会被立即发送，时间窗关闭，数据项不再有效（不可组合的）
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                println("right doOnSubscribe");
                            }
                        })
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                println("right doOnNext:" + s);
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("right doOnComplete");
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("right doOnDispose");
                            }
                        });
            }
        }, new BiFunction<String, String, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull String s2) throws Exception {
                println("left-right:" + s + "-" + s2);
                return s2 + s;
            }
        }).subscribe(new PrintObserver() {
            @Override
            public void onComplete() {
                println("所有Observable的生命周期到头才结束");
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    public void groupJoin() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //左Observable,每秒产生一个数据
        Observable<String> o1 = Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(aLong -> String.valueOf((char) (aLong + 97)));
        //右Observable,延迟500毫秒发送,每秒产生一个数据
        Observable<String> o2 = Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(aLong -> String.valueOf((char) (aLong + 65)));

        o1.groupJoin(o2, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                println("left:" + s);
                return Observable.just(s)
                        .delay(1, TimeUnit.SECONDS)
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                println("left doOnSubscribe");
                            }
                        })
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                println("left doOnNext:" + s);
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("left doOnComplete");
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("left doOnDispose");
                            }
                        });
            }
        }, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String s) throws Exception {
                println("right:" + s);
                return Observable.just(s)
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                println("right doOnSubscribe");
                            }
                        })
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                println("right doOnNext:" + s);
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("right doOnComplete");
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                println("right doOnDispose");
                            }
                        });
            }
        }, new BiFunction<String, Observable<String>, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull Observable<String> stringObservable) throws Exception {
                println("combine function: " + s);
                return s + stringObservable.blockingSingle("default");
            }
        }).subscribe(new SimpleObserver() {
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
    public void merge() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.merge(Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }), Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟500ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (65 + aLong));//转成大些字母
            }
        })).subscribe(new PrintObserver() {
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
    public void mergeDelayError() throws Exception {
        // without being interrupted by an error notification from one of them
        CountDownLatch latch = new CountDownLatch(1);
        Observable.mergeDelayError(Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                if (aLong == 5) {
                    throw new Exception("some exception");
                }
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }), Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟500ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (65 + aLong));//转成大些字母
            }
        })).subscribe(new PrintObserver() {
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
    public void mergeOrMergeDelayError() throws Exception {
        Semaphore semaphore = new Semaphore(0);
        //有错误信息或立即中断，并传递给Observer
        Observable.merge(Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                if (aLong == 5) {
                    throw new Exception("强行出错");
                }
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }), Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟500ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (65 + aLong));//转成大些字母
            }
        })).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                println("merge开始");
            }
        }).subscribe(new PrintObserver() {
            @Override
            public void onError(@NonNull Throwable e) {
                super.onError(e);
                semaphore.release();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                semaphore.release();
            }
        });
        semaphore.acquire();//等待merge执行完
        //有错误信息只中断错误的Observable，并延迟（等到所有Observable结束发送）传递错误信息给Observer
        Observable.mergeDelayError(Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                if (aLong == 5) {
                    throw new Exception("强行出错");
                }
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }), Observable.intervalRange(0, 10, 500, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟500ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (65 + aLong));//转成大些字母
            }
        })).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                println("***************************************************");
                println("mergeDelayError开始");
            }
        }).subscribe(new PrintObserver() {
            @Override
            public void onError(@NonNull Throwable e) {
                super.onError(e);
                semaphore.release();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                semaphore.release();
            }
        });
        semaphore.acquire();
    }

    @Test
    public void mergeArray() throws Exception {
        //merge的变体
    }

    @Test
    public void mergeArrayDelayError() throws Exception {
        //mergeDelayError的变体
    }

    @Test
    public void mergeWith() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //mergeWith是个实例方法，可链式merge多个Observable
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }).mergeWith(Observable.intervalRange(0, 10, 300, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟300ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (65 + aLong));//转成大些字母
            }
        })).mergeWith(Observable.intervalRange(0, 10, 600, 1000, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {//延迟600ms发送
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (48 + aLong));//转成数字
            }
        })).subscribe(new PrintObserver() {
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
    public void startWith() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
            @Override
            public String apply(@NonNull Long aLong) throws Exception {
                return String.valueOf((char) (97 + aLong));//转成小写字母
            }
        }).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                println("doOnSubscribe");
            }
        })
                .startWith("我去前面探探路")//这里有一个bug：没有startWith时，先执行doOnSubscribe再执行onSubscribe（绝大部分Observable都是这样）,有startWith时，先执行onSubscribe再执行doOnSubscribe
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
    public void switchOnNext() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.switchOnNext(Observable.intervalRange(0, 3, 0, 5, TimeUnit.SECONDS).map(new Function<Long, Observable<String>>() {//每隔五秒产生一个数据源，每个数据源每秒产生一个数据
            @Override
            public Observable<String> apply(@NonNull Long aLong) throws Exception {
                int index = Integer.valueOf(String.valueOf(aLong));
                return Arrays.asList(Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (97 + aLong));//转成小写字母
                    }
                }), Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {//延迟3s发送
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (65 + aLong));//转成大些字母
                    }
                }), Observable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS).map(new Function<Long, String>() {//延迟6s发送
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf((char) (48 + aLong));//转成数字
                    }
                })).get(index);
            }
        })).subscribe(new PrintObserver() {
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
