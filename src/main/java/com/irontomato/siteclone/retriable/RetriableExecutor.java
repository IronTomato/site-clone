package com.irontomato.siteclone.retriable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RetriableExecutor {

    private BlockingQueue<Retriable> priorityQueue = new PriorityBlockingQueue<>(16, Retriable.COMPARATOR);
    private BlockingQueue<Retriable> delayQueue = new DelayQueue<>();
    private ExecutorService pool;
    private Thread priorityDaemon;
    private static AtomicInteger priorityDarmonNum = new AtomicInteger(0);
    private Thread delayDaemon;
    private static AtomicInteger delayDaemonNum = new AtomicInteger(0);
    private Semaphore semaphore;
    protected Logger log = LogManager.getLogger(getClass());
    private volatile boolean started = false;

    public RetriableExecutor(){
        this(Executors.newFixedThreadPool(16),16);
    }

    public RetriableExecutor(ExecutorService pool, int parallelScale) {
        this.pool = pool;
        semaphore = new Semaphore(parallelScale);
    }

    @PostConstruct
    public synchronized void start() {
        if (started) {
            throw new IllegalStateException("RetriableExecutor already started.");
        }
        priorityDaemon = this.new PriorityDaemonThread();
        delayDaemon = this.new DelayDaemonThread();
        priorityDaemon.start();
        delayDaemon.start();
        started = true;
    }

    @PreDestroy
    public synchronized void stop() {
        if (!started) {
            return;
        }
        started = false;
        priorityDaemon.interrupt();
        delayDaemon.interrupt();
    }

    public void execute(Retriable retryable) {
        if (retryable.getDelay(TimeUnit.NANOSECONDS) > 0) {
            if (!delayQueue.offer(retryable)) {
                log.info("delayQueue offer failed, element is " + retryable.toString());
            }
        } else {
            if (!priorityQueue.offer(retryable)) {
                log.info("priorityQueue offer failed, element is " + retryable.toString());
            }
        }
    }

    protected void onPriorityDaemonShutdown() {
        if (started) {
            priorityDaemon = this.new PriorityDaemonThread();
            priorityDaemon.start();
        }
    }

    protected void onDelayDaemonShutdown() {
        if (started) {
            delayDaemon = this.new DelayDaemonThread();
            delayDaemon.start();
        }
    }

    private class PriorityDaemonThread extends Thread {

        PriorityDaemonThread() {
            super("PriorityDaemon-" + priorityDarmonNum.incrementAndGet());
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (started) {
                    Retriable retryable = priorityQueue.take();
                    if (retryable.getDelay(TimeUnit.NANOSECONDS) > 0) {
                        if (!delayQueue.offer(retryable)) {
                            log.debug("delayQueue offer failed, element is " + retryable.toString());
                        }
                    } else {
                        semaphore.acquire();
                        pool.execute(() -> {
                            try {
                                new RetriableExecutor.ExecuteUnit(retryable).run();
                            } finally {
                                semaphore.release();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                log.error(this.getName() + " shutdown.", e);
                RetriableExecutor.this.onPriorityDaemonShutdown();
            }
        }
    }

    private class DelayDaemonThread extends Thread {
        public DelayDaemonThread() {
            super("DelayDaemon-" + delayDaemonNum.incrementAndGet());
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (started) {
                    Retriable retryable = delayQueue.take();
                    semaphore.acquire();
                    pool.execute(() -> {
                        try {
                            new RetriableExecutor.ExecuteUnit(retryable).run();
                        } finally {
                            semaphore.release();
                        }
                    });
                }
            } catch (Exception e) {
                log.error(this.getName() + " shutdown.", e);
                RetriableExecutor.this.onDelayDaemonShutdown();
            }
        }
    }

    private class ExecuteUnit implements Runnable {

        private Retriable r;

        ExecuteUnit(Retriable retriable) {
            this.r = retriable;
        }

        @Override
        public void run() {
            if (r.isCanceled()) {
                r.onCanceled();
                r.onGaveUp();
            } else if (r.isCallCountLimited()) {
                r.onCallCountLimited();
                r.onGaveUp();
            } else if (r.isExpired()) {
                r.onExpired();
                r.onGaveUp();
            } else if (r.tryCall()) {
                r.onCallSuccessed();
            } else {
                r.onCallFailed();
                RetriableExecutor.this.execute(r);
            }
        }
    }
}
