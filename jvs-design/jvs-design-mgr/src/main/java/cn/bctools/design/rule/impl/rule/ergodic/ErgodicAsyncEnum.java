package cn.bctools.design.rule.impl.rule.ergodic;

import lombok.Getter;

/**
 * 异步执行的类型
 *
 * @author jvs
 */
@Getter
public enum ErgodicAsyncEnum {
    /**
     * 当逻辑执行时逻辑中的线默认为同步执行，当存在一个节点下有多个节点时根据顺序执行。
     */
    Asynchronous("同步"),
    /**
     * 线-异步执行： 设置一个节点下多个线同时异步阻塞执行时，将会同时执行不会等待上一条线执行完后再执行
     * 循环容器-异步执行：  在循环容器中服务可以设置循环容器的执行规则为异步，如循环 10次，每次休眠 1 秒，循环容器异步总时间不会超过 2 秒。
     */
    Asynchronous_Blocking("异步阻塞");

    public String msg;

    ErgodicAsyncEnum(String msg) {
        this.msg = msg;
    }
}
