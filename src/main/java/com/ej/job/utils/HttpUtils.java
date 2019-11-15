package com.ej.job.utils;

import com.ej.job.domain.JobInfo;
import com.ej.job.enums.HttpStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtils {

    public static String doRequest(JobInfo jobInfo){
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>{}",jobInfo.getId());
        return HttpStatus._200.getCode();
    }
}
