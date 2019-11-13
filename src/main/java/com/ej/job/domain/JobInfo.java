package com.ej.job.domain;

import lombok.Data;

@Data
public class JobInfo extends BaseDomain {


    /**
     * 失败告警人邮箱地址列表
     **/
    private String alertEmail;
    /**
     * 下次调度时间
     **/
    private Long executeTime;
    /**
     * 任务CRON表达式
     **/
    private String jobCron;
    /**
     * 任务名称
     **/
    private String jobName;
    /**
     * 任务状态
     **/
    private String jobStatus;
    /**
     * 请求方式
     **/
    private String reqMethod;
    /**
     * 请求参数
     **/
    private String reqParams;
    /**
     * 请求地址
     **/
    private String reqUrl;
    /**
     * 任务分区
     **/
    private Integer jobPartition;

}
