package com.yenanren.socket_kafka.worker;

public interface WorkerInterface<J> {
    /**
     * 事前准备工作
     * @param job
     */
    void before(J job);

    /**
     * 执行 核心相关 事件
     * @param job
     */
    void process(J job);

    /**
     * 执行 其他附加 事件
     * @param job
     */
    void after(J job);

    /**
     * 停止 事件
     * @param job
     */
    void onDown(J job);

    /**
     * 错误事件 补偿 以及 打印
     * @param job
     */
    void onError(J job);


}
