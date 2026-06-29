package cn.bctools.xxl.job.api;

import cn.bctools.common.utils.R;
import io.swagger.models.auth.In;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * The interface Xxl admin api.
 *
 * @author guojing
 */
@FeignClient(name = "jvs-xxl-job-admin", path = "/xxl-job-admin")
public interface XxlAdminApi {

    /**
     * 保存或新增任务
     * 创建的任务，永远不会被删除，建议使用名称，为区分应用名，逻辑名
     *
     * @param xxlJobInfoDto the xxl job info dto
     * @param token         the token
     * @param jobName       the job name
     * @return r
     * @throws Exception the exception
     * @author: xb
     */
    @PostMapping("/job/api/saveOrUpdate")
    R saveOrUpdate(@RequestBody XxlJobInfoDto xxlJobInfoDto, @RequestHeader("XXL-JOB-ACCESS-TOKEN") String token, @RequestHeader("jobName") String jobName) throws Exception;


    /**
     * 删除一个定时任务
     *
     * @param id    使用定时任务的ID值
     * @param token the token
     * @return the r
     * @throws Exception 删除一个定时任务失败
     * @author: xb  建议使用saveOrUpdate 默认不再删除
     */
    @Deprecated
    @DeleteMapping("/job/api/{id}")
    R<Boolean> delete(@PathVariable("id") int id, @RequestHeader("XXL-JOB-ACCESS-TOKEN") String token) throws Exception;


    /**
     * 停止一个定时任务
     *
     * @param id    使用定时任务的ID值
     * @param token the token
     * @return the r
     * @throws Exception 停止一个定时任务失败                   建议使用saveOrUpdate
     */
    @Deprecated
    @PostMapping("/job/api/stop/{id}")
    R stop(@PathVariable("id") int id, @RequestHeader("XXL-JOB-ACCESS-TOKEN") String token) throws Exception;


    /**
     * 开启一个定时任务
     *
     * @param id    使用定时任务的ID值
     * @param token the token
     * @return the r
     * @throws Exception 开启一个定时任务失败                   建议使用saveOrUpdate
     */
    @Deprecated
    @PostMapping("/job/api/start/{id}")
    R<Boolean> start(@PathVariable("id") int id, @RequestHeader("XXL-JOB-ACCESS-TOKEN") String token) throws Exception;

    /**
     * 生成一个新的定时任务
     * 或直接更新一个新的定时任务，逻辑执行器那边添加或修改时，可直接调用此方法即可完成定时任务的调用，调用的定时任务Handler 名为 RuleTask   参数自定义
     *
     * @param xxlJobInfoDto 定时任务的基础参数
     * @param token         the token
     * @param jobName       the job name
     * @return the r
     * @throws Exception 修改或保存一个定时任务失败
     * @author: xb  建议使用saveOrUpdate
     */
    @Deprecated
    @PostMapping("/job/api/save")
    R<Integer> save(@RequestBody XxlJobInfoDto xxlJobInfoDto, @RequestHeader("XXL-JOB-ACCESS-TOKEN") String token, @RequestHeader("jobName") String jobName) throws Exception;

}
