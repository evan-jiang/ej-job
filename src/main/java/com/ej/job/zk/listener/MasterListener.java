package com.ej.job.zk.listener;

import com.ej.job.constants.EJConstants;
import com.ej.job.zk.AbstractChildrenListener;
import com.ej.job.zk.option.MasterOptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

@Slf4j
public class MasterListener extends AbstractChildrenListener {
    MasterOptionService masterOptionService;

    public MasterListener() {
        super();
    }

    public MasterListener(CuratorFramework client) {
        super(client);
    }


    public MasterListener(CuratorFramework client, MasterOptionService masterOptionService) {
        super(client);
        this.masterOptionService = masterOptionService;
    }

    @Override
    protected String zkPath() {
        return EJConstants.ZK_MASTER_BASE;
    }

    @Override
    protected void add(CuratorFramework client, String path) {
        if (!EJConstants.ZK_MASTER_PATH.equals(path)) {
            return;
        }
        try {
            String value = new String(client.getData().forPath(path));
            if (EJConstants.NODE_NAME.equals(value)) {
                log.info("本节点[{}]已晋升为Master角色", value);
                EJConstants.IS_MASTER.set(Boolean.TRUE);
                masterOptionService.refresh();
            } else {
                log.info("节点[{}]已晋升为Master角色!", value);
                EJConstants.IS_MASTER.set(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void remove(CuratorFramework client, String path) {
        if (!EJConstants.ZK_MASTER_PATH.equals(path)) {
            return;
        }
        try {
            log.info("Master节点已下线,当前节点[{}]开始抢占Master...", EJConstants.NODE_NAME);
            masterOptionService.holdMaster();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void update(CuratorFramework client, String path) {

    }
}
