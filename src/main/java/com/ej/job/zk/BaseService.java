package com.ej.job.zk;

import org.apache.curator.framework.CuratorFramework;

import javax.annotation.Resource;

public class BaseService {

    @Resource
    protected CuratorFramework client;

    public BaseService() {
    }

    public BaseService(CuratorFramework client) {
        this.client = client;
    }
}
