package com.ej.job.web;

import com.ej.job.constants.EJConstants;
import com.ej.job.dao.JobInfoMapper;
import com.ej.job.domain.JobInfo;
import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

@Controller
@RequestMapping("job_info")
public class JobInfoWeb {

    @Resource
    private JobInfoMapper jobInfoMapper;

    @RequestMapping("add")
    @ResponseBody
    public Object add() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobName("test");
        jobInfo.setAlertEmail("jiangyongsheng@zhongan.com");
        jobInfo.setJobCron("0/5 * * * * ? *");
        jobInfo.setJobPartition(new Random().nextInt(EJConstants.PARTITION_TOTAL) + 1);
        jobInfo.setJobStatus("Y");
        jobInfo.setReqMethod("GET");
        jobInfo.setReqUrl("http://www.tdpark.com");
        jobInfo.setCreated(new Date());
        jobInfo.setModified(new Date());
        jobInfo.setCreator("S");
        jobInfo.setModifier("S");
        try {
            jobInfo.setExecuteTime(new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jobInfoMapper.insert(jobInfo);
        return jobInfo;
    }
}
