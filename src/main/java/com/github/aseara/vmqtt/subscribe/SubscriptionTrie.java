package com.github.aseara.vmqtt.subscribe;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionTrie {


    /**
     * 增加订阅
     * @param sub 待新增订阅
     * @return 是否新增
     */
    public boolean subscribe(Subscriber sub) {
        return true;
    }

    /**
     * 取消订阅
     * @param sub 待新增订阅
     * @return 是否新增
     */
    public boolean unsubscribe(Subscriber sub) {
        return true;
    }

    /**
     * 查找订阅
     * @param topic 主题
     * @return 订阅列表
     */
    public List<Subscriber> lookup(String topic) {
        return new ArrayList<>();
    }

}
