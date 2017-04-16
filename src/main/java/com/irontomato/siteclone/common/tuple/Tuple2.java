package com.irontomato.siteclone.common.tuple;

public class Tuple2<T1,T2> implements Tuple {
    private T1 v1;

    private T2 v2;

    Tuple2(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T1 get_1() {
        return v1;
    }

    public T2 get_2() {
        return v2;
    }
}
