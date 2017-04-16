package com.irontomato.siteclone.common.tuple;

public class Pair<L,R> extends Tuple2<L,R> {

    public Pair(L left, R right) {
        super(left, right);
    }

    public L getLeft(){
        return get_1();
    }

    public R getRight(){
        return get_2();
    }

    public static <L,R> Pair<L, R> of(L left, R right){
        return new Pair<>(left, right);
    }
}
