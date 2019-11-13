package com.ej.job.runner;

import com.ej.job.constants.EJConstants;
import com.ej.job.dao.JobInfoMapper;
import com.ej.job.domain.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

@Slf4j
public class JobExecutor implements Runnable{

    private JobInfoMapper jobInfoMapper;

    public JobExecutor(JobInfoMapper jobInfoMapper) {
        this.jobInfoMapper = jobInfoMapper;
    }

    /**
     * 如果没有数据需要等待的时间毫秒数
     **/
    private static final Long NULL_DATA_WAIT = 2000L;

    /**
     * 执行数据最大等待时间毫秒数
     **/
    private static final Long EXECUTE_MAX_WAIT = 2000L;

    /**
     * 接近执行时间前不等待是时间毫秒数
     **/
    private static final Long CLOSE_TO_THE_NUT_WAIT = 5L;

    /**
     * 过期多久之后不执行的时间毫秒数
     **/
    private static final Long EXPIRE_NOT_EXECUTE_TIME = 5000L;

    public void run() {
        while (EJConstants.CONSUMER_POWER.get()) {
            JobInfo jobInfo = getJobInfo();
            if (jobInfo == null) {
                synchronized (EJConstants.JOB_LOCK) {
                    try {
                        log.debug("没有可执行的数据,等待{}毫秒", NULL_DATA_WAIT);
                        EJConstants.JOB_LOCK.wait(NULL_DATA_WAIT);
                    } catch (Exception e) {
                        log.error("执行线程等待异常", e);
                    }
                }
            } else {
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
                        synchronized (EJConstants.JOB_LOCK) {
                            try {
                                log.debug("还没有到执行时间,需要等待{}毫秒", wait);
                                EJConstants.JOB_LOCK.wait(wait);
                                if (executeTime > System.currentTimeMillis()) {
                                    log.debug("唤醒之后依然没有到执行时间");
                                    continue;
                                }
                            } catch (Exception e) {
                                log.error("执行线程等待异常", e);
                            }
                        }
                    }
                }
                if (EJConstants.SUSPENDED.get()) {
                    log.debug("执行区间已被刷新或者被清空,需要中断执行");
                    continue;
                } else {
                    //doing
                    log.info("数据被执行执行 >>> {}" , jobInfo.toString());
                    refresh(jobInfo);
                }
            }
        }
    }

    protected JobInfo getJobInfo() {
        if (EJConstants.SUSPENDED.get() || EJConstants.BEGIN.intValue() < 1 || EJConstants.END.intValue() < 1) {
            log.debug("没有可执行区间");
            return null;
        }
        return jobInfoMapper.selectByPartition(EJConstants.BEGIN.intValue(),EJConstants.END.intValue());
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
