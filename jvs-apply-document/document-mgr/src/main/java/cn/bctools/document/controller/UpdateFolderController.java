package cn.bctools.document.controller;


import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.R;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.dto.UpdateFolderFileDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.vo.req.DcLibraryAddReqVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "文件夹上传")
@RestController
@RequestMapping(value = "/update/folder")
public class UpdateFolderController {
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    DcLibraryService dcLibraryService;

    @Log
    @ApiOperation("获取此次上传的文件夹唯一key")
    @GetMapping("/get/folder/key")
    public R<String> getLibrary() {
        String idStr = IdGenerator.getIdStr();
        return R.ok(idStr);
    }

    @Log
    @ApiOperation("文件绑定")
    @PostMapping("/update/file")
    public R<DcLibrary> getLibrary(@RequestBody UpdateFolderFileDto updateFolderFileDto) {
        //获取已经新建了的文件夹数据
        String redisKey = String.format(Constant.UPDATE_FOLDER, updateFolderFileDto.getKey());
        //分割路径
        String[] split = updateFolderFileDto.getFilePath().split("/");
        Map<String, String> pathMap = (HashMap) Optional.ofNullable(redisUtils.get(redisKey)).orElse(new HashMap<String, String>(split.length));
        if (!pathMap.containsKey(updateFolderFileDto.getFilePath())) {
            StringBuilder path = new StringBuilder();
            for (String s : split) {
                //记录上级目录
                String parentPath = path.toString();
                //防止第一条记录加入斜杠
                if (path.length() != 0) {
                    path.append("/");
                }
                path.append(s);
                //判断是否已经创建
                boolean containsKey = pathMap.containsKey(path.toString());
                if (!containsKey) {
                    //获取上级id 判断路径是否存在值 如果不存在就表示是第一层
                    String parentId = updateFolderFileDto.getParentId();
                    if (parentPath.length() != 0) {
                        parentId = pathMap.get(parentPath);
                    }
                    //创建文件夹
                    DcLibraryAddReqVo dcLibraryAddReqVo = new DcLibraryAddReqVo()
                            .setFileType(DcLibraryTypeEnum.directory)
                            .setId(parentId)
                            .setParentId(parentId)
                            .setName(s);
                    DcLibrary dcLibrary = dcLibraryService.add(UserCurrentUtils.getCurrentUser(), dcLibraryAddReqVo);
                    pathMap.put(path.toString(), dcLibrary.getId());
                }
            }
        }
        //文件入库
        String parentId = pathMap.get(updateFolderFileDto.getFilePath());
        DcLibrary dcLibrary = dcLibraryService.fileToSave(parentId, updateFolderFileDto.getBaseFile(), updateFolderFileDto.getFileName());
        redisUtils.set(redisKey, pathMap);
        return R.ok(dcLibrary);
    }

    @Log
    @ApiOperation("某一批次数据上传完成后的通知接口")
    @GetMapping("/{key}")
    public R getLibrary(@ApiParam("批次key") @PathVariable String key) {
        String redisKey = String.format(Constant.UPDATE_FOLDER, key);
        redisUtils.del(redisKey);
        return R.ok();
    }
}
