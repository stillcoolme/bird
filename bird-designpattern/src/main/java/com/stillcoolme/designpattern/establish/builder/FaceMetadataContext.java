package com.stillcoolme.designpattern.establish.builder;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class FaceMetadataContext {

    @Getter
    private static volatile FaceMetadataContext METADATA_CONTEXT;

    public FaceMetadataContext(String zkStorePath) {

    }
    // Builder 可不是单例的！！！
    /**
     * 单例Builder(全局唯一)
     *
     * @return
     */
    public static SingletonBuilder singletonBuilder() {
        return new SingletonBuilder();
    }

    /**
     * SingletonBuilder 模式
     *
     * @param <PK> Partition Key
     * @param <T>  Node类型
     */
    @NoArgsConstructor
    private static class SingletonBuilder<PK, T> {
        private BuildMode buildMode;
        private String zkPath;

        public enum BuildMode {
            BUILD_AND_WRITE_TO_ZK,
            BUILD_BY_READ_FROM_ZK
        }

        public SingletonBuilder buildMode(BuildMode buildMode) {
            this.buildMode = buildMode;

            return this;
        }

        public SingletonBuilder zkPath(String zkPath) {
            this.zkPath = zkPath;

            return this;
        }

        /**
         * 构建实例
         *
         * @return 实例
         */
        public synchronized FaceMetadataContext build() {
            // 校验
            validateAttr();

            METADATA_CONTEXT = new FaceMetadataContext(this.zkPath);

            return METADATA_CONTEXT;
        }

        /**
         * 检查属性(attributes)的有效性
         */
        private void validateAttr() {
            if (this.buildMode == null) {
                throw new RuntimeException("buildeMode is not set yet.");
            } else {
                // buildMode为write时, groupInfoMap不能为空
                if (this.buildMode == BuildMode.BUILD_AND_WRITE_TO_ZK) {
                    throw new RuntimeException("groupInfoMapToBuild is still empty, can not build. Please insert some data.");
                }
            }

            if (this.zkPath == null || this.zkPath.isEmpty()) {
                throw new RuntimeException("zkPath is not set yet.");
            }
        }


    }
}
