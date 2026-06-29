package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.component.AsyncComponent;
import cn.bctools.document.constant.Constant;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "升级版本时的数据适配2.1.7->2.1.8")
@RestController
@RequestMapping(value = "/upgrade")
public class UpgradeController {
    @Autowired
    AsyncComponent asyncComponent;
    @Autowired
    RedisUtils redisUtils;

    @Log
    @ApiOperation("获取此次上传的文件夹唯一key")
    @PostMapping
    @SneakyThrows
    public R<String> upgrade(@RequestParam("file") MultipartFile file) {
        InputStream inputStream = file.getInputStream();
        File file1 = FileUtil.file("upgrade.json");
        FileUtil.writeFromStream(inputStream, file1);
        JSONObject jsonObject = JSONObject.parseObject(FileUtil.readUtf8String(file1));
        asyncComponent.upgrade(jsonObject, UserCurrentUtils.getCurrentUser());
        FileUtil.del(file1);
        //添加锁定状态
        redisUtils.set(Constant.LOCK_COMMON_KEY, "2.1.7升级到2.1.8,数据适配");
        return R.ok();
    }

}


