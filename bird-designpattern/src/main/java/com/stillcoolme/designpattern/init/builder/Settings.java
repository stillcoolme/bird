package com.stillcoolme.designpattern.init.builder;

import java.util.*;

/**
 * 模仿 org.elasticsearch.client#transport#5.2.2
 *
 * @author: stillcoolme
 * @date: 2019/10/30 15:40
 * @description:
 */
public class Settings {

    public static void main(String[] args) {
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .build();
        Settings newset = Settings.EMPTY;
        newset.settings.putAll(settings.settings);
    }


    private static final Settings EMPTY = new Builder().build();

    private Map<String, String> settings;

    Settings(Map<String, String> settings) {
        this.settings = Collections.unmodifiableMap(settings);
    }

    /**
     * Returns a builder to be used in order to build settings.
     */
    public static Settings.Builder builder() {
        return new Builder();
    }

    /**
     * The settings as a flat {@link java.util.Map}.
     * @return an unmodifiable map of settings
     */
    public Map<String, String> getAsMap() {
        // settings is always unmodifiable
        return this.settings;
    }


    public static class Builder {

        public static final Settings EMPTY_SETTINGS = new Builder().build();

        // we use a sorted map for consistent serialization when using getAsMap()
        private final Map<String, String> map = new TreeMap<>();

        private Builder() {
        }

        public Settings build() {
            return new Settings(map);
        }

        // 牛逼，返回自己
        /**
         * Sets a setting with the provided setting key and value.
         *
         * @param key   The setting key
         * @param value The setting value
         * @return The builder
         */
        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Builder put(Object... settings) {
            if(settings.length == 1) {
                if(settings[0] instanceof Map) {
                    return put((Map) settings[0]);
                } else if(settings[0] instanceof Settings) {
                    return put((Settings) settings[0]);
                }
            }
            if ((settings.length % 2) != 0) {
                throw new IllegalArgumentException(
                        "array settings of key + value order doesn't hold correct number of arguments (" + settings.length + ")");
            }
            for (int i = 0; i < settings.length; i++) {
                put(settings[i++].toString(), settings[i].toString());
            }
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Settings settings) {
            map.putAll(settings.getAsMap());
            return this;
        }

    }
}
