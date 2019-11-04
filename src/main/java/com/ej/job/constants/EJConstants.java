package com.ej.job.constants;

public class EJConstants {



    public static final String SLASH = "/";
    private static final String ZK_ROOT_PATH = SLASH + "ej_job";

    /**master**/
    public static final String ZK_MASTER_BASE = ZK_ROOT_PATH + SLASH + "master";
    public static final String ZK_MASTER_PATH = ZK_MASTER_BASE + SLASH + "key";

    /**node**/
    public static final String ZK_NODE_BASE = ZK_ROOT_PATH + SLASH + "node";


    public static final String ZK_INTERVAL_VALUE_TEMPLATE = "%06d,%06d";


    /**分布式锁**/
    private static final String ZK_LOCK_BASE = ZK_ROOT_PATH + SLASH + "lock";
    public static final String ZK_LOCK_MASTER = ZK_LOCK_BASE + SLASH + "master";



}
