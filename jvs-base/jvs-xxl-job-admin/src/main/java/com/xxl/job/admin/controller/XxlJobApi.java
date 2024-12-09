package com.xxl.job.admin.controller;

import cn.bctools.common.utils.ObjectNull;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dto.R;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import groovy.util.logging.Slf4j;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping
public class XxlJobApi {
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobService xxlJobService;

    /**
     * 保存或更新这个设计任务
     *
     * @param jobInfo
     * @param accessToken
     * @param jobName
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/job/api/saveOrUpdate")
    public R saveOrUpdate(@RequestBody XxlJobInfo jobInfo, @RequestHeader(value = "XXL-JOB-ACCESS-TOKEN", required = false) String accessToken, @RequestHeader("jobName") String jobName) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() == null
                || XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() == 0
                || !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken)) {
            return new R().setMsg("The access token is wrong.");
        }
        XxlJobGroup xxlJobGroup = new XxlJobGroup();
        List<XxlJobGroup> all = xxlJobGroupDao.findAll();
        Optional<XxlJobGroup> first = all.stream().filter(e -> e.getAppname().equals(jobName)).findFirst();
        if (first.isPresent()) {
            xxlJobGroup = first.get();
        } else {
            xxlJobGroup.setAddressType(0);
            xxlJobGroup.setAppname(jobName);
            xxlJobGroup.setTitle(jobName);
            xxlJobGroupDao.save(xxlJobGroup);
        }
        jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
        jobInfo.setScheduleType("CRON");
        jobInfo.setJobGroup(xxlJobGroup.getId());
        jobInfo.setGlueType("BEAN");
        jobInfo.setChildJobId("");
        jobInfo.setGlueSource("");
        jobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        jobInfo.setExecutorRouteStrategy("FIRST");
        jobInfo.setMisfireStrategy("DO_NOTHING");
        ReturnT<String> t;

        //判断是否存在，如果不存在，则直接创建
        XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(jobInfo.getId());
        if (exists_jobInfo == null) {
            xxlJobService.add(jobInfo);
        }
        if (jobInfo.getId() != 0) {
            t = xxlJobService.update(jobInfo);
            if (t.getCode() != 200) {
                return new R().setCode(500).setMsg(t.getMsg());
            }
        } else {
            t = xxlJobService.add(jobInfo);
            if (t.getCode() != 200) {
                return new R().setCode(500).setMsg(t.getMsg());
            }
            jobInfo.setId(Integer.parseInt(t.getContent()));
        }
        xxlJobService.stop(jobInfo.getId());
        if (jobInfo.getTriggerStatus() == 1) {
            xxlJobService.start(jobInfo.getId());
        }
        return new R().setData(jobInfo.getId());
    }

    @DeleteMapping("/job/api/{id}")
    @SneakyThrows
    public R<Object> delete(@PathVariable Integer id, @RequestHeader(value = "XXL-JOB-ACCESS-TOKEN", required = false) String accessToken) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() == null
                || XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() == 0
                || !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken)) {
            return new R().setMsg("The access token is wrong.");
        }
        //验证
        ReturnT<String> returnT = xxlJobService.remove(id);
        if (returnT.getCode() == 200) {
            return new R();
        }
        return new R().setMsg(returnT.getMsg());
    }


    @PostMapping("/job/api/stop/{id}")
    @SneakyThrows
    public R<Object> stop(@PathVariable Integer id, @RequestHeader(value = "XXL-JOB-ACCESS-TOKEN", required = false) String accessToken) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() == null
                || XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() == 0
                || !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken)) {
            return new R().setMsg("The access token is wrong.");
        }
        //验证
        ReturnT<String> returnT = xxlJobService.stop(id);
        if (returnT.getCode() == 200) {
            return new R();
        }
        return new R().setMsg(returnT.getMsg());
    }

    @PostMapping("/job/api/start/{id}")
    @SneakyThrows
    public R<Object> start(@PathVariable Integer id, @RequestHeader(value = "XXL-JOB-ACCESS-TOKEN", required = false) String accessToken) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() == null
                || XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() == 0
                || !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken)) {
            return new R().setMsg("The access token is wrong.");
        }
        //验证
        ReturnT<String> returnT = xxlJobService.start(id);
        if (returnT.getCode() == 200) {
            return new R();
        }
        return new R().setMsg(returnT.getMsg());
    }

    @PostMapping("/job/api/save")
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> save(@RequestBody XxlJobInfo xxlJobInfoDto, @RequestHeader(value = "XXL-JOB-ACCESS-TOKEN", required = false) String accessToken, @RequestHeader("jobName") String jobName) {
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() == null
                || XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() == 0
                || !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken)) {
            return new R().setMsg("The access token is wrong.");
        }
        XxlJobGroup xxlJobGroup = new XxlJobGroup();
        List<XxlJobGroup> all = xxlJobGroupDao.findAll();
        Optional<XxlJobGroup> first = all.stream().filter(e -> e.getAppname().equals(jobName)).findFirst();
        if (first.isPresent()) {
            xxlJobGroup = first.get();
        } else {
            xxlJobGroup.setAddressType(0);
            xxlJobGroup.setAppname(jobName);
            xxlJobGroup.setTitle(jobName);
            xxlJobGroupDao.save(xxlJobGroup);
        }
//        Optional<XxlJobGroup> any = xxlJobGroupDao.findAll().stream().filter(e -> e.getAppname().equals(xxlJobInfoDto.getExecutorHandler())).findAny();
//        boolean present = any.isPresent();
//        if (!present) {
//            return new R().setCode(500).setMsg("没有找到执行器");
//        }
//        XxlJobGroup xxlJobGroup = any.get();
        xxlJobInfoDto.setExecutorHandler(xxlJobInfoDto.getExecutorHandler());
        xxlJobInfoDto.setScheduleType("CRON");
        xxlJobInfoDto.setJobGroup(xxlJobGroup.getId());
        xxlJobInfoDto.setGlueType("BEAN");
        xxlJobInfoDto.setChildJobId("");
        xxlJobInfoDto.setGlueSource("");
        xxlJobInfoDto.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfoDto.setExecutorRouteStrategy("FIRST");
        xxlJobInfoDto.setMisfireStrategy("DO_NOTHING");

        ReturnT<String> t = xxlJobService.add(xxlJobInfoDto);
        if (t.getCode() == 200) {
            Integer id = Integer.valueOf(t.getContent());
            ReturnT<String> start = xxlJobService.start(id);
            if (start.getCode() == 200) {
                return new R().setData(Integer.valueOf(id));
            }
            return new R().setCode(500).setMsg(start.getMsg());
        } else {
            return new R().setCode(500).setMsg(t.getMsg());
        }
    }

}
