package com.ej.job.zk.option;

import com.ej.job.constants.EJConstants;
import com.ej.job.zk.BaseService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

public class NodeOptionService extends BaseService {

    public NodeOptionService() {
        super();
    }

    public NodeOptionService(CuratorFramework client) {
        super(client);
    }

    public void register() {
        try {
            this.client.create().withMode(CreateMode.EPHEMERAL).forPath(EJConstants.ZK_NODE_BASE + EJConstants.SLASH + EJConstants.NODE_NAME, EJConstants.NODE_NAME.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
