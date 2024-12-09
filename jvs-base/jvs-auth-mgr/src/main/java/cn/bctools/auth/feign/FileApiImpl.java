package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.FileApi;
import cn.bctools.common.utils.R;
import cn.bctools.oss.mapper.SysFileMapper;
import cn.bctools.oss.po.OssFile;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xh
 */
@Slf4j
@Api(tags = "文件api")
@RequestMapping
@RestController
@AllArgsConstructor
public class FileApiImpl implements FileApi {

    SysFileMapper fileMapper;

    @Override
    public R insert(String entity) {
        OssFile ossFile = JSONObject.parseObject(entity, OssFile.class);
        fileMapper.insert(ossFile);
        return R.ok();
    }

}
