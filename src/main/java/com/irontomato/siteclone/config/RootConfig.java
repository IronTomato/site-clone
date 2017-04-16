package com.irontomato.siteclone.config;

import com.irontomato.siteclone.retriable.RetriableExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RootConfig {

    @Value("${siteclone.retriable.parallel-scale}")
    private int parallelScale;

    @Bean
    public ExecutorService threadpool(){
        return Executors.newFixedThreadPool(parallelScale);
    }

    @Bean
    public RetriableExecutor retriableExecutor(ExecutorService threadpool){
        return new RetriableExecutor(threadpool, parallelScale);
    }

}
