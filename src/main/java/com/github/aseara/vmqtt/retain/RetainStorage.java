package com.github.aseara.vmqtt.retain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;

public class RetainStorage {

    private final TrieNode root = new TrieNode(null, "");

    public void retain(RetainMessage message) {
        if (message.payload().length() != 0) {
            addRetain(message);
        } else {
            removeRetain(message);
        }
    }

    public List<RetainMessage> lookup(String[] levels) {
        List<RetainMessage> msgs = new ArrayList<>();
        lookup(root, levels, 0, msgs);
        return msgs;
    }

    private void lookup(TrieNode curr, String[] levels, int index, List<RetainMessage> msgs) {
        if (index == levels.length) {
            msgs.add(curr.message);
            return;
        }

        if ("#".equals(levels[index])) {
            for (TrieNode child : curr.children.asMap().values()) {
                lookup(child, msgs);
            }
            return;
        }

        if ("+".equals(levels[index])) {
            for (TrieNode child : curr.children.asMap().values()) {
                lookup(child, levels, index + 1, msgs);
            }
            return;
        }

        TrieNode child = curr.children.getIfPresent(levels[index]);
        if (child != null) {
            lookup(child, levels, index + 1, msgs);
        }
    }

    private void lookup(TrieNode curr, List<RetainMessage> msgs) {
        msgs.add(curr.message);
        for (TrieNode child : curr.children.asMap().values()) {
            lookup(child, msgs);
        }
    }


    private void addRetain(RetainMessage msg) {
        TrieNode curr = root;
        String[] levels = msg.levels();
        for (String level : levels) {
            TrieNode child = curr.children.getIfPresent(level);
            if (child == null) {
                child = new TrieNode(curr, level);
                curr.children.put(level, child);
            }
            curr = child;
        }
        curr.message = msg;
    }

    private void removeRetain(RetainMessage msg) {
        TrieNode curr = root;
        String[] levels = msg.levels();
        for (String level : levels) {
            TrieNode child = curr.children.getIfPresent(level);
            if (child == null) {
                // retain topic not exist
                return;
            }
            curr = child;
        }
        curr.message = null;
        if (curr.children.size() == 0) {
            curr.orphan();
        }
    }

    private static class TrieNode {
        private final TrieNode parent;
        private final String level;

        private RetainMessage message;

        private final Cache<String, TrieNode> children = CacheBuilder.newBuilder().softValues().build();

        public TrieNode(TrieNode parent, String level) {
            this.parent = parent;
            this.level = level;
        }

        public void orphan() {
            if (parent == null) {
                return;
            }
            parent.children.invalidate(level);
            if (parent.message == null && parent.children.size() == 0) {
                parent.orphan();
            }
        }

    }

}
