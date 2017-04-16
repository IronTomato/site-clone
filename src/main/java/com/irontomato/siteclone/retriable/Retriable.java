package com.irontomato.siteclone.retriable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 此类的实例代表一个执行可能会失败，但可以进行重试的任务，
 * 通过构造方法参数，可以设置任务的重试次数上限、超时时间、重试时间间隔、任务创建时间，
 * 需要配合一个执行器来执行
 * @see RetriableExecutor
 */
public abstract class Retriable implements Delayed {

    /**
     * 已重试次数
     */
    private int callCount = 0;

    /**
     * 重试次数上限，负值代表无上限
     */
    private int callCountLimit = -1;

    /**
     * 最近一次调用的时间
     */
    private long lastCallTime = 0;

    /**
     * 任务创建时间
     */
    private long createTime;

    /**
     * 生命上限时间，负值表示无上限
     */
    private long lifeTime = -1;

    /**
     * 重试时间间隔，负值表示无间隔
     */
    private long delay = -1;

    /**
     * 表明此任务是否已被取消
     */
    private volatile boolean canceled = false;

    protected Logger log = LogManager.getLogger(getClass());

    public static RetriableComparator COMPARATOR = new RetriableComparator();

    /**
     * 通过构造方法参数设置实例的属性，将会影响任务的执行策略
     * @param callCountLimit 重试次数上限，默认为 -1
     * @param lifeTime 生命持续时间，默认为 -1
     * @param delay 重试的时间间隔，默认为 -1
     * @param createTime 任务的创建时间，默认为当前系统时间
     */
    public Retriable(int callCountLimit, long lifeTime, long delay, long createTime) {
        this.callCountLimit = callCountLimit;
        this.lifeTime = lifeTime;
        this.delay = delay;
        this.createTime = createTime >= 0 ? createTime : System.currentTimeMillis();
    }

    /**
     * 无参数的构造方法，将创建一个没有重试次数限制、不会超时、没有重试时间间隔、
     * 创建时间为当前系统时间的可重试任务
     */
    public Retriable() {
        createTime = System.currentTimeMillis();
    }

    /**
     * 可重试任务的主体，通过实现此方法执行业务任务，返回一个 boolean类型的值
     * 表示任务执行是否成功
     * @return 代表任务执行成功与否的 boolean值
     */
    protected abstract boolean call();

    /**
     * 执行器通过此方法调用call()方法，同时改变此实例内部状态来反映此次调用尝试
     * @return call()方法执行的结果
     */
    public boolean tryCall() {
        lastCallTime = System.currentTimeMillis();
        callCount++;
        return call();
    }

    /**
     *
     * @return 此任务是否已超时
     */
    public boolean isExpired() {
        return lifeTime > 0 && System.currentTimeMillis() - createTime >= lifeTime;
    }

    /**
     *
     * @return 此任务的重试次数是否以达到上限
     */
    public boolean isCallCountLimited() {
        return callCountLimit > 0 && callCount >= callCountLimit;
    }

    /**
     *
     * @return 此任务是否已被取消
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * 取消此任务
     */
    public void cancel() {
        canceled = true;
    }

    /**
     *
     * @param unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return delay > 0 ?
                lastCallTime + delay - System.currentTimeMillis() :
                0;
    }

    @Override
    public int compareTo(Delayed o) {
        TimeUnit unit = TimeUnit.NANOSECONDS;
        return Long.compare(this.getDelay(unit), o.getDelay(unit));
    }

    /**
     * 任务执行成功时(call()方法返回true)，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务执行成功事件
     */
    public void onCallSuccessed() {
    }

    /**
     * 任务超时时(isExpired()方法返回true)，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务超时事件
     */
    public void onExpired() {
    }

    /**
     * 任务重试次数达到上限时(isCallCountLimited()方法返回true)，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务执重试次数达到上限事件
     */
    public void onCallCountLimited() {
    }

    /**
     * 任务执行失败时(call()方法返回false)，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务执行失败事件
     */
    public void onCallFailed() {
    }

    /**
     * 任务取消时(isCanceled()方法返回true)，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务取消事件
     */
    public void onCanceled() {

    }

    /**
     * 任务放弃时，执行器将调用此方法，
     * 默认方法什么也不干，子类可以重写此方法来监听任务放弃事件，
     * 重试次数达到上限、超时、取消，都会引发任务放弃
     */
    public void onGaveUp() {
    }

    public int getCallCount() {
        return callCount;
    }

    public int getCallCountLimit() {
        return callCountLimit;
    }

    public long getLastCallTime() {
        return lastCallTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public long getDelay() {
        return delay;
    }

    /**
     * 默认的可重试任务比较器，可用于优先级排序，
     * 将依次依据已重试次数、任务创建时间、剩余时延来比较，
     * 重试次数更少、创建时间更早、剩余时延更短的任务具有更小的比较值
     */
    public static class RetriableComparator implements Comparator<Retriable> {

        @Override
        public int compare(Retriable o1, Retriable o2) {
            if (o1.callCount == o2.callCount) {
                if (o1.createTime == o2.createTime) {
                    return o1.compareTo(o2);
                }
                return o1.createTime < o2.createTime ? -1 : 1;
            }
            return o1.callCount < o2.callCount ? -1 : 1;
        }
    }

}
