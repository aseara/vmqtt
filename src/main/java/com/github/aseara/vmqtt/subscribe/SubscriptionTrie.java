package com.github.aseara.vmqtt.subscribe;

import com.github.aseara.vmqtt.common.TopicUtil;
import io.vertx.core.VertxException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscriptionTrie {

    private final TrieNode root = new TrieNode("", null);

    private final AtomicInteger count = new AtomicInteger();

    /**
     * 增加订阅
     * @param sub 待新增订阅
     */
    public void subscribe(Subscriber sub) {
        TrieNode curr = root;
        String[] levels = sub.getLevels();
        for (int i = 0; i < levels.length; i++) {
            String level = levels[i];
            if (level.length() > 1 && (level.indexOf('+') >= 0 || level.indexOf('#') >= 0)) {
                throw new VertxException("subscribe topic " + sub.getSubTopic() + " is not valid!");
            }
            if ("#".equals(level) && i != levels.length - 1) {
                throw new VertxException("subscribe topic " + sub.getSubTopic() + " is not valid!");
            }
            TrieNode child = curr.children.get(level);

            if (child == null) {
                child = new TrieNode(level, curr);
                curr.children.put(level, child);
            }
            curr = child;
        }

        if (curr.addSub(sub)) {
            count.incrementAndGet();
        }

    }

    /**
     * 取消订阅
     * @param sub 待取消订阅
     */
    public void unsubscribe(Subscriber sub) {
        TrieNode curr = root;

        String[] levels = sub.getLevels();
        for (String level : levels) {
            TrieNode child = curr.children.get(level);
            if (child == null) {
                // subscription not exist.
                return;
            }
            curr = child;
        }

        if (curr.removeSub(sub)) {
            count.decrementAndGet();
        }

        if (curr.subs.size() == 0 && curr.children.isEmpty()) {
            curr.orphan();
        }

    }

    /**
     * 查找订阅
     * @param topic 主题
     * @return 订阅列表
     */
    public List<Subscriber> lookup(String topic) {
        List<Subscriber> subs = new ArrayList<>();
        String[] levels = TopicUtil.splitTopic(topic);
        lookup(root, levels, 0, subs);
        return subs;
    }

    private void lookup(TrieNode curr, String[] levels, int index, List<Subscriber> subs) {
        if (index == levels.length) {
            subs.addAll(curr.subs.values());
            return;
        }
        TrieNode child = curr.children.get(levels[index]);
        if (child != null) {
            lookup(child, levels, index + 1, subs);
        }

        child = curr.children.get("+");
        if (child != null) {
            lookup(child, levels, index + 1, subs);
        }

        child = curr.children.get("#");
        if (child != null) {
            subs.addAll(child.subs.values());
        }
    }

    @Getter
    @Setter
    private static class TrieNode {
        private final String level;
        private final ConcurrentMap<String, Subscriber> subs = new ConcurrentHashMap<>();
        private final TrieNode parent;
        private final ConcurrentMap<String, TrieNode> children = new ConcurrentHashMap<>();

        public TrieNode(String level, TrieNode parent) {
            this.level = level;
            this.parent = parent;
        }

        public boolean addSub(Subscriber sub) {
            Subscriber old = subs.get(sub.getClientId());
            if (old == null || old.getQos() != sub.getQos()) {
                subs.put(sub.getClientId(), sub);
            }
            return old == null;
        }

        public boolean removeSub(Subscriber sub) {
            return subs.remove(sub.getClientId()) != null;
        }

        public void orphan() {
            if (parent == null) {
                return;
            }
            parent.children.remove(level);
            if (parent.subs.size() == 0 && parent.children.isEmpty()) {
                parent.orphan();
            }
        }

    }

}
