package com.lidynast.demo_zxing.rxbus;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * <p>
 * 功能描述：观察者模式
 * </p>
 * Created by weiwei on 2017/5/8 15:49.
 */
@SuppressWarnings("ALL")
public class RxBus {

    private HashMap<Object, List<Subject>> maps = new HashMap<>();
    private static RxBus instance;

    private RxBus() {
        super();
    }

    /**
     * 创建单例对象
     *
     * @return
     */
    public static RxBus get() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册观察者
     *
     * @param tag
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz) {
        List<Subject> subjects = maps.get(tag);
        if (subjects == null) {
            subjects = new ArrayList<>();
            maps.put(tag, subjects);
        }
        Subject<T, T> subject = PublishSubject.<T>create();
        subjects.add(subject);
        return subject;
    }

    /**
     * 解除绑定
     *
     * @param tag
     * @param observable
     */
    public void unregister(@NonNull Object tag, @NonNull Observable observable) {
        List<Subject> subjects = maps.get(tag);
        if (subjects != null) {
            subjects.remove((Subject) observable);
            if (subjects.isEmpty()) {
                maps.remove(tag);
            }
        }
    }

    public void post(@NonNull Object o) {
        post(o.getClass().getSimpleName(), o);
    }

    /**
     * 传值
     *
     * @param tag
     * @param o
     */
    public void post(@NonNull Object tag, @NonNull Object o) {
        List<Subject> subjects = maps.get(tag);
        if (subjects != null && !subjects.isEmpty()) {
            for (Subject s : subjects) {
                s.onNext(o);
            }
        }
    }
}
