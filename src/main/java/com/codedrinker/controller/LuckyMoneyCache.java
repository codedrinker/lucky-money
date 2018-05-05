package com.codedrinker.controller;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by codedrinker on 05/05/2018.
 */
public class LuckyMoneyCache {
    private static final Logger logger = LoggerFactory.getLogger(LuckyMoneyCache.class);

    private static LoadingCache<String, Optional<Map<String, Integer>>> loadingCache;

    static {
        loadingCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(3, TimeUnit.DAYS)
                .removalListener((RemovalListener<String, Optional<Map<String, Integer>>>) listener -> {
                    if (listener.getValue().isPresent()) {
                        Map<String, Integer> stringIntegerMap = listener.getValue().get();
                        if (stringIntegerMap.containsKey(listener.getKey())) {
                            logger.info("key -> {} is expired at count -> {}", listener.getKey(), stringIntegerMap.get(listener.getKey()));
                        }
                    }
                })
                .build(new CacheLoader<String, Optional<Map<String, Integer>>>() {
                    @Override
                    public Optional<Map<String, Integer>> load(String key) throws Exception {
                        HashMap<String, Integer> reference = new HashMap<>();
                        reference.put(key, 0);
                        return Optional.fromNullable(reference);
                    }
                });
    }

    public Integer count(String url) {
        try {
            if (loadingCache.get(url).isPresent()) {
                return loadingCache.get(url).get().get(url);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void incCount(String url) {
        try {
            if (loadingCache.get(url).isPresent()) {
                Map<String, Integer> map = loadingCache.get(url).get();
                if (map.containsKey(url)) {
                    map.put(url, map.get(url) + 1);
                } else {
                    map.put(url, 1);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getCurrentCache() {
        ConcurrentMap<String, Optional<Map<String, Integer>>> caches = loadingCache.asMap();
        Set<String> keys = caches.keySet();
        Map<String, Integer> map = new HashMap();
        for (String key : keys) {
            Optional<Map<String, Integer>> mapOptional = caches.get(key);
            if (mapOptional.isPresent()) {
                Map<String, Integer> stringIntegerMap = mapOptional.get();
                if (stringIntegerMap.containsKey(key)) {
                    map.put(key, stringIntegerMap.get(key));
                }
            }
        }
        return map;
    }

    public static void main(String[] args) {
        String url = "http://www.baidu.com";
        LuckyMoneyCache luckyMoneyCache = new LuckyMoneyCache();
        System.out.println(luckyMoneyCache.count(url));
        luckyMoneyCache.incCount(url);
        System.out.println(luckyMoneyCache.count(url));
        luckyMoneyCache.incCount(url);
        System.out.println(luckyMoneyCache.count(url));
        luckyMoneyCache.incCount(url);
        System.out.println(luckyMoneyCache.count(url));

        System.out.println(luckyMoneyCache.getCurrentCache());

        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        luckyMoneyCache.incCount(url);
        System.out.println(luckyMoneyCache.count(url));
        luckyMoneyCache.incCount(url);
        System.out.println(luckyMoneyCache.count(url));
        System.out.println(luckyMoneyCache.getCurrentCache());
    }
}
