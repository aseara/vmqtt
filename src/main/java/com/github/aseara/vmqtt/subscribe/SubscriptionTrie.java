package com.github.aseara.vmqtt.subscribe;

import io.vertx.core.VertxException;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SubscriptionTrie {

    private final TrieNode root = new TrieNode("", null);

    private final Lock readLock;
    private final Lock writeLock;

    private int count;

    public SubscriptionTrie() {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    /**
     * 增加订阅
     * @param sub 待新增订阅
     */
    public void subscribe(Subscriber sub) {
        writeLock.lock();
        try {
            TrieNode curr = root;
            String[] levels = sub.getLevels();
            for (int i = 0; i < levels.length; i++) {
                String level = levels[i];
                checkLevel(level, i == levels.length - 1, sub.getSubTopic());
                TrieNode child = curr.children.get(level);
                if (child == null) {
                    child = new TrieNode(level, curr);
                    curr.children.put(level, child);
                }
                curr = child;
            }

            if (curr.addSub(sub)) {
                count++;
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 取消订阅
     * @param sub 待取消订阅
     */
    public void unsubscribe(Subscriber sub) {
        writeLock.lock();
        try {
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
                count--;
            }

            if (curr.subs.size() == 0 && curr.children.isEmpty()) {
                curr.orphan();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 查找订阅
     * @param topicLevels topic split by /
     * @return 订阅列表
     */
    public Collection<Subscriber> lookup(String[] topicLevels) {
        readLock.lock();
        try {
            Map<String, Subscriber> subMap = new LinkedHashMap<>();
            lookup(root, topicLevels, 0, subMap);
            return subMap.values();
        } finally {
            readLock.unlock();
        }
    }

    public int getCount() {
        return count;
    }

    private void checkLevel(String level, boolean isLast, String topic) {
        if (level.length() > 1 && (level.indexOf('+') >= 0 || level.indexOf('#') >= 0)) {
            throw new VertxException("subscribe topic " + topic + " is not valid!");
        }
        if ("#".equals(level) && !isLast) {
            throw new VertxException("subscribe topic " + topic + " is not valid!");
        }
    }

    private void lookup(TrieNode curr, String[] levels, int index, Map<String, Subscriber> subMap) {
        if (index == levels.length) {
            checkAndAdd(subMap, curr.subs);
            return;
        }
        TrieNode child = curr.children.get(levels[index]);
        if (child != null) {
            lookup(child, levels, index + 1, subMap);
        }

        child = curr.children.get("+");
        if (child != null) {
            lookup(child, levels, index + 1, subMap);
        }

        child = curr.children.get("#");
        if (child != null) {
            checkAndAdd(subMap, child.subs);
        }
    }

    private void checkAndAdd(Map<String, Subscriber> resultMap, Map<String, Subscriber> nodeMap) {
        nodeMap.forEach((clientId, sub) -> {
            Subscriber exit = resultMap.get(clientId);
            if (exit == null || sub.getQos().value() > exit.getQos().value()) {
                resultMap.put(clientId, sub);
            }
        });
    }

    @Getter
    @Setter
    private static class TrieNode {
        private final String level;
        private final Map<String, Subscriber> subs = new HashMap<>();
        private final TrieNode parent;
        private final Map<String, TrieNode> children = new HashMap<>();

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
