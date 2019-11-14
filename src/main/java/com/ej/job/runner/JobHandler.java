package com.ej.job.runner;

import com.ej.job.constants.EJConstants;
import com.ej.job.dao.JobInfoMapper;
import com.ej.job.domain.JobInfo;
import com.ej.job.enums.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JobHandler implements Runnable {
    /**
     * 如果没有数据需要等待的时间毫秒数
     **/
    private static final Long NULL_DATA_WAIT = 2000L;

    /**
     * 执行数据最大等待时间毫秒数
     **/
    private static final Long EXECUTE_MAX_WAIT = 2000L;

    /**
     * 过期多久之后不执行的时间毫秒数
     **/
    private static final Long EXPIRE_NOT_EXECUTE_TIME = 5000L;

    private AtomicBoolean shutdown;
    private AtomicInteger begin;
    private AtomicInteger end;
    private JobInfoMapper jobInfoMapper;

    public JobHandler(int begin, int end, JobInfoMapper jobInfoMapper) {
        this.begin = new AtomicInteger(begin);
        this.end = new AtomicInteger(end);
        this.jobInfoMapper = jobInfoMapper;
        this.shutdown = new AtomicBoolean(Boolean.FALSE);
    }


    public void stop() {
        shutdown.set(Boolean.TRUE);
        begin.set(0);
        end.set(0);
    }

    @Override
    public void run() {
        while (!shutdown.get()) {
            List<JobInfo> jobInfoList = getJobInfoList();
            if (jobInfoList == null || jobInfoList.isEmpty()) {
                synchronized (this) {
                    try {
                        log.debug("没有可执行的数据,等待{}毫秒", NULL_DATA_WAIT);
                        this.wait(NULL_DATA_WAIT);
                    } catch (InterruptedException e) {
                        log.error("执行线程被中断", e);
                    } catch (Exception e) {
                        log.error("执行线程等待异常", e);
                    }
                }
            } else {
                for (JobInfo jobInfo : jobInfoList) {
                    long executeTime = jobInfo.getExecuteTime();
                    if (executeTime + EXPIRE_NOT_EXECUTE_TIME < System.currentTimeMillis()) {
                        //过期太久不执行
                        log.debug("任务已经过期超过{}毫秒,不予执行", EXPIRE_NOT_EXECUTE_TIME);
                        refresh(jobInfo);
                        continue;
                    } else if (executeTime <= System.currentTimeMillis()) {
                        //过期时间不长可以继续执行
                        log.debug("任务已经过期,但是未超过{}毫秒,继续执行", EXPIRE_NOT_EXECUTE_TIME);
                    } else if (executeTime > System.currentTimeMillis()) {
                        //还未到行时间需要等待,但是不能无限等待
                        long wait = Math.max(Math.min(executeTime - System.currentTimeMillis(), EXECUTE_MAX_WAIT), 0L);
                        if (wait > 0) {
                            synchronized (this) {
                                try {
                                    log.debug("还没有到执行时间,需要等待{}毫秒", wait);
                                    this.wait(wait);
                                } catch (InterruptedException e) {
                                    log.error("执行线程被中断", e);
                                } catch (Exception e) {
                                    log.error("执行线程等待异常", e);
                                }
                            }
                            if (executeTime > System.currentTimeMillis()) {
                                log.debug("唤醒之后依然没有到执行时间");
                                continue;
                            }
                        }
                    }
                    if (shutdown.get()) {
                        log.debug("执行区间已被刷新或者被清空,需要中断执行");
                        continue;
                    } else {
                        //doing
                        log.info("数据被执行执行 >>> {}", jobInfo.getId());
                        refresh(jobInfo);
                    }
                }
            }
        }
        log.info("任务区间[{},{}]任务停止", begin.intValue(), end.intValue());
    }

    protected List<JobInfo> getJobInfoList() {
        if (shutdown.get() || begin.intValue() < 1 || end.intValue() < 1) {
            log.debug("没有可执行区间");
            return null;
        }
        return jobInfoMapper.selectRecentExecute(begin.get(), end.get(), System.currentTimeMillis() + NULL_DATA_WAIT, JobStatus.Y.name(), EJConstants.HANDLER_QUERY_PAGE_SIZE);
    }

    protected void refresh(JobInfo jobInfo) {
        try {
            jobInfo.setExecuteTime(new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date()).getTime());
            jobInfoMapper.update(jobInfo);
        } catch (ParseException e) {
            log.error("Cron表达式[{}]解析异常", jobInfo.getJobCron(), e);
        } catch (Exception e) {
            log.error("更新下一次执行时间异常", e);
        }
    }
}
