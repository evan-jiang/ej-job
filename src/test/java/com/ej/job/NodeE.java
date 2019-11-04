package com.ej.job;

import com.ej.job.zk.listener.MasterListener;
import com.ej.job.zk.listener.NodeListener;
import com.ej.job.zk.option.MasterOptionService;
import com.ej.job.zk.option.NodeOptionService;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeE {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String host = "127.0.0.1:2181";
                RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);
                CuratorFramework client = CuratorFrameworkFactory.newClient(host, RETRY_POLICY);
                client.start();
                MasterOptionService masterOptionService = new MasterOptionService(client);
                NodeOptionService nodeOptionService = new NodeOptionService(client);
                MasterListener masterListener = new MasterListener(client, masterOptionService);
                NodeListener nodeListener = new NodeListener(client, masterOptionService);
                try {
                    masterListener.listen();
                    masterOptionService.holdMaster();
                    nodeListener.listen();
                    nodeOptionService.register();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
