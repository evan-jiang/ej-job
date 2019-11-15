package com.ej.job.runner;

import com.ej.job.dao.JobInfoMapper;
import com.ej.job.dao.JobLogMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JobManager {
    private final static Long HANDLER_STOP_SLEEP = 2000L;
    private final static Integer NODE_HANDLER_THREAD_TOTAL = 8;

    private JobInfoMapper jobInfoMapper;
    private JobLogMapper jobLogMapper;
    private List<JobHandler> handlers;
    private List<Thread> threads;

    public JobManager(JobInfoMapper jobInfoMapper,JobLogMapper jobLogMapper) {
        this.jobInfoMapper = jobInfoMapper;
        this.jobLogMapper = jobLogMapper;
        this.handlers = new ArrayList<>();
        this.threads = new ArrayList<>();
    }

    public synchronized void stop() {
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        for (JobHandler jobHandler : handlers) {
            jobHandler.stop();
        }
        try {
            Thread.sleep(HANDLER_STOP_SLEEP);
        } catch (InterruptedException e) {
            log.error("终止执行线程等待异常", e);
        }
        while (true) {
            boolean allStop = Boolean.TRUE;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    allStop = Boolean.FALSE;
                    break;
                }
            }
            if (allStop) {
                break;
            } else {
                try {
                    Thread.sleep(HANDLER_STOP_SLEEP);
                } catch (InterruptedException e) {
                    log.error("终止执行线程等待异常", e);
                }
            }
        }
        handlers.clear();
        threads.clear();
    }

    public synchronized void restart(Integer begin, Integer end) {
        stop();
        if (begin == null || begin.intValue() < 1) {
            log.error("执行分区起点[{}]非法", begin);
            return;
        }
        if (end == null || end.intValue() < 1) {
            log.error("执行分区终点[{}]非法", end);
            return;
        }
        if (begin.intValue() > end.intValue()) {
            log.error("执行分区[{},{}]非法", begin, end);
            return;
        }
        int nodePartitionTotal = end.intValue() - begin.intValue() + 1;
        int partitionPage = nodePartitionTotal / NODE_HANDLER_THREAD_TOTAL;
        int partitionMode = nodePartitionTotal % NODE_HANDLER_THREAD_TOTAL;
        int threadNum = NODE_HANDLER_THREAD_TOTAL;
        int start = begin.intValue() - 1;
        if (partitionPage == 0) {
            threadNum = partitionMode;
        }
        for (int idx = 0; idx < threadNum; idx++) {
            int tidx = partitionPage;
            if (idx < partitionMode) {
                tidx++;
            }
            JobHandler jobHandler = new JobHandler(start + 1, start + tidx, jobInfoMapper, jobLogMapper);
            Thread thread = new Thread(jobHandler);
            handlers.add(jobHandler);
            threads.add(thread);
            thread.start();
            start += tidx;
        }
    }
}
