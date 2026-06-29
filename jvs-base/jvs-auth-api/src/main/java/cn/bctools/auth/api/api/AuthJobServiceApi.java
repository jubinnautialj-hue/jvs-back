package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.SysJobDto;
import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 岗位信息获取接口
 *
 * @Author: GuoZi
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "job")
public interface AuthJobServiceApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/job";

    /**
     * 查询所有岗位
     *
     * @return 岗位信息集合 all
     */
    @GetMapping(PREFIX + "/query/all")
    R<List<SysJobDto>> getAll();

    /**
     * 根据岗位id查询单个岗位
     *
     * @param jobId 岗位id
     * @return 岗位信息 by id
     */
    @GetMapping(PREFIX + "/query/by/id/{jobId}")
    R<SysJobDto> getById(@ApiParam("岗位id") @PathVariable("jobId") String jobId);

    /**
     * 根据用户id查询岗位
     *
     * @param userId 用户id
     * @return 岗位信息 user by id
     */
    @GetMapping(PREFIX + "/query/user/by/id/{userId}")
    R<List<SysJobDto>> getUserById(@ApiParam("用户id") @PathVariable("userId") String userId);

    /**
     * 根据岗位id集合查询岗位集合
     * <p>
     * 返回集合的数量、顺序不能保证与入参一致
     *
     * @param jobIds 岗位id集合
     * @return 岗位信息集合 by ids
     */
    @PostMapping(PREFIX + "/query/by/ids")
    R<List<SysJobDto>> getByIds(@ApiParam("岗位id集合") @RequestBody List<String> jobIds);

    /**
     * Gets by names.
     *
     * @param jobNames the job names
     * @return the by names
     */
    @PostMapping(PREFIX + "/query/by/names")
    R<List<SysJobDto>> getByNames(@ApiParam("岗位名称集合") @RequestBody List<String> jobNames);

}
