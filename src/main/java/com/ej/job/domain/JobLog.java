package com.ej.job.domain;

import lombok.Data;

@Data
public class JobLog{

    /** 主键ID **/
    private Long id;
    /** 执行开始时间 **/
    private Long beginTime;
    /** 执行结束时间 **/
    private Long endTime;
    /** 执行状态 **/
    private String executeStatus;
    /** 任务ID **/
    private Long jobId;
    /** 任务名称 **/
    private String jobName;
    /** 请求方式 **/
    private String reqMethod;
    /** 请求参数 **/
    private String reqParams;
    /** 请求地址 **/
    private String reqUrl;
    /** 执行结果信息 **/
    private String respMsg;

    
}
