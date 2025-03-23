package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.enums.DataSyncPluginEnums;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Administrator
 */
@Api(tags = "数据同步插件")
@Slf4j
@RestController
@RequestMapping("/data/sync/plugin")
public class DataSyncPluginController {
    RedisUtils redisUtils;

    @ApiOperation("获取插件")
    @GetMapping
    public R<List<DataSyncPluginEnums>> getDataSyncPlugin() {
        String string = redisUtils.get(RedisKey.getDataFactoryDataSyncPlugin()).toString();
        List<DataSyncPluginEnums> pluginEnums = JSONArray.parseArray(string, DataSyncPluginEnums.class);
        return R.ok(pluginEnums);
    }
}
