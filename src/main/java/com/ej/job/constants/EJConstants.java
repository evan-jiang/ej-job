package com.ej.job.constants;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EJConstants {

    /**
     * 执行区间总数
     **/
    public static final int PARTITION_TOTAL = 1024;

    /**
     * 节点名称
     **/
    public static final String NODE_NAME = UUID.randomUUID().toString().replace("-", "");

    /**
     * 是否是主节点
     **/
    public static final AtomicBoolean IS_MASTER = new AtomicBoolean(Boolean.FALSE);

    public static final Integer HANDLER_QUERY_PAGE_SIZE = 20;


    public static final String SLASH = "/";
    /**
     * 项目zk根目录
     **/
    public static final String ZK_ROOT_PATH = SLASH + "ej_job";

    /**
     * master目录
     **/
    public static final String ZK_MASTER_BASE = ZK_ROOT_PATH + SLASH + "master";
    /**
     * master节点
     **/
    public static final String ZK_MASTER_PATH = ZK_MASTER_BASE + SLASH + "key";

    /**
     * node目录
     **/
    public static final String ZK_NODE_BASE = ZK_ROOT_PATH + SLASH + "node";

    /**
     * node执行区间值
     **/
    public static final String SPLIT_STR = ",";
    public static final String ZK_PARTITION_VALUE_TEMPLATE = "%06d%s%06d";


    /**
     * 抢占master时分布式锁zk节点
     **/
    private static final String ZK_LOCK_BASE = ZK_ROOT_PATH + SLASH + "lock";
    public static final String ZK_LOCK_MASTER = ZK_LOCK_BASE + SLASH + "master";

}
