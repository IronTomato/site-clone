package com.irontomato.siteclone.common.tuple;

public class Tuple3<T1,T2,T3> {
    private T1 v1;
    private T2 v2;
    private T3 v3;

    Tuple3(T1 v1, T2 v2, T3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public T1 get_1() {
        return v1;
    }

    public T2 get_2() {
        return v2;
    }

    public T3 get_3() {
        return v3;
    }
}
