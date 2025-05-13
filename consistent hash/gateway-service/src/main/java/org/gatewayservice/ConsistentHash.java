package org.gatewayservice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;

public class ConsistentHash<T> {
    private final TreeMap<Integer, T> circle = new TreeMap<>();
    private final int virtualNodes;
    private final Function<T, String> nodeKeyFunction;

    public ConsistentHash() {
        this(100, Object::toString);
    }

    public ConsistentHash(int virtualNodes, Function<T, String> nodeKeyFunction) {
        this.virtualNodes = virtualNodes;
        this.nodeKeyFunction = nodeKeyFunction;
    }

    public void update(List<T> nodes) {
        circle.clear();
        for (T node : nodes) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(node.toString() + i);
                circle.put(hash, node);
            }
        }
    }

    public synchronized Optional<T> get(String key) {
        if (circle.isEmpty()) {
            return Optional.empty(); // 안전하게 Optional로 반환
        }

        int hash = hash(key);
        SortedMap<Integer, T> tailMap = circle.tailMap(hash);
        int targetHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        return Optional.ofNullable(circle.get(targetHash));
    }

    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ((digest[0] & 0xFF) << 24)
                    | ((digest[1] & 0xFF) << 16)
                    | ((digest[2] & 0xFF) << 8)
                    | (digest[3] & 0xFF);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }
}
