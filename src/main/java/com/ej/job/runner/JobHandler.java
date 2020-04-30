package com.ej.job.runner;

import com.alibaba.fastjson.JSON;
import com.ej.job.constants.EJConstants;
import com.ej.job.dao.JobInfoMapper;
import com.ej.job.dao.JobLogMapper;
import com.ej.job.domain.JobInfo;
import com.ej.job.domain.JobLog;
import com.ej.job.enums.HttpStatus;
import com.ej.job.enums.JobStatus;
import com.ej.job.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

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

    private volatile boolean shutdown;
    private volatile int begin;
    private volatile int end;
    private JobInfoMapper jobInfoMapper;
    private JobLogMapper jobLogMapper;

    public JobHandler(int begin, int end, JobInfoMapper jobInfoMapper,JobLogMapper jobLogMapper) {
        this.shutdown = Boolean.FALSE;
        this.begin = begin;
        this.end = end;
        this.jobInfoMapper = jobInfoMapper;
        this.jobLogMapper = jobLogMapper;
    }


    public void stop() {
        shutdown = Boolean.TRUE;
    }

    @Override
    public void run() {
        while (!shutdown) {
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
                    if (shutdown) {
                        log.debug("执行区间已被刷新或者被清空,需要中断执行");
                        return;
                    } else {
                        doAndLog(jobInfo);
                    }
                }
            }
        }
        log.info("任务区间[{},{}]任务停止", begin, end);
    }

    private void doAndLog(JobInfo jobInfo) {
        String result = null;
        Long begin = System.currentTimeMillis();
        Long end = 0L;
        try {
            result = HttpUtils.doRequest(jobInfo);
            end = System.currentTimeMillis();
        } catch (Exception e) {
            end = System.currentTimeMillis();
            log.error("任务执行异常，任务数据:{}", JSON.toJSONString(jobInfo));
            result = Exception.class.getSimpleName();
        } finally {
            saveLog(jobInfo,begin,end,result);
            refresh(jobInfo);
        }
    }

    private List<JobInfo> getJobInfoList() {
        if (begin < 1 || end < 1) {
            log.debug("没有可执行区间");
            return null;
        }
        if(shutdown){
            log.debug("执行任务已被强制停止");
            return null;
        }
        return jobInfoMapper.selectRecentExecute(begin, end, System.currentTimeMillis() + NULL_DATA_WAIT, JobStatus.Y.name(), EJConstants.HANDLER_QUERY_PAGE_SIZE);
    }

    private void refresh(JobInfo jobInfo) {
        try {
            jobInfo.setExecuteTime(new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date()).getTime());
            jobInfoMapper.update(jobInfo);
        } catch (ParseException e) {
            log.error("Cron表达式[{}]解析异常", jobInfo.getJobCron(), e);
        } catch (Exception e) {
            log.error("更新下一次执行时间异常", e);
        }
    }

    private void saveLog(JobInfo jobInfo,long begin,long end,String result){
        try {
            JobLog jobLog = new JobLog();
            jobLog.setJobId(jobInfo.getId());
            jobLog.setJobName(jobInfo.getJobName());
            jobLog.setReqMethod(jobInfo.getReqMethod());
            jobLog.setReqParams(jobInfo.getReqParams());
            jobLog.setReqUrl(jobInfo.getReqUrl());
            jobLog.setBeginTime(begin);
            jobLog.setEndTime(end);
            HttpStatus status = HttpStatus.getEnum(result);
            jobLog.setRespMsg(result);
            jobLog.setExecuteStatus(status.getStatus());
            jobLogMapper.insert(jobLog);
        } catch (Exception e) {
            log.error("报错执行日志异常", e);
        }
    }
}
