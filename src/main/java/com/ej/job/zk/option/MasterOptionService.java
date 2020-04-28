package com.ej.job.zk.option;

import com.ej.job.constants.EJConstants;
import com.ej.job.zk.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Slf4j
public class MasterOptionService extends BaseService {
    public MasterOptionService() {
        super();
    }

    public MasterOptionService(CuratorFramework client) {
        super(client);
    }

    public boolean holdMaster() {
        boolean result = Boolean.FALSE;
        InterProcessMutex lock = new InterProcessMutex(client, EJConstants.ZK_LOCK_MASTER);
        try {
            lock.acquire();
            Stat stat = client.checkExists().forPath(EJConstants.ZK_MASTER_PATH);
            if (stat == null) {
                client.create().withMode(CreateMode.EPHEMERAL).forPath(EJConstants.ZK_MASTER_PATH, EJConstants.NODE_NAME.getBytes());
                result = Boolean.TRUE;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public void refresh() throws Exception {
        log.info("开始分配任务区间...");
        Stat stat = client.checkExists().forPath(EJConstants.ZK_NODE_BASE);
        if(stat == null){
            log.info("还没有任何节点,暂时不分配任务区间");
            return;
        }
        List<String> list = client.getChildren().forPath(EJConstants.ZK_NODE_BASE);
        if (list == null || list.isEmpty()) {
            return;
        }
        int partitionPage = EJConstants.PARTITION_TOTAL / list.size();
        int partitionMode = EJConstants.PARTITION_TOTAL % list.size();
        int start = 0;
        for (int idx = 0; idx < list.size(); idx++) {
            String path = EJConstants.ZK_NODE_BASE + EJConstants.SLASH + list.get(idx);
            int ip = partitionPage;
            if (idx < partitionMode) {
                ip++;
            }
            String value = String.format(EJConstants.ZK_PARTITION_VALUE_TEMPLATE, start + 1, EJConstants.SPLIT_STR, start + ip);
            client.setData().forPath(path, value.getBytes());
            log.info("节点[{}]任务区间为:[{}]", list.get(idx), value);
            start += ip;
        }
        log.info("任务区间分配完成!");
    }
}
