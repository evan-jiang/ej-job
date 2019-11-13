package com.ej.job.dao;

import com.ej.job.domain.JobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobInfoMapper{

    public int insert(JobInfo jobInfo);
    
    public int update(JobInfo jobInfo);
    
    public JobInfo selectByPartition(@Param("begin")Integer begin,@Param("end")Integer end);
}
