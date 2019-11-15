package com.ej.job.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ej.job.domain.JobLog;

@Mapper
public interface JobLogMapper{

    public int insert(JobLog jobLog);
    
    public int update(JobLog jobLog);
    
    public int deleteById(@Param("id")Long id);
    
    public JobLog selectById(@Param("id")Long id);
}
