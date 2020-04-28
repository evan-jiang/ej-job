package com.ej.job.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

public abstract class AbstractChildrenListener extends BaseService {

    public AbstractChildrenListener() {
        super();
    }

    public AbstractChildrenListener(CuratorFramework client) {
        super(client);
    }

    protected abstract String zkPath();

    public PathChildrenCache listen() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(this.client, this.zkPath(), false);
        cache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    add(client, event.getData().getPath());
                    break;
                case CHILD_UPDATED:
                    update(client, event.getData().getPath());
                    break;
                case CHILD_REMOVED:
                    remove(client, event.getData().getPath());
                    break;
                default:
                    break;
            }
        });
        cache.start();
        return cache;
    }

    protected abstract void add(CuratorFramework client, String path);

    protected abstract void remove(CuratorFramework client, String path);

    protected abstract void update(CuratorFramework client, String path);
}
