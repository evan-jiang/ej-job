package com.ej.job.init;

import com.ej.job.constants.EJConstants;
import com.ej.job.runner.JobManager;
import com.ej.job.dao.JobInfoMapper;
import com.ej.job.dao.JobLogMapper;
import com.ej.job.zk.listener.MasterListener;
import com.ej.job.zk.listener.NodeListener;
import com.ej.job.zk.option.MasterOptionService;
import com.ej.job.zk.option.NodeOptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class InitBean {

    @Value("${ej.job.zk.host}")
    private String host;

    @Bean
    public JobManager jobManager(JobInfoMapper jobInfoMapper, JobLogMapper jobLogMapper) {
        return new JobManager(jobInfoMapper, jobLogMapper);
    }

    @Bean
    public CuratorFramework client() {
        RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(host, RETRY_POLICY);
        client.start();
        return client;
    }

    @Bean
    public MasterOptionService masterOptionService(CuratorFramework client) {
        return new MasterOptionService(client);
    }

    @Bean
    public NodeOptionService nodeOptionService(CuratorFramework client) {
        return new NodeOptionService(client);
    }

    @Bean
    public MasterListener masterListener(CuratorFramework client, MasterOptionService masterOptionService) {
        return new MasterListener(client, masterOptionService);
    }

    @Bean
    public NodeListener nodeListener(CuratorFramework client, MasterOptionService masterOptionService, JobManager jobManager) {
        return new NodeListener(client, masterOptionService, jobManager);
    }

    @Bean
    public Object object(final CuratorFramework client, MasterOptionService masterOptionService,
                         NodeOptionService nodeOptionService,
                         MasterListener masterListener,
                         NodeListener nodeListener) throws Exception {
        initNode(client);
        final PathChildrenCache ml = masterListener.listen();
        masterOptionService.holdMaster();
        final PathChildrenCache nl = nodeListener.listen();
        nodeOptionService.register();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("应用被关闭...");
                try {
                    ml.close();
                    nl.close();
                    client.close();
                } catch (Exception e) {
                    log.error("关闭zookeeper资源异常", e);
                }
            }
        }));
        return null;
    }

    private void initNode(final CuratorFramework client){
        boolean result = Boolean.FALSE;
        InterProcessMutex lock = new InterProcessMutex(client, EJConstants.ZK_LOCK_MASTER);
        try {
            lock.acquire();
            if(client.checkExists().forPath(EJConstants.ZK_ROOT_PATH) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(EJConstants.ZK_ROOT_PATH);
            }
            if(client.checkExists().forPath(EJConstants.ZK_MASTER_BASE) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(EJConstants.ZK_MASTER_BASE);
            }
            if(client.checkExists().forPath(EJConstants.ZK_NODE_BASE) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(EJConstants.ZK_NODE_BASE);
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
    }
}
