package com.ej.job.zk.listener;

import com.ej.job.constants.EJConstants;
import com.ej.job.container.EJNodeInfo;
import com.ej.job.zk.AbstractChildrenListener;
import com.ej.job.zk.option.MasterOptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.annotation.Resource;

@Slf4j
public class NodeListener extends AbstractChildrenListener {
    @Resource
    MasterOptionService masterOptionService;

    public NodeListener() {
        super();
    }

    public NodeListener(CuratorFramework client) {
        super(client);
    }


    public NodeListener(CuratorFramework client,MasterOptionService masterOptionService) {
        super(client);
        this.masterOptionService = masterOptionService;
    }

    @Override
    protected String zkPath() {
        return EJConstants.ZK_NODE_BASE;
    }

    @Override
    protected void add(CuratorFramework client, String path) {
        try {
            String nodeName = getNodeName(path);
            EJNodeInfo.intervalName = null;
            log.info("节点[{}]已加入到集群中,需要中断当前任务线程", nodeName);
            if (EJNodeInfo.master) {
                masterOptionService.refresh();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void remove(CuratorFramework client, String path) {
        try {
            String nodeName = getNodeName(path);
            EJNodeInfo.intervalName = null;
            log.info("节点[{}]已从集群中移除,需要中断当前任务线程", nodeName);
            if (EJNodeInfo.master) {
                masterOptionService.refresh();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void update(CuratorFramework client, String path) {
        try {
            String nodeName = getNodeName(path);
            if (!EJNodeInfo.nodeName.equals(nodeName)) {
                return;
            }
            EJNodeInfo.intervalName = new String(client.getData().forPath(path));
            log.info("当前节点[{}]已被重新分配任务区间:[{}],需要唤醒当前任务线程", nodeName, EJNodeInfo.intervalName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getNodeName(String path) {
        return path.substring(path.lastIndexOf(EJConstants.SLASH) + 1);
    }
}
