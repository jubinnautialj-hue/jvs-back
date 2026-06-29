package cn.bctools.im.api;

import cn.bctools.common.utils.R;
import cn.bctools.im.dto.DataPushDto;
import cn.bctools.im.dto.NotifyDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM服务API
 */
@FeignClient(value = "jvs-modules-im-mgr")
public interface ImServiceApi {

    /**
     * 数据推送
     *
     * @param dataPushDto
     * @return
     */
    @ApiOperation("数据推送")
    @PostMapping("/im/data/push")
    R<String> dataPush(@RequestBody DataPushDto dataPushDto);


    @ApiOperation("通知")
    @PostMapping("/im/notify")
    R<String> notify(@RequestBody NotifyDto notifyDto);

}
