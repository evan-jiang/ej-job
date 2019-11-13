package com.ej.job.init;

import com.ej.job.dao.JobInfoMapper;
import com.ej.job.runner.JobExecutor;
import com.ej.job.zk.listener.MasterListener;
import com.ej.job.zk.listener.NodeListener;
import com.ej.job.zk.option.MasterOptionService;
import com.ej.job.zk.option.NodeOptionService;
import jdk.nashorn.internal.runtime.regexp.joni.exception.JOniException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class InitBean {

    @Value("${ej.job.zk.host}")
    private String host;

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
    public NodeListener nodeListener(CuratorFramework client, MasterOptionService masterOptionService) {
        return new NodeListener(client, masterOptionService);
    }

    @Bean
    public JobExecutor jobExecutor(JobInfoMapper jobInfoMapper){
        return new JobExecutor(jobInfoMapper);
    }

    @Bean
    public Object object(final CuratorFramework client, MasterOptionService masterOptionService,
                         NodeOptionService nodeOptionService,
                         MasterListener masterListener,
                         NodeListener nodeListener, JobExecutor jobExecutor) throws Exception {
        new Thread(jobExecutor).start();
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
                    log.error("关闭zookeeper资源异常",e);
                }
            }
        }));
        return null;
    }
}
