package com.ej.job.constants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EJConstants {



    public static final String SLASH = "/";
    private static final String ZK_ROOT_PATH = SLASH + "ej_job";

    /**master**/
    public static final String ZK_MASTER_BASE = ZK_ROOT_PATH + SLASH + "master";
    public static final String ZK_MASTER_PATH = ZK_MASTER_BASE + SLASH + "key";

    /**node**/
    public static final String ZK_NODE_BASE = ZK_ROOT_PATH + SLASH + "node";

    public static final String SPLIT_STR = ",";
    public static final String ZK_INTERVAL_VALUE_TEMPLATE = "%06d%s%06d";


    /**分布式锁**/
    private static final String ZK_LOCK_BASE = ZK_ROOT_PATH + SLASH + "lock";
    public static final String ZK_LOCK_MASTER = ZK_LOCK_BASE + SLASH + "master";




    public static final AtomicBoolean CONSUMER_POWER = new AtomicBoolean(Boolean.TRUE);
    public static final AtomicBoolean SUSPENDED = new AtomicBoolean(Boolean.FALSE);
    public static final AtomicInteger BEGIN = new AtomicInteger(0);
    public static final AtomicInteger END = new AtomicInteger(0);
    public static final Object JOB_LOCK = new Object();
}
